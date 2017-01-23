package com.example.lock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class CallListenerService extends Service{
	
	public final int port = 25000;
	ServerSocket server = null;
	ServerAcceptThread serverAcceptThread =new ServerAcceptThread();
	Socket serverSocket = new Socket();
	private static final String TAG = "CallListenerService";
	
	private static final int CALL_START = 1;
	private static final int CALL_STOP_FROM_INDOOR = 2;
	private static final String HANGUP = "HangUp";
	private static final String INDOORHANGUP = "IndoorHangUp";
	
	private IntentFilter intentFilter;
	private IndoorHangupReceiver indoorHangupReceiver;
	
	private String ScreenStatus = "ON";
	
	private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		//接受连接开始Call活动
    		case CALL_START:
    			if(ScreenStatus.equals("ON")){
        			Intent callIntent = new Intent(CallListenerService.this, Call.class);
        			callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			startActivity(callIntent);
        			  	
          	  }
          	//屏幕暗，则开启ScreenLockService服务，由该服务开启ScreenLock活动
          	  else {
/*           		  Intent screenIntent = new Intent(getBaseContext(), ScreenLock.class);///////////////////////////////
          		  screenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          	  	  getApplication().startService(screenIntent);*/
          		  Intent screenLockIntent = new Intent(CallListenerService.this, ScreenLockService.class);
          		  screenLockIntent.putExtra("ScreenStatusJudge", ScreenStatus);
          	  	  startService(screenLockIntent);
          	  	  
          	  }
	    	break;	
    		
			case CALL_STOP_FROM_INDOOR:
/*				Intent mainIntent = new Intent(CallListenerService.this, Main.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(mainIntent);*/
				stopService(new Intent(CallListenerService.this,ScreenLockService.class));//关闭ScreenLockService，回到锁屏界面
				Intent hangupIntent = new Intent(CallListenerService.this, Main.class);
				hangupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(hangupIntent);
				break;   		  

    		}
    	}
    };
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		super.onCreate();
		
		//注册挂断室内机广播
		intentFilter = new IntentFilter();
		intentFilter.addAction("android.com.lock.INDOORHANGUP");
		indoorHangupReceiver = new IndoorHangupReceiver();
		registerReceiver(indoorHangupReceiver, intentFilter);
		
		/*注册广播*/
		IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
		CallListenerService.this.registerReceiver(mScreenOnReceiver, mScreenOnFilter);
		
		/*注册广播*/
		IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
		CallListenerService.this.registerReceiver(mScreenOffReceiver, mScreenOffFilter);
		
		Log.d(TAG, "Create");
		serverAcceptThread.start();//循环接收连接
		
	}
	
	public void onDestory(){
		super.onDestroy();
//		stopThread = true;//关闭线程
		
		//断开连接
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
class ServerAcceptThread extends Thread {  
		
		public void run(){
			try {
				server = new ServerSocket(port);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
/*			try {
				server.setSoTimeout(11000);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			while(!(server.isClosed())){
				try {
					
					serverSocket = server.accept();
					new Thread(new readRunable(serverSocket)).start(); 
					Message msg = new Message();
					msg.what = CALL_START;
					handler.sendMessage(msg);
//					new Thread(new HeartRunable(serverSocket)).start();  
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public class readRunable implements Runnable {  
	  
		Socket serverSocket = null;  
  
		public readRunable(Socket serverSocket) {  
			this.serverSocket = serverSocket;  
		}  
  
		@Override  
		public void run() {  
			// 向android客户端输出hello worild  
			Log.d(TAG, "Thread Run");
			String line = null;  
			InputStream input;  
			OutputStream output;  
//    	    String str = "hello world!";  
			try {  

				input = serverSocket.getInputStream();  
				BufferedReader bff = new BufferedReader(  
						new InputStreamReader(input));  
				Log.d(TAG, "Try start");

				
//            	Log.d(TAG, line); 
				//接收挂断信号，挂断
				if ((line = bff.readLine()) != null) {            	
					Log.d(TAG, line); 
					if(line.equals(HANGUP)){
						Message msg = new Message();
						msg.what = CALL_STOP_FROM_INDOOR;//与收到挂断信号相同
						handler.sendMessage(msg);
					}
					
				}  
				else Log.d(TAG, "No input");
				//关闭输入输出流  

				bff.close();  
				input.close();  
				serverSocket.close();  
  
			} catch (IOException e) {  
//        		Log.d(TAG, "No input");
				e.printStackTrace();  
			}  
  
		}  
	} 

	
	
	class IndoorHangupReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
	            	//获取输出流 
				   OutputStream out = serverSocket.getOutputStream();	  		            
		          //发送信息 以挂断分机
		           out.write((INDOORHANGUP+"\n").getBytes());  
		           out.flush();  

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		}
		
	}
	
	//屏幕变亮的广播,我们要隐藏默认的锁屏界面
		private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context , Intent intent) {
				
	            Log.i(TAG, intent.getAction());

				if(intent.getAction().equals("android.intent.action.SCREEN_ON")){
					Log.i(TAG, "----------------- android.intent.action.SCREEN_ON------");
					ScreenStatus = "ON";
				}
			}
			
		};
		
		//屏幕变暗/变亮的广播 ， 我们要调用KeyguardManager类相应方法去解除屏幕锁定
		private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context , Intent intent) {
				String action = intent.getAction() ;
				
			    Log.i(TAG, intent.toString());
			    
				if(action.equals("android.intent.action.SCREEN_OFF")){
					ScreenStatus = "OFF";
				}
			}
			
		};
	
	

}

