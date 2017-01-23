package com.example.lock;

import com.example.lock.R;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class Main extends Activity{
	Button pairButton;
	Button watchButton;
	Button passwordSetButton;
	ImageView image;
	
	private static final String RECONNECT = "Socket Reconnect";
	
	private static final String TAG = "Main";
	public static final String PASSWORD_FILE = "passwordFile";
  	public String password;
	
//	private MyReceiver receiver = null;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.main);
		
		pairButton = (Button)this.findViewById(R.id.pair);
		watchButton = (Button)this.findViewById(R.id.watch);
		image = (ImageView)this.findViewById(R.id.imageView1);
		passwordSetButton = (Button)this.findViewById(R.id.passwordSet);
		

		//按下配对button，打开Pair活动
		pairButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent pairIntent = new Intent(Main.this, Pair.class);
				startActivity(pairIntent);
			}  
		});
		
		//按下监视button，打开Video活动
		watchButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent watchIntent = new Intent(Main.this, Video.class);
				startActivity(watchIntent);
			}  
		});
		
		//按下测试button，打开Call活动
		image.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				Intent testCallIntent = new Intent(Main.this, Call.class);
				startActivity(testCallIntent);
			}  
		});
		
		//按下密码设置button，打开密码设置活动
		passwordSetButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				
				final EditText et = new EditText(Main.this);
				et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    			new AlertDialog.Builder(Main.this)
    			.setTitle("请输入旧密码")
    			.setMessage("初始密码为admin")
    			.setView(et)
    			.setPositiveButton("确定", new DialogInterface.OnClickListener() {   				
    				@Override      				
                	public void onClick(DialogInterface dialog, int which) {
    					String inputPassword = et.getText().toString();
    					SharedPreferences password_settings = getSharedPreferences(PASSWORD_FILE, 0);
    					password = password_settings.getString("password", PASSWORD_FILE);
    					Log.d("PassSet","input"+inputPassword);
						Log.d("PassSet","Real"+password);
    					if(inputPassword.equals(password)){   						
    						dialog.dismiss();
    						Intent passwordSetIntent = new Intent(Main.this, PasswordSet.class);
    						startActivity(passwordSetIntent);
    						
    					}	
    					
    					else{
    						dialog.dismiss();
    						new AlertDialog.Builder(Main.this)    	             
//    		                .setTitle("标题")  	             
    		                .setMessage("密码输入错误，无法修改密码")  	            
    		                .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮  
    		                	@Override  
    		                	public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
    		                		// TODO Auto-generated method stub  
    		                		dialog.dismiss();
    		                	}  
    		                })  	            
    		                .show();
    					}
    				}
    			})
    			.setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加取消按钮  
    				@Override  
    				public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
    					// TODO Auto-generated method stub  
    					dialog.dismiss();
    				}  
    			})  	
    			.show();
				

			}  
		});
		
		//注册广播接收器
//		  receiver = new MyReceiver();
//		  IntentFilter filter=new IntentFilter();
//		  filter.addAction("com.example.lock.SocketClientService");
//		  Main.this.registerReceiver(receiver,filter);
	}
	public void onResume(){
		super.onResume();
//		startService(new Intent(Main.this, ScreenLockService.class));

	        
/*	        //若连接室内机失败，显示对话框重新启动SocketClientService重连
	        Intent reconnectIntent = getIntent();
	        String reconSig = reconnectIntent.getStringExtra("Reconnect");
	        Log.d(TAG, "reconnectIntent: "+reconSig);
	        
	        if(reconSig.equals(RECONNECT)){
				new AlertDialog.Builder(Main.this)    	             
	          .setTitle("无法连接室内机")  	             
	          .setMessage("请检查网络是否正常以及ip地址是否正确输入")  	            
	          .setPositiveButton("重新连接",new DialogInterface.OnClickListener() {//添加确定按钮  
	          	@Override  
	          	public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
	          		// TODO Auto-generated method stub  
	          		
	          		Intent socketIntent = new Intent(Main.this, SocketClientService.class);
	          		stopService(socketIntent);
	          		startService(socketIntent);
	          		dialog.dismiss();
	          	}  
	          })  	  
	          .setNegativeButton("重置ip",new DialogInterface.OnClickListener() {//添加取消按钮  
	          	@Override  
	          	public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
	          		// TODO Auto-generated method stub  
	          		dialog.dismiss();
	    			Intent pairIntent = new Intent(Main.this, Pair.class);
	    			startActivity(pairIntent);
	          	}  
	          })  	
	          .show();
	        }*/
//		processServiceConnect();
	} 
	
	public void onDestory(){
		super.onDestroy();
//		Main.this.unregisterReceiver(receiver);
	}
	
/*	 public class MyReceiver extends BroadcastReceiver {
	     @Override
	     public void onReceive(Context context, Intent intent) {
//	      Bundle bundle = intent.getExtras();
//	      String reconSig = bundle.getString("Reconnect");
	      
	    //若连接室内机失败，显示对话框重新启动SocketClientService重连
	      Log.d(TAG, "reconnectIntent: ");
//	        if(reconSig.equals(RECONNECT)){
					new AlertDialog.Builder(Main.this)    	             
		          .setTitle("无法连接室内机")  	             
		          .setMessage("请检查网络是否正常以及ip地址是否正确输入")  	            
		          .setPositiveButton("重新连接",new DialogInterface.OnClickListener() {//添加确定按钮  
		          	@Override  
		          	public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
		          		// TODO Auto-generated method stub  
		          		
		          		Intent socketIntent = new Intent(Main.this, SocketClientService.class);
		          		stopService(socketIntent);
		          		startService(socketIntent);
		          		dialog.dismiss();
		          	}  
		          })  	  
		          .setNegativeButton("重置ip",new DialogInterface.OnClickListener() {//添加取消按钮  
		          	@Override  
		          	public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
		          		// TODO Auto-generated method stub  
		          		dialog.dismiss();
		    			Intent pairIntent = new Intent(Main.this, Pair.class);
		    			startActivity(pairIntent);
		          	}  
		          })  	
		          .show();
		        }
	         
	     //}
	 }*/
	
	//屏蔽掉Back键
	public boolean onKeyDown(int keyCode ,KeyEvent event){
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true ;

		else
			return super.onKeyDown(keyCode, event);
		
	}
	

}
