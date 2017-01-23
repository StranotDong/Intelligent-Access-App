package com.example.indoor;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

import android.app.Activity;  
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;  
import android.os.Bundle;  
import android.view.View;  
import android.widget.Button;  
import android.widget.EditText;  
import android.widget.Toast;
import android.widget.VideoView;  

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
  
public class Video_open extends Activity {  
/** Called when the activity is first created. */  
  
	Button openButton;  
	Button refuseButton;  
	VideoView videoView;
	
	private static String clientId = "PiCommanderAndroid";
	private static MqttClient mqttClient;
	
    public static final String TOPIC = "PiCommander";
    public static final String TOPIC_STATUS = "PiCommanderStatus";
    public static final int QOS = 2;
    public static final int TIMEOUT = 3;
    public final String gpio_lock = "GPIO 10";/////////////////////////////////////////////////////////////
    public final String gpio_outdoor = "GPIO 11";
    public final String port = "1883";
//    public final String ip = "192.168.191.3";/////////////////////////////////////////////////////////////////////////
    public String ip;
    
  //默认室外机ip地址
  	public static final String OUTIP_ADDRESS = "192.168.191.3";
  	public static final String OUTIP_ADDRESS_FILE = "outipAddressFile";
    
    private static final String LOG_TAG = "Video";
    
    public static final int UPDATE_CON_THREAD_SUC = 0;
    public static final int UPDATE_CON_THREAD_FAIL = 1;
    public static final int UPDATE_DIA_THREAD = 2;
    
    public boolean openClick = false;
    
    
    private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case UPDATE_CON_THREAD_SUC:
    			Toast.makeText(Video_open.this, "连接成功",////////////////////////////////////////////////////////////
    	                  Toast.LENGTH_SHORT).show();
    			break;
    		case UPDATE_CON_THREAD_FAIL:
    			Toast.makeText(Video_open.this, "当前网络不可用；请检查您是否开启wifi或ip地址是否输入正确",
    	    			Toast.LENGTH_LONG).show();
    			break;
    		case UPDATE_DIA_THREAD:
    			new AlertDialog.Builder(Video_open.this)    	             
//                .setTitle("标题")  
    			.setCancelable(false)
                .setMessage("单元楼门打开成功")  	            
                .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮  
                	@Override  
                	public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
                		// TODO Auto-generated method stub  
                		
                		stopService(new Intent(Video_open.this,ScreenLockService.class));//关闭ScreenLockService，回到锁屏界面  
                		
                		Intent backIntent = new Intent(Video_open.this, Main.class);
            			startActivity(backIntent); 
                	}  
                })  	            
                .show();
    			break;
    			
    		}
    	}
    };
    
	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		//从SharedPreference中获得室外机ip
		SharedPreferences outip_settings = getSharedPreferences(OUTIP_ADDRESS_FILE, 0);
		ip = outip_settings.getString("outip_address", OUTIP_ADDRESS);
		// 全屏运行
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.video_open); 

		// 设置SurfaceView地址参数
		VideoSurfaceView r = (VideoSurfaceView) findViewById(R.id.SurfaceViewVideo);
		if (r == null) {
			Log.e("r", "r == null");
		}
		r.setMontiorAddress("http://"+ip+":8090/?action=stream");
		Log.i("address", "setMontiorAddress complete");
		
		new Thread(new Runnable(){
			@Override
			public void run(){
				processConnect(ip, port);//连接室外机
				if (mqttClient.isConnected()){
					Message message = new Message();
					message.what = UPDATE_CON_THREAD_SUC;
					handler.sendMessage(message);
				}
				else {
					Message message = new Message();
					message.what = UPDATE_CON_THREAD_FAIL;
					handler.sendMessage(message);
				}
			}
		}).start();
		
