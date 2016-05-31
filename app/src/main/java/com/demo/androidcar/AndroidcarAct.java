package com.demo.androidcar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
public class AndroidcarAct extends Activity {


	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;//客户端socket
	private OutputStream outStream = null;
	private InputStream  InStream = null;
	public  boolean BluetoothFlag  = true;
	public  int speed = 0;//0:停止  1:前进  2:后退
	public  int tansState = 0;
	private int len = 0;
	private byte[] databuffer;
	public  boolean send =true;
	SensorManager mySensorManager;  //SensorManager对象引用

	Button   mButtonF;
	Button   mButtonB;
	Button   mButtonL;
	Button   mButtonR;
	Button   mButtonS;
	Button   mButtonAuto;
	Button   mButtonSend;
	TextView myText,recText;
	SeekBar  seekBarPWM,seekBarS;

	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static String address = "98:D3:31:B3:91:24"; // <==要连接的蓝牙设备MAC地址

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		myText = (TextView) findViewById(R.id.myText);
		myText.setText("蓝牙还不可用,请稍后...");
		recText = (TextView) findViewById(R.id.recText);
		seekBarPWM = (SeekBar) findViewById(R.id.seekBarPWM);
		seekBarPWM.setMax(100);
		seekBarS = (SeekBar) findViewById(R.id.seekBarS);
		seekBarS.setMax(180);

		// 设置拖动条改变监听器  速度
		OnSeekBarChangeListener osbcl = new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				speed = seekBarPWM.getProgress();
				myText.setText("当前速度：" + speed);

