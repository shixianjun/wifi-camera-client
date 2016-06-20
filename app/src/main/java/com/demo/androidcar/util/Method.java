package com.demo.androidcar.util;

import android.hardware.Camera;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;

public class Method{
	private static float   R1=1/9.805f;
	private static float   R2=180/3.1415926f;

	public static float[] Get_angle(float angle_meas, float accer_meas)
	{
		float [] data = new float[2];
		angle_meas *= R1;
		if (angle_meas > 1)
			data[0] = 90;
		else if (angle_meas < -1)
			data[0] = -90;
		else
		{
			data[0] = (float)Math.asin(angle_meas)*R2 ;
		}
		data[1] = accer_meas * R2;
		return data;
	}

	// 转换long型为byte数组
	public static void putLong(byte[] b, int index, long x) {
		for(int i = 0;i < 8;i++){
			b[index+i] = (byte)x;
			x = x >> 8;
		}
	}

	// double转换byte 数组
	public static void putDouble(byte[] b, int index, double x) {
		// byte[] b = new byte[8];
		long l = Double.doubleToLongBits(x);
		putLong(b, index, l);
	}

	// 转换int型为byte数组
	public static void putInt(byte[] b, int index, int x){
		for(int i = 0;i < 4;i++){
			b[index+i] = (byte)x;
			x = x >> 8;
		}
	}
	// 转换float型为byte数组,前面是低位，后面是高位
	public static void putFloat(byte[] b, int index, float x){
		int y =Float.floatToIntBits(x);
		for(int i = 0;i < 4;i++){
			b[index+i] = (byte)y;
			y = y >> 8;
		}
	}

	// 转换byte数组为float型
	public static float byte2float(byte[] b, int index) {
		int x =0;
		for (int i = 3;i > 0;i--){
			x += b[index + i] & 0xFF;
			x = x << 8;
		}
		x += b[index ] & 0xFF;
		return Float.intBitsToFloat(x);
	}

	public static byte[] RequestMessage(String name,int port){
		try {
			byte[] bName=name.getBytes("UTF8");
			int nameLength=bName.length;
			byte[] requestMessage=new byte[10+nameLength];
			System.arraycopy(bName,0,requestMessage,4,nameLength);
			requestMessage[0]=(byte)0xfe;
			requestMessage[1]=(byte)0xfe;
			requestMessage[2]=(byte)0x0f;
			requestMessage[3]=(byte)0x02;
			requestMessage[8+nameLength]=(byte)0xef;
			requestMessage[9+nameLength]=(byte)0xef;
			putInt(requestMessage, 4+nameLength, port);

			return requestMessage;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] SensorData(float[] data){
		int Ns=data.length;
		int Nbf=2+1+1+8+Ns*4+2;
		long time = Calendar.getInstance().getTimeInMillis();

		byte[] bytedata = new byte[Nbf];
		bytedata[0]=(byte)0xFE;
		bytedata[1]=(byte)0xFE;
		bytedata[2]=(byte)0x03;
		Method.putLong(bytedata,3,time);
		bytedata[11]=(byte)Ns;
		//putLong(bytedata,4,Tn);
		for (int i=0;i<Ns;i++){
			Method.putFloat(bytedata,12+i*4,data[i]);
		}
		//putFloat(bytedata,12+(Ns-1)*4,(float)90.00);
		bytedata[Nbf-2]=(byte)0xEF;
		bytedata[Nbf-1]=(byte)0xEF;
		return bytedata;
	}
	public static byte[] CarData(byte[] buffer){
		byte [] bytedata=new byte[16];
		System.arraycopy(buffer, 1, bytedata, 11, 3);
		bytedata[0]=(byte)0xFE;
		bytedata[1]=(byte)0xFE;
		bytedata[2]=(byte)0x0E;
		long time = Calendar.getInstance().getTimeInMillis();
		Method.putLong(bytedata,3,time);
		bytedata[14]=(byte)0xEF;
		bytedata[15]=(byte)0xEF;
		return bytedata;
	}
	public static byte[] TurningData(float turning){
		byte[] data2server=new byte[10];
		data2server[0]=(byte)0xFE;
		data2server[1]=(byte)0xFE;
		data2server[2]=(byte)0x06;
		data2server[3]=(byte)0x00;
		Method.putFloat(data2server,4,turning);
		data2server[8]=(byte)0xEF;
		data2server[9]=(byte)0xEF;
		return data2server;
	}
	public static String IpAddress(){
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("WifiIpAddress", ex.toString());
		}


		return null;
	}

	public static int FindFrontCamera(){
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number

		for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
			Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo
			if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) {
				// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
				return camIdx;
			}
		}
		return 0;
	}
}