package com.demo.androidcar.util;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.demo.androidcar.R;

public class MusicService extends Service {


	public IBinder musicBinder  = new MyBinder();

	//获取到activity的Handler，用来通知更新进度条
	Handler mHandler;

	//播放音乐的媒体类
	MediaPlayer mediaPlayer;

	//本地歌曲的路径
	String path = "/storage/sdcard1/Music/浪漫满屋.mp3";

	private String TAG = "MyService";

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate() executed");

		init();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		//当绑定后，返回一个musicBinder
		return musicBinder;
	}

	public class MyBinder extends Binder{

		public Service getService(){
			return MusicService.this;
		}
	}

	//初始化音乐播放
	void init(){
		//进入Idle
		mediaPlayer =MediaPlayer.create(this,R.raw.little_apple); //new MediaPlayer();
		try {
			//初始化
//			mediaPlayer.setsetDataSource(R.id.list);

			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

			// prepare 通过异步的方式装载媒体资源
			mediaPlayer.prepareAsync();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//返回当前的播放进度，是double类型，即播放的百分比
	public double getProgress(){
		int position = mediaPlayer.getCurrentPosition();

		int time = mediaPlayer.getDuration();

		double progress = (double)position / (double)time;

		return progress;
	}

	//通过activity调节播放进度
	public void setProgress(int max , int dest){
		int time = mediaPlayer.getDuration();
		mediaPlayer.seekTo(time*dest/max);
	}

	//测试播放音乐
	public void play(){
		if(mediaPlayer != null){
			mediaPlayer.start();
		}

	}
	//暂停音乐
	public void pause() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}

	public void playSong(){
		if (mediaPlayer != null){

			mediaPlayer.reset();
			try {
				mediaPlayer.setDataSource(this,Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.little_apple));
				mediaPlayer.setLooping(true);
				mediaPlayer.prepare();
				mediaPlayer.start();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void playStory(){
		if (mediaPlayer != null){

			mediaPlayer.reset();
			try {
				mediaPlayer.setDataSource(this,Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.whiteprincess));
				mediaPlayer.prepare();
				mediaPlayer.setLooping(true);
				mediaPlayer.start();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	//service 销毁时，停止播放音乐，释放资源
	@Override
	public void onDestroy() {
		// 在activity结束的时候回收资源
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		super.onDestroy();
	}
}