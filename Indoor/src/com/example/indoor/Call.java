package com.example.indoor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
	
	Socket clientSocket = new Socket();
	SocketClientThread socketClientThread =new SocketClientThread();//新的socketThread对象
	//////////////////////////////////////////
	public String ip = "192.168.191.4";/////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	public final int port = 25000;
	
	private static final String TAG = "Indoor call";
	private static final String HANGUP = "HangUp";
	private static final String INDOOR_HANGUP = "IndoorHangUp";
	private static final int CALL_STOP = 0;
	
	public final long TIMER = 45000;//定时45s
	
	private Vibrator vibrator;
	
	TimerThread timer = new TimerThread();
	
	public static SQLiteDatabase db;
	private static int myClickCount;
		
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

    }
    
   
    private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
/*    		 //定时45s后自动挂断
    		case 1:
    			stopService(new Intent(Call.this,ScreenLockService.class));//关闭ScreenLockService，回到锁屏界面
				Intent hangupIntent = new Intent(Call.this, Main.class);
				startActivity(hangupIntent);
				break;*/
			//挂断
    		case CALL_STOP:
				stopService(new Intent(Call.this,ScreenLockService.class));//关闭ScreenLockService，回到锁屏界面
				Intent mainIntent = new Intent(Call.this, Main.class);
//				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(mainIntent);
				break;   	
    			
    		}
    	}
    };

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d("Call", "Create");
		
//		Starting main = new Starting();
		db = SQLiteDatabase.openOrCreateDatabase(Call.this.getFilesDir().toString()
				+ "/network.dbs", null);
//		main.db = db;
		//查询获得游标  
		try{
			Cursor cursor = db.query   ("tb_user",null,null,null,null,null,null); 
			cursor.getCount();
			myClickCount++;
			int i= myClickCount-1;
			if(cursor.getCount()!=0){
				ip = query(db,i);
			}
		}catch(Exception e){
//			createDb();
		}
		
		socketClientThread.start();
		
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
		
		Log.d("Call", "Resume");
		acquireWakeLock();//屏幕亮/////////////////////////////////////////////////////////////////////
		
//		playLocalFile();//响铃
		
		//定时45s自动挂断
		
		
		
		hangupButton = (Button)this.findViewById(R.id.hangup);
		answerButton = (Button)this.findViewById(R.id.answer);
		
		//按下挂断button，回到Main活动，发送信息挂断手机
		hangupButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) { 
				stopService(new Intent(Call.this,ScreenLockService.class));//关闭ScreenLockService，回到锁屏界面
				Intent hangupIntent = new Intent(Call.this, Main.class);
				startActivity(hangupIntent);
				
				//发送信息挂断手机
				if(clientSocket.isConnected()&&(!clientSocket.isClosed())){
	  	            try {
	  	            	//获取输出流 
	  					OutputStream out = clientSocket.getOutputStream();	  		            
	  		          //发送信息  
	  		           out.write((HANGUP+"\n").getBytes());  
	  		           out.flush();  

	  				} catch (IOException e) {
	  					// TODO Auto-generated catch block
	  					e.printStackTrace();
	  				}  

	  		    }
			}  
		});
		
		//按下接通button，打开video界面，发送信息挂断手机
		answerButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent answerIntent = new Intent(Call.this, Video_open.class);
				startActivity(answerIntent);
				
				//发送信息挂断手机
				if(clientSocket.isConnected()&&(!clientSocket.isClosed())){
	  	            try {
	  	            	//获取输出流 
	  					OutputStream out = clientSocket.getOutputStream();	  		            
	  		          //发送信息  
	  		           out.write((HANGUP+"\n").getBytes());  
	  		           out.flush();  

	  				} catch (IOException e) {
	  					// TODO Auto-generated catch block
	  					e.printStackTrace();
	  				}  

	  		    }
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
 //   	mMediaPlayer.stop();  
 //   	mMediaPlayer.release();
    	//断开连接
    	try {
    		clientSocket.close();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	releaseWakeLock();//////////////////////////////////////////////////////////////////////
    	db.close();
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
                msg.what = CALL_STOP;  
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
    
  //Socket相关线程类
  	class SocketClientThread extends Thread {  
  		public void run(){
  			Log.d(TAG, "TreadStart");
			String line = null;  
			InputStream input;  
  			//连接服务器 并设置连接超时为5秒  
//  			clientSocket = new Socket();  
              try {
  				clientSocket.connect(new InetSocketAddress(ip, port), 5000);/////////////////////////////

  			} catch (IOException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
  		    if(clientSocket.isConnected()&&(!clientSocket.isClosed())){
  		    	Log.d(TAG, "Connected");
  		    	
//  		    	WriteThread writeThread =new WriteThread();
//  		    	writeThread.start();
  		    	
  		    	try {  

  					input = clientSocket.getInputStream();  
  					BufferedReader bff = new BufferedReader(  
  							new InputStreamReader(input));  
  					Log.d(TAG, "Try start");
  					
//  	            	Log.d(TAG, line); 
  					//接收挂断信号，挂断
  					if ((line = bff.readLine()) != null) {            	
  						Log.d(TAG, line); 
  						if(line.equals(INDOOR_HANGUP)){
  							Message msg = new Message();
  							msg.what = CALL_STOP;
  							handler.sendMessage(msg);
  						}
  						
  					}  
  					else Log.d(TAG, "No input");
  					//关闭输入输出流  
  					bff.close();  
  					input.close();  
  					clientSocket.close();  
  	  
  				} catch (IOException e) {  
//  	        		Log.d(TAG, "No input");
  					e.printStackTrace();  
  				}  

  		    }
  		    else {
  		    	Log.d(TAG, "Connecting failed");

  		    }
  		    		   

              

  		}
  	}
  	
	private String query(SQLiteDatabase db,int i )  
	{  
	   
		String pass = null;
	   //查询获得游标  
	   Cursor cursor = db.query   ("tb_user",null,null,null,null,null,null); 
	   int j,k;
	   //获得总的数据项数
	   j=cursor.getCount();
	   k=i%j;
	   if(cursor.moveToFirst()) {  
		   		//移动到指定记录 
		   		cursor.move(k);  
	            //获得用户名  
//	            String username=cursor.getString(cursor.getColumnIndex("name")); 
	            pass=cursor.getString(cursor.getColumnIndex("password")); 
	            //输出用户信息  
	           System.out.println(pass);  
	           final String[] strs = new String[] {pass};
	           //新增
//	           MAClist.setAdapter(new ArrayAdapter<String>(this,
//	                   android.R.layout.simple_list_item_1,strs));
	           
	       }  
	   
	   return pass;
	 }   
	
	public void createDb() {
		db.execSQL("create table tb_user( name varchar(30) primary key,password varchar(30))");
	}
    
}
