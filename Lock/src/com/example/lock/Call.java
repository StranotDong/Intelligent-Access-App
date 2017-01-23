package com.example.lock;

import java.io.IOException;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class Call extends Activity{
	Button hangupButton;
	Button answerButton;
	private MediaPlayer mMediaPlayer;
	private WakeLock wakeLock;
	
	public final long TIMER = 45000;//定时45s
	
	private Vibrator vibrator;
	
	TimerThread timer = new TimerThread();
	
	private boolean isplayRing = false;
		
	//播放铃声的方法
    private void playLocalFile() {        
        mMediaPlayer = MediaPlayer.create(this,R.raw.ring);       
        try {    
        	mMediaPlayer.prepare();       
        }catch (IllegalStateException e) {                   
        }catch (IOException e) { 
        }
        mMediaPlayer.start();  
        
        Log.d("Call", "Ringplay");
    
    	mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {    
    		public void onCompletion(MediaPlayer mp) {  
    			// TODO Auto-generated method stub  
    			// 循环播放  
    			try {  
    				mp.start();  
    			} catch (IllegalStateException e) {  
            	// TODO Auto-generated catch block  
    				e.printStackTrace();  
    			}  
    		}  
    	}); 
    	isplayRing = true;

    }
    
    //定时45s后自动挂断
    private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case 1:
    			stopService(new Intent(Call.this,ScreenLockService.class));//关闭ScreenLockService，回到锁屏界面
				Intent hangupIntent = new Intent(Call.this, Main.class);
				startActivity(hangupIntent);
				break;
			default:
				break;
    			
    		}
    	}
    };

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d("Call", "Create");
		
		
		playLocalFile();//响铃
				
		//震动
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);  
        long [] pattern = 
        	{500, 500,
        	500, 500,};   // 停止 开启 停止 开启   
        vibrator.vibrate(pattern, 1); 
//        vibrator.vibrate(TIMER);
		
		timer.start();//定时开始
		/*设置全屏，无标题*/
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				   WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.call);	
				
		acquireWakeLock();//屏幕亮//////////////////////////////////////////////////////////////////////
	}
	

	public void onResume(){
		super.onResume();
		
//		playLocalFile();//响铃
		
		Log.d("Call", "Resume");
		acquireWakeLock();//屏幕亮/////////////////////////////////////////////////////////////////////
				
		hangupButton = (Button)this.findViewById(R.id.hangup);
		answerButton = (Button)this.findViewById(R.id.answer);
		
		//按下挂断button，回到Main活动,并令分机挂断
		hangupButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) { 
				stopService(new Intent(Call.this,ScreenLockService.class));//关闭ScreenLockService，回到锁屏界面
				Intent hangupIntent = new Intent(Call.this, Main.class);
				startActivity(hangupIntent);
				
				//发送广播给CallListenerServer,令分机挂断
				Intent intent = new Intent("android.com.lock.INDOORHANGUP");
				sendBroadcast(intent);
			}  
		});
		
		//按下接通button，打开video界面,并令分机挂断
		answerButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent answerIntent = new Intent(Call.this, Video_open.class);
				startActivity(answerIntent);
				
				//发送广播给CallListenerServer,令分机挂断
				Intent intent = new Intent("android.com.lock.INDOORHANGUP");
				sendBroadcast(intent);
			}  
		});
	} 
	
    public void onStop() {  
    	super.onStop();  
        // TODO Auto-generated method stub  
        // 服务停止时停止播放音乐并释放资源  
//    	mMediaPlayer.stop();  
    	mMediaPlayer.release();
    	vibrator.cancel();
    	
    	timer.exit = true;
    	releaseWakeLock();/////////////////////////////////////////////////////////////////////
    	
    } 
    
    public void onDestroy() {  
    	super.onDestroy();  
        // TODO Auto-generated method stub  
        // 服务停止时停止播放音乐并释放资源  
//    	mMediaPlayer.stop();  
//    	mMediaPlayer.release();
    	
    	releaseWakeLock();//////////////////////////////////////////////////////////////////////
    	
    } 
    
	//屏蔽掉Back键
	public boolean onKeyDown(int keyCode ,KeyEvent event){
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true ;

		else
			return super.onKeyDown(keyCode, event);
		
	}
    
    
    private void acquireWakeLock() {
    	if (wakeLock == null) {
//    		Log.d("Acquiring wake lock", null);
    		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    		wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
    		wakeLock.acquire();
    	}

    	}


    private void releaseWakeLock() {
    	if (wakeLock !=null && wakeLock.isHeld()) {
    		wakeLock.release();
    		wakeLock =null;
    	}

    }
    
    //定时45s挂断
    class TimerThread extends Thread{
    	public volatile boolean exit = false; 
    	public volatile int i = 0; 
		public void run(){
			while(!exit){
			try {  
                Thread.sleep(1);  
                i++;
                if(i >= TIMER){
                Message msg = new Message();  
                msg.what = 1;  
                handler.sendMessage(msg);  
                System.out.println("send...");
                i = 0;
                }
            } catch (Exception e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
                System.out.println("thread error...");  
            }
			}
		}
    }
    
}
