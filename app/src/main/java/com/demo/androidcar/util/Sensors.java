package com.demo.androidcar.util;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Sensors{
	//传感器相关变量
	/*重力、陀螺仪、加速度       */
	private final int[] sensortypes={Sensor.TYPE_GRAVITY,Sensor.TYPE_GYROSCOPE,Sensor.TYPE_ACCELEROMETER};
	public  int numberofsensor=sensortypes.length;
	public  float[] value_ins =new float[numberofsensor*3];
	private boolean [] sensorRegistered =new boolean[numberofsensor];
	private SensorManager sm;
	private MySensorEventListener mySensorEventListener;

	public Sensors(SensorManager mysm){
		this.sm =mysm;
		mySensorEventListener=new MySensorEventListener();
	}
	public void opensensor(int i)//打开第i个传感器
	{
		Sensor sensorname = sm.getDefaultSensor(sensortypes[i]);
		if(!sensorRegistered[i])
			sm.registerListener(mySensorEventListener, sensorname, SensorManager.SENSOR_DELAY_FASTEST);
		sensorRegistered[i] =true;
	}

	public void closesensor(int i)//关闭第i个传感器
	{
		if(sensorRegistered[i])
			sm.unregisterListener(mySensorEventListener,sm.getDefaultSensor(sensortypes[i]));
		sensorRegistered[i]=false;
	}
	public class MySensorEventListener implements SensorEventListener {

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub

			for(int j=0;j<numberofsensor;j++)
			{
				if(event.sensor.getType()==sensortypes[j])
				{
					for (int i=0;i<3;i++){
						value_ins[i+j*3]=event.values[i];
					}
				}
			}
		}
	}
}