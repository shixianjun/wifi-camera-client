package com.demo.androidcar.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BlueTooth{
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private InputStream  InStream = null;
	private boolean BluetoothFlag  = true;

	private byte[] databuffer;
	public  byte[] inbuffer;

	private Handler mHandler;

	/**要连接的蓝牙设备MAC地址 */
	private String btaddress = "98:D3:31:40:8D:9E";
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public String recvMessage;

	public BlueTooth(Handler mHandler,String btaddress){
		this.mHandler=mHandler;
		this.btaddress=btaddress;
	}

	public boolean OpenBlueTooth(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			ShowMessage(2,"蓝牙设备不可用，请打开蓝牙！");
			return false;
		}
		if (!mBluetoothAdapter.isEnabled()) {
			ShowMessage(2,"请打开蓝牙并重新运行程序！");
			return false;
		}
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btaddress);
		try
		{
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			ShowMessage(2,"成功连接蓝牙设备！");
			mBluetoothAdapter.cancelDiscovery();
			btSocket.connect();
			ShowMessage(2,"连接成功建立，可以开始操控了~~~！");
			MyBluetoothThread bluetoothThread = new MyBluetoothThread();
			bluetoothThread.start();
		}
		catch (IOException e) {
			ShowMessage(2,"连接失败！");
			return false;
		}
		return true;
	}
	public void CloseBlueTooth(){
		BluetoothFlag=false;
		try {
			outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public class  MyBluetoothThread extends Thread {
		MyBluetoothThread()
		{
			BluetoothFlag = true;
			try {
				InStream = btSocket.getInputStream();
				outStream = btSocket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				while(BluetoothFlag){
					int len = InStream.available();
					if (len > 0)
					{
						//recvMessage = byte2HexStr(tmp,len);
						databuffer =new byte[len];
						InStream.read(databuffer,0,len);
						inbuffer=databuffer.clone();
						ShowMessage(1,"新指令");
					}
				}
				InStream.close();
				btSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void sendCmd(byte[] msgBuffer){
		try {
			outStream.write(msgBuffer);
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void ShowMessage(int w,String message){
		Message msg = new Message();
		msg.what = w;
		recvMessage = message;
		mHandler.sendMessage(msg);
	}
}