package com.demo.androidcar;

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
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity1 extends Activity implements
		WifiStateReceiver.BRInteraction {

	private Button button_send = null;
	private EditText et_ip = null;
	private EditText et_port = null;
	//	private EditText et_conent = null;
//	private TextView tv_history = null;
	private CheckBox checkBoxSwitch = null;
	private static int defaultPort = 8888;
	public static ArrayList<Socket> socketList = new ArrayList<Socket>();

	//	private OutputStream out = null;
	private Handler handler = null;
	private Socket s = null;
	String tag = "chatRoom";
	private BufferedReader buRead = null;

	private final int CONTROL_CAR = 0;
	private final int UPDATE_INPUT_CONTENT = 1;

	private final int FORWORD = 1,BACKWORD=2,LEFT=3,RIGHT=4,SFAST=5;

	String ip;
	private WifiStateReceiver wifiStateReceiver;
	EditText et;
	WifiManager wifiManager;
	WifiInfo wifiInfo;
	private volatile boolean isRuning = true;
	InetAddress address;
	MyThread t;
	ServerSocket ss;
	int temp;

	int k=0;
	/* 发送广播端的socket */
	private MulticastSocket ms;
	MulticastLock multicastLock;
	DatagramPacket dataPacket = null;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.getip_main);

		wifiStateReceiver = new WifiStateReceiver();

		wifiStateReceiver.setBRInteractionListener(this);

		// 获取wifi服务
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// 判断wifi是否开启
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}

		wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		et = (EditText) findViewById(R.id.EditText01);
		ip = intToIp(ipAddress);
		et.setText(ip);
	}

	private String intToIp(int i) {

		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}

	@Override
	protected void onStart() {

		Start();
		serverStart();
		// TODO Auto-generated method stub
		super.onStart();

		handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what)
				{
					case CONTROL_CAR:
						Log.d(tag, "更新历史记录"+msg.obj);
						temp=(Integer)msg.obj;
						et.setText(ip+" 接收的内容=======   "+temp);
						if(temp==FORWORD){//前进
							et.setText(ip+"-FORWARD");

						}else if(temp==BACKWORD){
							et.setText(ip+"-BACKWARD");

						}else if(temp==LEFT){

						}else if(temp==RIGHT){

						}

						break;

					case UPDATE_INPUT_CONTENT:
						Log.d(tag, "清空输入记录");
//	                    et_conent.setText("");//清空文本
						break;
				}
			}
		};

	}


	public void serverStart()
	{
		try {

			ss = new ServerSocket(defaultPort);

			Log.d(tag, "on serverStart==================");

			new Thread()
			{
				public void run()
				{
					while(true)
					{
						try {
//                            Log.d(tag, "on serverStart: ready to accept");
							s=ss.accept();
							socketList.add(s);
							buRead = new BufferedReader(new InputStreamReader(s.getInputStream(), "utf-8"));
//                            try {
//								Thread.sleep(3000);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
							String receive_content = null;
							while ((receive_content=readFromClient())!=null) {
								Log.d(tag,"客户端说："+receive_content);
								isRuning=false;//连接上之后不再广播
//                                String history_content = tv_history.getText().toString();
//                                history_content+=s.getInetAddress()+"说:"+receive_content+"\n";

								Message msg = new Message();
								msg.what = CONTROL_CAR;
								msg.obj = receive_content;
								handler.sendMessage(msg);


//                                for (Socket ss:socketList)
//                                {
//                                    OutputStream out=ss.getOutputStream();
//                                    out.write(("[服务器已经收到消息]"+"\n").getBytes("utf-8"));
//                                }
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
			}.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	private String readFromClient(){
		try {
			return buRead.readLine();
		} catch (Exception e) {
			//删除此Socket
			socketList.remove(s);
		}
		return null;
	}


	private void Start() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		this.registerReceiver(wifiStateReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(wifiStateReceiver);
		// if(wifiStateReceiver!=null){

		// }
		super.onDestroy();
		try {
			if(ss != null)
				ss.close();
//	            if(out != null)
//	                out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	// public static void wifiUpdate() {
	// // TODO Auto-generated method stub
	// ip = intToIp(wifiInfo.getIpAddress());
	// et.setText(ip);
	// }

	public void setText(String content) {
		if (content != null) {
			Log.e("APActivity", "CONNECTED");
			wifiInfo = wifiManager.getConnectionInfo();
			ip = intToIp(wifiInfo.getIpAddress());
			et.setText(ip);
			broadcast(ip);
			// textView.setText(content);
		}
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
			ms = new MulticastSocket(8003);
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

//			ip=""+k;
			byte[] data = ip.getBytes();
			// 224.0.0.1为广播地址

			// 这个地方可以输出判断该地址是不是广播类型的地址
			Log.e("APActivity=",
					address.isMulticastAddress() + "");
			while (isRuning) {
				// if(isRuning)
				// {
//				ip=""+k;
				data = ip.getBytes();
				dataPacket = new DatagramPacket(data, data.length, address, 8003);
				k++;
				try {
					Log.e("APActivity",
							"broadcast over");
					ms.send(dataPacket);
					Thread.sleep(3000);
					Log.e("APActivity", "broadcast over");
					// System.out.println("再次发送ip地址广播:.....");
//					isRuning = false;

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