				byte[] msgBuffer = new byte[5];
				msgBuffer[0] = (byte)(0xFF);
				msgBuffer[1] = (byte)(0x00);
				msgBuffer[2] = (byte)(0x04);
				msgBuffer[3] = (byte)(speed & 0xff);
				msgBuffer[4] = (byte)(0xFE);
				sendCmd(msgBuffer);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Toast.makeText(getApplicationContext(), "onStartTrackingTouch",
				//   Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Toast.makeText(getApplicationContext(), "onStopTrackingTouch",
				//   Toast.LENGTH_SHORT).show();
			}
		};
		// 为拖动条绑定监听器
		seekBarPWM.setOnSeekBarChangeListener(osbcl);

		// 设置拖动条改变监听器  速度
		OnSeekBarChangeListener osbc2 = new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int angle = seekBarS.getProgress();
				myText.setText("舵机角度：" + angle);

				byte[] msgBuffer = new byte[5];
				msgBuffer[0] = (byte)(0xFF);
				msgBuffer[1] = (byte)(0x04);
				msgBuffer[2] = (byte)(0x00);
				msgBuffer[3] = (byte)(angle & 0xff);
				msgBuffer[4] = (byte)(0xFE);
				sendCmd(msgBuffer);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Toast.makeText(getApplicationContext(), "onStartTrackingTouch",
				//   Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Toast.makeText(getApplicationContext(), "onStopTrackingTouch",
				//   Toast.LENGTH_SHORT).show();
			}
		};
		// 为拖动条绑定监听器
		seekBarS.setOnSeekBarChangeListener(osbc2);

		//前进
		mButtonF=(Button)findViewById(R.id.btnF);
		mButtonF.setOnTouchListener(new Button.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					byte[] msgBuffer = new byte[5];
					msgBuffer[0] = (byte)(0xFF);
					msgBuffer[1] = (byte)(0x00);
					msgBuffer[2] = (byte)(0x02);
					msgBuffer[3] = (byte)(speed & 0xff);
					msgBuffer[4] = (byte)(0xFE);
					sendCmd(msgBuffer);
				}
				return false;
			}
		});


		//后退
		mButtonB=(Button)findViewById(R.id.btnB);
		mButtonB.setOnTouchListener(new Button.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					byte[] msgBuffer = new byte[5];
					msgBuffer[0] = (byte)(0xFF);
					msgBuffer[1] = (byte)(0x00);
					msgBuffer[2] = (byte)(0x03);
					msgBuffer[3] = (byte)(speed & 0xff);
					msgBuffer[4] = (byte)(0xFE);
					sendCmd(msgBuffer);
				}
				return false;
			}
		});

		//左转
		mButtonL=(Button)findViewById(R.id.btnL);
		mButtonL.setOnTouchListener(new Button.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					byte[] msgBuffer = new byte[5];
					msgBuffer[0] = (byte)(0xFF);
					msgBuffer[1] = (byte)(0x00);
					msgBuffer[2] = (byte)(0x00);
					msgBuffer[3] = (byte)(speed & 0xff);
					msgBuffer[4] = (byte)(0xFE);
					sendCmd(msgBuffer);
				}
				else
				{
					byte[] msgBuffer = new byte[5];
					msgBuffer[0] = (byte)(0xFF);
					msgBuffer[1] = (byte)(0x00);
					msgBuffer[2] = (byte)(0x04);
					msgBuffer[3] = (byte)(0x00);
					msgBuffer[4] = (byte)(0xFE);
					sendCmd(msgBuffer);
				}
				return false;
			}
		});

		//右转
		mButtonR=(Button)findViewById(R.id.btnR);
		mButtonR.setOnTouchListener(new Button.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					byte[] msgBuffer = new byte[5];
					msgBuffer[0] = (byte)(0xFF);
					msgBuffer[1] = (byte)(0x00);
					msgBuffer[2] = (byte)(0x01);
					msgBuffer[3] = (byte)(speed & 0xff);
					msgBuffer[4] = (byte)(0xFE);
					sendCmd(msgBuffer);
				}
				else
				{
					byte[] msgBuffer = new byte[5];
					msgBuffer[0] = (byte)(0xFF);
					msgBuffer[1] = (byte)(0x00);
					msgBuffer[2] = (byte)(0x04);
					msgBuffer[3] = (byte)(0x00);
					msgBuffer[4] = (byte)(0xFE);
					sendCmd(msgBuffer);
				}
				return false;
			}
		});
		//停止
		mButtonS=(Button)findViewById(R.id.btnS);
		mButtonS.setOnTouchListener(new Button.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					byte[] msgBuffer = new byte[5];
					msgBuffer[0] = (byte)(0xFF);
					msgBuffer[1] = (byte)(0x00);
					msgBuffer[2] = (byte)(0x04);
					msgBuffer[3] = (byte)(0x00);
					msgBuffer[4] = (byte)(0xFE);
					sendCmd(msgBuffer);
				}
				return false;
			}
		});


		mButtonAuto=(Button)findViewById(R.id.btnAuto);
		mButtonAuto.setEnabled(false);
		mButtonAuto.setOnTouchListener(new Button.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					byte[] msgBuffer = new byte[5];
					msgBuffer[0] = (byte)(0xFF);
					msgBuffer[1] = (byte)(0x03);
					msgBuffer[2] = (byte)(0x01);
					msgBuffer[3] = (byte)(0x01);
					msgBuffer[4] = (byte)(0xFE);
					sendCmd(msgBuffer);
				}
				return false;
			}
		});

		mButtonSend=(Button)findViewById(R.id.btnSend);
		mButtonSend.setEnabled(false);
		mButtonSend.setOnTouchListener(new Button.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					if (send)
					{
						byte[] msgBuffer = new byte[5];
						msgBuffer[0] = (byte)(0xFF);
						msgBuffer[1] = (byte)(0x03);
						msgBuffer[2] = (byte)(0x02);
						msgBuffer[3] = (byte)(0x01);
						msgBuffer[4] = (byte)(0xFE);
						sendCmd(msgBuffer);
						send = false;
						mButtonSend.setText("停止测距");
					}
					else
					{
						byte[] msgBuffer = new byte[5];
						msgBuffer[0] = (byte)(0xFF);
						msgBuffer[1] = (byte)(0x03);
						msgBuffer[2] = (byte)(0x02);
						msgBuffer[3] = (byte)(0x00);
						msgBuffer[4] = (byte)(0xFE);
						sendCmd(msgBuffer);
						send = true;
						mButtonSend.setText("启动测距");
					}
				}
				return false;
			}
		});


		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "蓝牙设备不可用，请打开蓝牙！", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(this,  "请打开蓝牙并重新运行程序！", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); //获得SensorManager对象
	}

	//开发实现了SensorEventListener接口的传感器监听器
	private SensorEventListener mySensorEventListener = new SensorEventListener(){
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{//重写onAccuracyChanged方法，在此为空实现
		}

		@Override
		public void onSensorChanged(SensorEvent event)
		{
			if(event.sensor.getType() == Sensor.TYPE_ORIENTATION)
			{//判断是否为加速度传感器变化产生的数据
				float [] values = event.values;  //获得捕获的数据
				myText.setText("Yaw:" +values[0] + " Pitch:" +values[1] + " Roll:"+values[2]);

			}
		}
	};

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onResume()
	{
		mySensorManager.registerListener(//调用方法为SensorManager注册监听器
				mySensorEventListener,   //实现了SensorEventListener接口的监听器对象
				mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), //Sensor对象
				SensorManager.SENSOR_DELAY_UI  //系统传递SensorEvent事件的频度
		);
		super.onResume();

		DisplayToast("正在尝试连接蓝牙设备，请稍后····");
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		try
		{
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		}
		catch (IOException e) {
			DisplayToast("套接字创建失败！");
		}
		DisplayToast("成功连接蓝牙设备！");
		mBluetoothAdapter.cancelDiscovery();
		try
		{
			btSocket.connect();
			DisplayToast("连接成功建立，可以开始操控了~~~");
			myText.setText("蓝牙设备已准备好了!");
			BluetoothFlag = true;
			MyThread bluetoothThread = new MyThread();
			bluetoothThread.start();
		}
		catch (IOException e) {
			try
			{
				btSocket.close();
			} catch (IOException e2) {
				DisplayToast("连接没有建立，无法关闭套接字！");
			}
		}
	}

	@Override
	public void onPause()
	{
		mySensorManager.unregisterListener(mySensorEventListener); //取消注册监听器
		super.onPause();
		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			btSocket.close();
			BluetoothFlag = false;//关闭蓝牙读线程
		} catch (IOException e2) {
			DisplayToast("套接字关闭失败！");
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void DisplayToast(String str)
	{
		Toast toast=Toast.makeText(this, str, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP, 0, 220);
		toast.show();
	}

	public void sendCmd(byte[] msgBuffer){
		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			outStream.write(msgBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if (msg.what == 1)
			{
				String recvMessage = "";

				for (int n=0; n<len; n++)
				{
					int num = (int)databuffer[n] & 0xFF;
					if (num <16)
					{
						recvMessage = recvMessage + "0" + Integer.toHexString(databuffer[n] & 0xFF) +  " ";
					}
					else
					{
						recvMessage = recvMessage + Integer.toHexString(databuffer[n] & 0xFF) +  " ";
					}
				}
				recText.append(recvMessage);	// 刷新
			}
		}
	};

	public class  MyThread extends Thread {
		MyThread()
		{
			BluetoothFlag = true;
			try {
				InStream = btSocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(BluetoothFlag){
				try{
					Thread.sleep(100);
				}catch (Exception e) {
					e.printStackTrace();
				}
				try {
					len = InStream.available();
					if (len>0)
					{
						//recvMessage = byte2HexStr(tmp,len);
						databuffer =new byte[len];
						InStream.read(databuffer,0,len);
						Message msg = new Message();
						msg.what = 1;
						mHandler.sendMessage(msg);
					}

				} catch (IOException e ) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * bytes转换成十六进制字符串
	 * @param byte[] b byte数组
	 * @return String 每个Byte值之间空格分隔
	 */
	public static String byte2HexStr(byte[] b,int len)
	{
		String stmp="";
		StringBuilder sb = new StringBuilder("");
		for (int n=0;n<len;n++)
		{
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length()==1)? "0"+stmp : stmp);
			sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}
}