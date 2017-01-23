package com.example.indoor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;

public class Starting extends Activity{
	
	public static SQLiteDatabase db;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); 
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);	
		setContentView(R.layout.starting);
		
		db = SQLiteDatabase.openOrCreateDatabase(Starting.this.getFilesDir().toString()
				+ "/network.dbs", null);//建立数据库，更新远端传来的mac和ip
		try{
			createDb();
		}catch(Exception e){
			Log.d("Start", "Db table exists");
		}
		
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
	    			Intent mainIntent = new Intent(Starting.this, Main.class);
	    			startActivity(mainIntent);
	            }
	        }.start();

		}
				
		
		//接收socket连接
		Intent socketIntent = new Intent(Starting.this, SocketServerService.class);
		startService(socketIntent);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		db.close();
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
	
	public void createDb() {
		db.execSQL("create table tb_user( name varchar(30) primary key,password varchar(30))");
	}
	
}
