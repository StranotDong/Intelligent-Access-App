package com.example.lock;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class ScreenLockService extends Service {

	private static String TAG = "ScreenLockService";
	private Intent zdLockIntent = null ;
	
	public String ScreenStatus; 
	
	private KeyguardManager mKeyguardManager = null ;
	private KeyguardManager.KeyguardLock mKeyguardLock = null ;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate(){
		super.onCreate();
		Log.d(TAG, "Create");
		
		
		
		
/*		zdLockIntent = new Intent(ScreenLockService.this , ScreenLock.class);
		zdLockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
		
		//注册广播
		IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
		ScreenLockService.this.registerReceiver(mScreenOnReceiver, mScreenOnFilter);
		
		//注册广播
		IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
		ScreenLockService.this.registerReceiver(mScreenOffReceiver, mScreenOffFilter);*/
	}

	public int onStartCommand(Intent intent , int flags , int startId){
		
		ScreenStatus = intent.getStringExtra("ScreenStatusJudge");
		Log.d(TAG, ScreenStatus);
		

		
		zdLockIntent = new Intent(ScreenLockService.this , Call.class);
		zdLockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		if(ScreenStatus.equals("OFF")){
			
			Context context = (Context) getApplicationContext();
			mKeyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
			mKeyguardLock = mKeyguardManager.newKeyguardLock("zdLock 1"); 
			mKeyguardLock.disableKeyguard();
			startActivity(zdLockIntent);
//			stopSelf();
		}
		
		return Service.START_STICKY;
		
	}
	
	public void onDestroy(){
		super.onDestroy();
//		ScreenLockService.this.unregisterReceiver(mScreenOnReceiver);
		Context context = (Context) getApplicationContext();
		mKeyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
		mKeyguardLock.reenableKeyguard();
		Log.d(TAG, "Destroy");
//		ScreenLockService.this.unregisterReceiver(mScreenOffReceiver);
		//在此重新启动
//		startService(new Intent(ScreenLockService.this, ScreenLockService.class));
	}
	
//	private KeyguardManager mKeyguardManager = null ;
//	private KeyguardManager.KeyguardLock mKeyguardLock = null ;
	//屏幕变亮的广播,我们要隐藏默认的锁屏界面
	private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context , Intent intent) {
			
            Log.i(TAG, intent.getAction());

			if(intent.getAction().equals("android.intent.action.SCREEN_ON")){
				Log.i(TAG, "----------------- android.intent.action.SCREEN_ON------");
//				mKeyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
//				mKeyguardLock = mKeyguardManager.newKeyguardLock("zdLock 1"); 
//				mKeyguardLock.disableKeyguard();
			}
		}
		
	};
	
/*	//屏幕变暗/变亮的广播 ， 我们要调用KeyguardManager类相应方法去解除屏幕锁定
	private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context , Intent intent) {
			String action = intent.getAction() ;
			
		    Log.i(TAG, intent.toString());
		    
			if(action.equals("android.intent.action.SCREEN_OFF")
					|| action.equals("android.intent.action.SCREEN_ON") ){
				mKeyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
				mKeyguardLock = mKeyguardManager.newKeyguardLock("zdLock 1"); 
				mKeyguardLock.disableKeyguard();
				startActivity(zdLockIntent);
				stopSelf();
			}
		}
		
	};*/
	
/*	//屏幕变暗/变亮的广播 ， 我们要调用KeyguardManager类相应方法去解除屏幕锁定
	private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context , Intent intent) {
//			String action = intent.getAction();
			
//		    Log.i(TAG, intent.toString());
		    
			if(ScreenStatus.equals("OFF")){
				mKeyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
				mKeyguardLock = mKeyguardManager.newKeyguardLock("zdLock 1"); 
				mKeyguardLock.disableKeyguard();
				startActivity(zdLockIntent);
//				stopSelf();
			}
		}
		
	};*/
	
}

