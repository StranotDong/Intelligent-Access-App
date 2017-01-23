package com.example.lock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;

public class Starting extends Activity{
	
	public static final String PASSWORD_FILE = "passwordFile";
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.starting);
		SharedPreferences password_settings = getSharedPreferences(PASSWORD_FILE, 0);
		String password = password_settings.getString("password", "admin");
		SharedPreferences.Editor editor = password_settings.edit();
		editor.putString("password", password);
		editor.commit();
		Log.d("Starting",password);//初始密码为admin
	}	
	
	public void onStart(){
		super.onStart();
		
//		Intent screenServiceIntent = new Intent(Starting.this, ScreenLockService.class);
//		startService(screenServiceIntent);
		
		if(!isWiFiActive()){
			new AlertDialog.Builder(Starting.this)    	             
//          .setTitle("标题")  	     
			.setCancelable(false)
          .setMessage("请打开您的wifi")  	            
          .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮  
          	@Override  
          	public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
          		// TODO Auto-generated method stub  
          		Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
          		startActivity(intent);
          	}  
          })  	  
          .setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加取消按钮  
          	@Override  
          	public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
          		// TODO Auto-generated method stub  
          		dialog.dismiss();
    			Intent mainIntent = new Intent(Starting.this, Main.class);
    			startActivity(mainIntent);
          	}  
          })  	
          .show();
		}
		else {
			new Thread() {
	            @Override
	            public void run() {
	            	try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	
	    			//开始socket连接
	    			Intent socketIntent = new Intent(Starting.this, SocketClientService.class);
	    			startService(socketIntent);
	    			//开始监听call
	    			Intent callListenerIntent = new Intent(Starting.this, CallListenerService.class);
	    			startService(callListenerIntent);
	            	//开启Main活动
	    			Intent mainIntent = new Intent(Starting.this, Main.class);
	    			startActivity(mainIntent);

	            }
	        }.start();

		}
		

	}
	
	public boolean isWiFiActive() {      
        ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);      
        if (connectivity != null) {      
            NetworkInfo[] infos = connectivity.getAllNetworkInfo();      
            if (infos != null) {      
                for(NetworkInfo ni : infos){  
                    if(ni.getTypeName().equals("WIFI") && ni.isConnected()){  
                        return true;  
                    }  
                }  
            }      
        }      
        return false;      
    }
	
}