//		PlayRtspStream("http://forum.ea3w.com/coll_ea3w/attach/2008_10/12237832415.3gp");
  
		//rtspUrl = (EditText)this.findViewById(R.id.url);  
		openButton = (Button)this.findViewById(R.id.open);  
		openButton.setOnClickListener(new Button.OnClickListener(){  
			
			public void onClick(View v) {  		
				
				openClick = true;
				
				String content = gpio_lock + "," + "OFF";
				
				MqttMessage message = new MqttMessage(content.getBytes());
	            message.setQos(QOS);

	            try {
	                mqttClient.publish(TOPIC, message);
	            }
	            catch (MqttException me) {
	                Log.d(LOG_TAG, me.toString());
	            }
	            
	            new Thread(){
	            	 public void run() {
	            		 Log.d(LOG_TAG, "close door thread start");
	            		 try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            		 Log.d(LOG_TAG, "close door thread run");
	            		 String content = gpio_lock + "," + "ON";						
	         			
	         			MqttMessage message = new MqttMessage(content.getBytes());
	                     message.setQos(QOS);

	                     try {
	                    	 
	                         mqttClient.publish(TOPIC, message);
	                     }
	                     catch (MqttException me) {
	                         Log.d(LOG_TAG, me.toString());
	                     }
	            	 }
	            }.start();
	            
			} 
	});  
  
		//拒绝按键refuse，返回到主界面Main
		refuseButton = (Button)this.findViewById(R.id.refuse);  
		refuseButton.setOnClickListener(new Button.OnClickListener(){  
		public void onClick(View v) {  
//			finish();
			
			stopService(new Intent(Video_open.this,ScreenLockService.class));//关闭ScreenLockService，回到锁屏界面
			
			Intent backIntent = new Intent(Video_open.this, Main.class);
			startActivity(backIntent);
		}  
	});  
//	videoView = (VideoView)this.findViewById(R.id.videoView);  
  
}  
	
  
	//play rtsp stream  
/*	private void PlayRtspStream(String rtspUrl){  
		videoView.setVideoURI(Uri.parse(rtspUrl));  
		videoView.requestFocus();  
		videoView.start();  
	}  */

//连接室外机
	private void processConnect(String brokerIp, String brokerPort) {
      String broker = "tcp://" + brokerIp + ":" + brokerPort;

      try {
          clientId = clientId + System.currentTimeMillis();

          MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
          mqttConnectOptions.setCleanSession(true);
          mqttConnectOptions.setConnectionTimeout(TIMEOUT);

          mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
          mqttClient.setCallback(new MqttCallbackHandler());////////////////////////////////////////////////
          mqttClient.connect(mqttConnectOptions);
          mqttClient.subscribe(TOPIC_STATUS);

//          controllerCommandAdapter.refresh();
//          listenerCommandAdapter.refresh();

//          main.setBackgroundResource(R.drawable.background_enable);
//          connect_imageview.setImageResource(R.drawable.mq_connected);

//          Toast.makeText(this, "Connected",
//                  Toast.LENGTH_LONG).show();
      }
      catch (MqttException me) {
//      	Toast.makeText(this, "Connecting failed",
//      			Toast.LENGTH_LONG).show();
      }
	}
	
	//活动停止。断开连接
/*	public void onStop() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            }
            catch (MqttException me) {
                Log.d(LOG_TAG, me.toString());
            }
        }

        super.onStop();
    }*/
	public void onDestroy() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            }
            catch (MqttException me) {
                Log.d(LOG_TAG, me.toString());
            }
        }

        super.onDestroy();
	}
	
	
	//门打开后，弹出确认对话框
	private class MqttCallbackHandler implements MqttCallback {

        @Override
        public void connectionLost(Throwable throwable) {
            Log.d(LOG_TAG, throwable.toString());
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            final String message = new String(mqttMessage.getPayload());
            final String[] messageArray = message.split(",");

            if (!(messageArray.length == 2)) {
                Log.d(LOG_TAG, "Message context malformed");
                return;
            }
            if(message.equals(gpio_lock + "," + "OFF")&&(openClick == true)){
            	new Thread(new Runnable() {
            		@Override
            		public void run() {
            			Message message = new Message();
            			message.what = UPDATE_DIA_THREAD;
            			handler.sendMessage(message);
            		}
            
            	}).start();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            //
        }

    }
	
	//判断服务是否运行/////////////////////////////////////////////////////////////////////////////////
	public static boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        		mContext.getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList 
        	= activityManager.getRunningServices(30);
       if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
	
	//屏蔽掉Back键
	public boolean onKeyDown(int keyCode ,KeyEvent event){
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true ;

		else
			return super.onKeyDown(keyCode, event);
		
	}
}  

