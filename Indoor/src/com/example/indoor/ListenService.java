package com.example.indoor;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class ListenService extends Service {

    private MqttClient mqttClient;
    public static final String TOPIC = "PiCommander";
    public static final String TOPIC_STATUS = "PiCommanderStatus";
    public static final int QOS = 2;
    public static final int TIMEOUT = 3;
    public final String gpio_lock = "GPIO 10";/////////////////////////////////////////////////////////////
    public final String gpio_outdoor = "GPIO 4";
    public final String gpio_longconnect = "GPIO 21";
    public final String port = "1883";
//    public final String ip = "192.168.191.3";/////////////////////////////////////////////////////////////////////////
    public String ip;
  //默认室外机ip地址
  	public static final String OUTIP_ADDRESS = "192.168.191.3";
  	public static final String OUTIP_ADDRESS_FILE = "outipAddressFile";
    
    private static final String LOG_TAG = "Listen";
    
    private String ScreenStatus = "ON";
    
    longMqttThread longmqttThread = new longMqttThread(); 
    private int longConFlag = 0;
    
  //Thread与Handler相关
    public static final int UPDATE_CON_THREAD_SUC = 0;
    public static final int UPDATE_CON_THREAD_FAIL = 1;
    
    private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		//显示连接
    		case UPDATE_CON_THREAD_SUC:
    			Toast.makeText(ListenService.this, "连接成功",///////////////////////////////////////////////////////
    	                  Toast.LENGTH_SHORT).show();
    			break;
    		//显示连接失败
    		case UPDATE_CON_THREAD_FAIL:
    			Toast.makeText(ListenService.this, "匹配失败；请检查室外机ip是否输入正确",
    	    			Toast.LENGTH_LONG).show();
    			break;
    		    			
    		}
    	}
    };

    @Override
    public void onCreate() {
        super.onCreate();
        
        /*注册广播*/
		IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
		ListenService.this.registerReceiver(mScreenOnReceiver, mScreenOnFilter);
		
		/*注册广播*/
		IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
		ListenService.this.registerReceiver(mScreenOffReceiver, mScreenOffFilter);
		
		

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
    	Log.d(LOG_TAG, "onStartCommand");
        //从SharedPreference中获得室外机ip
      	SharedPreferences outip_settings = getSharedPreferences(OUTIP_ADDRESS_FILE, 0);
      	ip = outip_settings.getString("outip_address", OUTIP_ADDRESS);
      	Log.d(LOG_TAG, "start"+ip);
    	
        if (checkNetwork(this)) {
        	new Thread(new Runnable(){
    			@Override
    			public void run(){
    				connect();//连接室外机
    				Log.d(LOG_TAG, "connect() done");
    				if (mqttClient.isConnected()){
    					Message mes = new Message();
    					mes.what = UPDATE_CON_THREAD_SUC;
    					handler.sendMessage(mes);
    					
    					Log.d(LOG_TAG, Integer.toString(longConFlag));
    					if( longConFlag == 0){
    						longmqttThread.start();///////////////////////////启动长连接服务
    					}
    					
    					String content = gpio_outdoor + "," + "LISTEN";//发布报文使得可以对其进行监听						
    					
    					MqttMessage message = new MqttMessage(content.getBytes());
    		            message.setQos(QOS);

    		            try {
    		                mqttClient.publish(TOPIC, message);
    		            }
    		            catch (MqttException me) {
    		                Log.d(LOG_TAG, me.toString());
    		            }
    					
    				}
    				else {
    					Message message = new Message();
    					message.what = UPDATE_CON_THREAD_FAIL;
    					handler.sendMessage(message);
    				}
    			}
    		}).start();

            
        }
        else {
        	Toast.makeText(ListenService.this, "请打开您的wifi",
	    			Toast.LENGTH_LONG).show();
            Log.d("ListenService", "onStartCommand: Connection required.");
        }
        
//    	if (checkNetwork(this)&&mqttClient.isConnected()){
//    		longmqttThread.start();///////////////////////////启动长连接服务
//    	}

        return Service.START_STICKY;
    }

    public void connect() {
        try {
            if (mqttClient == null || !mqttClient.isConnected()) {
//                String broker = "tcp://" + TurtleUtil.getBrokerIP(this) +
//                        ":" + TurtleUtil.getBrokerPort(this);
            	String broker = "tcp://" + ip + ":" + port;
            	Log.d(LOG_TAG, ip);
                       
                String clientId = "PiCommanderAndroidService" +
                        System.currentTimeMillis();
                mqttClient = new MqttClient(broker, clientId,
                        new MemoryPersistence());
                mqttClient.setCallback(new MqttListenServiceHandler(this));

                MqttConnectOptions mqttConnectOptions =
                        new MqttConnectOptions();
                mqttConnectOptions.setCleanSession(true);
                mqttConnectOptions.setConnectionTimeout(TIMEOUT);

                mqttClient.connect(mqttConnectOptions);
                mqttClient.subscribe(TOPIC_STATUS);
                
//                Toast.makeText(this, "Connected",
//                        Toast.LENGTH_LONG).show();
                
            }
        }
        catch (MqttException me) {
            Log.d("ListenService", "connect: " + me.toString());
//            Toast.makeText(this, "Connecting failed",
//        			Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onDestroy() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            }
            catch (MqttException me) {
                Log.d("ListenService", me.toString());
            }
        }
                
        ListenService.this.unregisterReceiver(mScreenOnReceiver);
		ListenService.this.unregisterReceiver(mScreenOffReceiver);
		
//		longConFlag = 0;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ListenServiceBinder();
    }

    public class ListenServiceBinder extends Binder {
        public ListenService getListenService() {
            return ListenService.this;
        }
    }

    private class MqttListenServiceHandler implements MqttCallback {

        private Context context;

        public MqttListenServiceHandler(Context context) {
            this.context = context;
        }

        @Override
        public void connectionLost(Throwable throwable) {

        }

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage)
                throws Exception {
            String message = new String(mqttMessage.getPayload());

            final String[] messageArray = message.split(",");

            if (!(messageArray.length == 2)) {
                Log.d("ListenService", "Message context malformed");
                return;
            }

//            boolean isExpander = (messageArray.length == 4);

              String gpioName = messageArray[0];
//            String address = null;
//            String type = null;
              Log.d(LOG_TAG, message);
              
              if(message.equals(gpio_outdoor + "," + "OFF")) {
            	  //屏幕亮，则开启Call活动
            	  Log.d(LOG_TAG, message);
            	  if(ScreenStatus.equals("ON")){
            		  Intent callIntent = new Intent(getBaseContext(), Call.class);
            		  callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	  	  getApplication().startActivity(callIntent);
            	  }
            	//屏幕暗，则开启ScreenLockService服务，由该服务开启ScreenLock活动
            	  else {
/*           		  Intent screenIntent = new Intent(getBaseContext(), ScreenLock.class);///////////////////////////////
            		  screenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	  	  getApplication().startService(screenIntent);*/
            		  Intent screenLockIntent = new Intent(ListenService.this, ScreenLockService.class);
            		  screenLockIntent.putExtra("ScreenStatusJudge", ScreenStatus);
            	  	  startService(screenLockIntent);
            	  	  
            	  }
              } 

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }
    }
    
     
    public static boolean checkNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            return false;
        }

        return true;
    }
    

	//屏幕变亮的广播,我们要隐藏默认的锁屏界面
	private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context , Intent intent) {
			
            Log.i(LOG_TAG, intent.getAction());

			if(intent.getAction().equals("android.intent.action.SCREEN_ON")){
				Log.i(LOG_TAG, "----------------- android.intent.action.SCREEN_ON------");
				ScreenStatus = "ON";
			}
		}
		
	};
	
	//屏幕变暗/变亮的广播 ， 我们要调用KeyguardManager类相应方法去解除屏幕锁定
	private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context , Intent intent) {
			String action = intent.getAction() ;
			
		    Log.i(LOG_TAG, intent.toString());
		    
			if(action.equals("android.intent.action.SCREEN_OFF")){
				ScreenStatus = "OFF";
			}
		}
		
	};
	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	//不断向树莓派发送心跳包进行长连接
	class longMqttThread extends Thread{
		public void run(){
			while(mqttClient != null&&mqttClient.isConnected()){
				longConFlag = 1;
				String content = gpio_longconnect + "," + "ON";						
				
				MqttMessage message = new MqttMessage(content.getBytes());
	            message.setQos(QOS);

	            try {
	                mqttClient.publish(TOPIC, message);
	            }
	            catch (MqttException me) {
	                Log.d(LOG_TAG, me.toString());
	            }
	            
	            try {
	            	longMqttThread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
			}
			Log.d(LOG_TAG, "long connect thread stop");
//			longConFlag = 0;
			Log.d(LOG_TAG, "Thread "+Integer.toString(longConFlag));
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
}

