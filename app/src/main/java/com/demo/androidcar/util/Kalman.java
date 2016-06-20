package com.demo.androidcar.util;

public class Kalman {
	private static float[]   data_esti={0,0};
	private float[]   E = new float[2];
	private float[]   R ={0.2f,0.2f};
	private float[]   Q ={0.01f,0.05f};
	private float[][] P ={{1,0},{0,1}};
	private float[][] A ={{1,0.006f},{0,1}};
	private float[][] K =new float[2][2];

	
	public Kalman(float []Angle_meas)
	{
		data_esti[0] = Angle_meas[0];
		data_esti[1] = Angle_meas[1];
	}
	
	public  void Write_A(float Ts)
	{
		A[0][1] = Ts;
	}
	public  void Write_R(float[] data)
	{
		R[0] = data[0];
		R[1] = data[1];
	}
	public  void Write_Q(float[] data)
	{
		Q[0] = data[0];
		Q[1] = data[1];
	}
	
	public float[] Get_Esti(float []Angle_meas)	
	{
		float a,b,c,d;		
		float [][] P2 = new float[2][2];
		data_esti[0] += Angle_meas[1] * A[0][1];
		P2[0][0] = P[0][0] + (P[1][0]+P[0][1]+P[1][1]*A[0][1])*A[0][1] + Q[0];
		P2[0][1] = P[0][1] + P[1][1]*A[0][1];
		P2[1][0] = P[1][0] + P[1][1]*A[0][1];
		P2[1][1] = P[1][1] + Q[1];
		a = P2[0][0]+R[0];
		b = P2[1][1]+R[1];
		c = P2[0][1]*P2[1][0];
		d = 1/(a*b-c); 
		K[0][0] = (P2[0][0]*b-c)*d;
		K[0][1] = P2[0][1]*R[0]*d;
		K[1][0] = P2[1][0]*R[1]*d;
		K[1][1] = (P2[1][1]*a-c)*d;
		P[0][0] = (1-K[0][0])*P2[0][0]-K[0][1]*P2[1][0];
		P[0][1] = (1-K[0][0])*P2[0][1]-K[0][1]*P2[1][1];
		P[1][0] = (1-K[1][1])*P2[1][0]-K[1][0]*P2[0][0];
		P[1][1] = (1-K[1][1])*P2[1][1]-K[1][0]*P2[0][1];		
		E[0] = Angle_meas[0] - data_esti[0];
		E[1] = Angle_meas[1] - data_esti[1];
		data_esti[0] += K[0][0]*E[0] + K[0][1]*E[1];
		data_esti[1] += K[1][0]*E[0] + K[1][1]*E[1];
		
		return data_esti;
	}
}
