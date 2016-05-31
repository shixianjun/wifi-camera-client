package com.demo.androidcar;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.androidcar.util.MusicService;
import com.demo.androidcar.util.MusicService.MyBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity2 extends Activity implements
        WifiStateReceiver.BRInteraction {

    private Button button_send = null, button_play = null, button_pause = null;
    private EditText et_ip = null;
    private EditText et_port = null;
    private EditText et_conent = null;
    private TextView tv_history = null, et = null;
    private CheckBox checkBoxSwitch = null;
    private static int defaultPort = 8888;
    public static ArrayList<Socket> socketList = new ArrayList<Socket>();

    private OutputStream out = null;
    private Handler handler = null;
    private Socket s = null;
    String tag = "chatRoom";
    private BufferedReader buRead = null;

    private final int UPDATE_HISTORY_CONTENT = 0;
    private final int UPDATE_INPUT_CONTENT = 1;
    private final int UPDATE_IP_CONTENT = 3;
    private final int MUSIC_PLAYSONG = 4;
    private final int MUSIC_PLAYSTORY = 5;
    private final int MUSIC_PAUSE = 6;

    private final int CAR_LEFT = 7;
    private final int CAR_RIGHT = 8;
    private final int CAR_FORWARD = 9;
    private final int CAR_BACK = 10;

    private WifiStateReceiver wifiStateReceiver;
    ServerSocket ss;
    WifiManager wifiManager;
    WifiInfo wifiInfo;
    MyThread t;
    InetAddress address;

    String local_ip, dest_ip;

    private volatile boolean isRuning = true, serverrun = true, mBound = false;

    private MulticastSocket ms;
    MulticastLock multicastLock;
    DatagramPacket dataPacket = null;

    Thread clientThread, serverThread;

    private MusicService audioService;
    Intent serviceIntent;

//	private CameraManager cameraManager;
//	private OrientationManager orientationManager;
//	private SurfaceView surfacePreview;

    private int selectedPreviewSize = 1;
    private RelativeLayout layoutParent;

    // 使用ServiceConnection来监听Service状态的变化
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            audioService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            // 这里我们实例化audioService,通过binder来实现
            MyBinder myBinder = (MyBinder) binder;
            // 获取service
            audioService = (MusicService) myBinder.getService();
            mBound = true;
            button_play.setEnabled(true);
            button_pause.setEnabled(true);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
//		orientationManager = new OrientationManager(this);
//		cameraManager = new CameraManager(selectedPreviewSize);
//		cameraManager.setCameraManagerListener(this);
//		layoutParent = (RelativeLayout) findViewById(R.id.layout_parent);
//		layoutParent.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				cameraManager.requestAutoFocus();
//			}
//		});


        wifiStateReceiver = new WifiStateReceiver();

        wifiStateReceiver.setBRInteractionListener(this);


        // 获取wifi服务
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // 判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        } else {

            wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            et = (TextView) findViewById(R.id.TextView_ip_localip);
            local_ip = intToIp(ipAddress);
            et.setText(local_ip);
        }

        // 绑定service;
        serviceIntent = new Intent(this, MusicService.class);

        // 如果未绑定，则进行绑定
        if (!mBound) {
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }

    }

    public void init() {
        button_send = (Button) findViewById(R.id.button_send);
        et_ip = (EditText) findViewById(R.id.editText_ip);
        et_port = (EditText) findViewById(R.id.EditText_port);
        et_conent = (EditText) findViewById(R.id.EditText_content);
        tv_history = (TextView) findViewById(R.id.textView_history_content);
        checkBoxSwitch = (CheckBox) findViewById(R.id.checkBox_server_start);

        button_play = (Button) findViewById(R.id.button_play);
        button_pause = (Button) findViewById(R.id.button_pause);
        button_play.setEnabled(false);
        button_pause.setEnabled(false);

        // et = (TextView)findViewById(R.id.TextView_ip_localip);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        initWifi();

        configure();

        serverStart();
        super.onStart();
    }

    private void initWifi() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(wifiStateReceiver, filter);
    }

    /**
     *
     */
    public void configure() {
        button_send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    String content = et_conent.getText().toString();// 读取用户输入文本

                    if (out == null) {
                        Log.d(tag, "the fucking out is null");
                        return;
                    }

                    out.write((content + "\n").getBytes("utf-8"));// 写入socket

                    String history_content = tv_history.getText().toString();
                    history_content += "你说:" + et_conent.getText() + "\n";

                    Message msg = new Message();
                    msg.what = UPDATE_HISTORY_CONTENT;
                    msg.obj = history_content;
                    handler.sendMessage(msg);

                    msg = new Message();
                    msg.what = UPDATE_INPUT_CONTENT;
                    msg.obj = "";
                    handler.sendMessage(msg);

                    Log.d(tag, "send success");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d(tag, "send failed " + e.getMessage());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d(tag, "send failed " + e.getMessage());
                }
            }
        });

        button_play.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mBound) {
                    audioService.play();

                }

            }
        });
        button_pause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mBound) {
                    audioService.pause();
                }

            }
        });

        checkBoxSwitch
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        // TODO Auto-generated method stub
                        if (isChecked) {
                            Log.d(tag, "clientStart");
                            clientStart();
                        } else {
                            Log.d(tag, "clientStop");
                            clientStop();
                        }
                    }
                });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPDATE_HISTORY_CONTENT:
                        Log.d(tag, "更新历史记录" + msg.obj);
                        tv_history.setText((String) msg.obj);
                        break;

                    case UPDATE_INPUT_CONTENT:
                        Log.d(tag, "清空输入记录");
                        et_conent.setText("");// 清空文本
                        break;

                    case UPDATE_IP_CONTENT:
                        Log.d(tag, "获取对方IP");
                        et_ip.setText((String) msg.obj);
                        isRuning=false;//
                        break;
                    case MUSIC_PLAYSONG:
                        Log.d(tag, "MUSIC_PLAYSONG=========");
                        if (mBound) {
                            audioService.playSong();

                        }

                        break;
                    case MUSIC_PLAYSTORY:
                        if (mBound) {
                            audioService.playStory();

                        }
                        break;
                    case MUSIC_PAUSE:
                        Log.d(tag, "获取对方IP");
                        if (mBound) {
                            audioService.pause();

                        }

                        break;
                }

            }

        };

    }

    public void serverStart() {
        try {
            ss = new ServerSocket(defaultPort);
            Log.d(tag, "on serverStart");

            serverThread = new Thread() {
                public void run() {
                    while (serverrun) {
                        try {
                            // Log.d(tag, "on serverStart: ready to accept");
                            s = ss.accept();
                            socketList.add(s);
                            buRead = new BufferedReader(new InputStreamReader(
                                    s.getInputStream(), "utf-8"));

                            String receive_content = null;
                            while ((receive_content = readFromClient()) != null) {

                                if (dest_ip == null) {
                                    dest_ip = s.getInetAddress()
                                            .getCanonicalHostName();
                                    Message msg = new Message();
                                    msg.what = UPDATE_IP_CONTENT;
                                    msg.obj = dest_ip;
                                    handler.sendMessage(msg);

                                    // Log.d(tag,
                                    // "fist time find dest ip address on serverStart: ready to accept="+dest_ip);
                                }
                                // Log.d(tag,"客户端说："+receive_content);
                                // isRuning=false;
                                Message msg = new Message();
                                if (receive_content.equals("musicplaysong")) {
                                    msg.what = MUSIC_PLAYSONG;
                                } else if (receive_content.equals("musicplaystory")) {
                                    msg.what = MUSIC_PLAYSTORY;
                                } else if (receive_content.equals("musicpause")) {
                                    msg.what = MUSIC_PAUSE;
                                } else {
                                    String history_content = tv_history.getText()
                                            .toString();
                                    history_content += s.getInetAddress() + "说:"
                                            + receive_content + "\n";


                                    msg.what = UPDATE_HISTORY_CONTENT;
                                    msg.obj = history_content;
                                }


                                handler.sendMessage(msg);

                                // for (Socket ss:socketList)
                                // {
                                // OutputStream out=ss.getOutputStream();
                                // out.write(("[服务器已经收到消息]"+"\n").getBytes("utf-8"));
                                // }
                            }

                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }
            };
            serverThread.start();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String readFromClient() {
        try {
            return buRead.readLine();
        } catch (Exception e) {
            // 删除此Socket
            socketList.remove(s);
        }
        return null;
    }

    public void clientStart() {

        if (dest_ip == null) {
            Toast.makeText(getApplicationContext(), "未获取对方地址，请稍后！",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        clientThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // String ip = et_ip.getText().toString();
                    String port = et_port.getText().toString();

                    if (!port.equals("") && port != null) {
                        s = new Socket(dest_ip, defaultPort);
                    } else {
                        s = new Socket(dest_ip, Integer.parseInt(port));
                    }

                    out = s.getOutputStream();
                    Log.d(tag, "clientStart success");

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(tag, "clientStart failed " + e.getMessage());
                }
            }
        });
        clientThread.start();

    }

    public void clientStop() {
        clientThread = null;
        try {
            if (s != null)
                s.close();
            if (out != null)
                out.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        this.unregisterReceiver(wifiStateReceiver);
        // if(wifiStateReceiver!=null){
        //
        // }
        serverrun = false;
        try {
            if (ss != null)
                ss.close();
            // if(out != null)
            // out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        unbindService(conn);
        stopService(serviceIntent);
        super.onDestroy();

    }

    public void setText(String content) {
        if (content != null) {
            Log.d("APActivityt", "CONNECTED,CONNECTED");
            wifiInfo = wifiManager.getConnectionInfo();
            local_ip = intToIp(wifiInfo.getIpAddress());
            et.setText("本地：" + local_ip);
            broadcast(local_ip);
            // textView.setText(content);
        }
    }

    private String intToIp(int i) {

        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + (i >> 24 & 0xFF);
    }

    private void allowMulticast() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();
    }

    private void broadcast(String ip) {
        // 发送的数据包，局网内的所有地址都可以收到该数据包
        allowMulticast();
        try {
            /* 创建socket实例 */
            ms = new MulticastSocket(8093);
            ms.setTimeToLive(1);
            // ms.setLoopbackMode(true);
            // 将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的
            address = InetAddress.getByName("224.0.0.1");

            ms.joinGroup(address);

            t = new MyThread();
            t.start();

            // Log.e("APActivity---1234--before--setText", "broadcast over");
            // ms.send(dataPacket);
            // Log.e("APActivity---1234-after---setText", "broadcast over");

        } catch (Exception e) {
            e.printStackTrace();
        }
        multicastLock.release();
    }

    class MyThread extends Thread {

        @Override
        public void run() {

            // ip=""+k;
            byte[] data = local_ip.getBytes();
            // 224.0.0.1为广播地址

            // 这个地方可以输出判断该地址是不是广播类型的地址
            // Log.e("APActivity---1234----setTextaddress.isMulticastAddress()=",
            // address.isMulticastAddress() + "");
            while (isRuning) {
                // if(isRuning)
                // {
                // ip=""+k;
                data = local_ip.getBytes();
                dataPacket = new DatagramPacket(data, data.length, address,
                        8093);
                // k++;
                try {
                    // Log.e("APActivity---1234-before---setText",
                    // "broadcast over");
                    ms.send(dataPacket);
                    Thread.sleep(3000);
                    // Log.e("APActivity---1234-after---setText",
                    // "broadcast over");
                    // System.out.println("再次发送ip地址广播:.....");
                    // isRuning = false;

                    // Log.e("APActivity---1234----setText", "broadcast over");

                } catch (Exception e) {
                    e.printStackTrace();
                }
                // }
            }
            try {

                ms.leaveGroup(address);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }// 自己不接收广播
            ms.close();

        }

    }

}
