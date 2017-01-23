package com.example.lock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class SocketClientService extends Service{

	Socket clientSocket = new Socket();
	public String ip;/////////////////////////////////////////////////////
	public final int port = 30000;
	SocketClientThread socketClientThread =new SocketClientThread();//新的socketThread对象
	private static final String TAG = "SocketClientService"; 
	
	private static final String RECONNECT = "Socket Reconnect";
	private static final String CONNECTED = "Connected";
	
	public String buffer = "";
	
	public static final String INIP_ADDRESS = "192.168.191.2";
	public static final String INIP_ADDRESS_FILE = "inipAddressFile";
	
    private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case 1:
    			Toast.makeText(SocketClientService.this, "与室内连接成功",
    	    			Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
    			
    		}
    	}
    };
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
	    super.onCreate();
      	SharedPreferences inip_settings = getSharedPreferences(INIP_ADDRESS_FILE, 0);
      	ip = inip_settings.getString("inip_address", INIP_ADDRESS);
      	Log.d(TAG, "start"+ip);
	    socketClientThread.start();//启动socket线程

	}
	
	public void onDestory(){
		super.onDestroy();
		
		
		//断开连接
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Socket相关线程类
	class SocketClientThread extends Thread {  
		public void run(){
			Log.d(TAG, "TreadStart");
			//连接服务器 并设置连接超时为15秒  
//			clientSocket = new Socket();  
            try {
				clientSocket.connect(new InetSocketAddress(ip, port), 15000);/////////////////////////////

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    if(clientSocket.isConnected()&&(!clientSocket.isClosed())){
//            if(clientSocket.isConnected()){
		    	Log.d(TAG, "Connected");
		    	 Message msg = new Message();  
	                msg.what = 1;  
	                handler.sendMessage(msg); 
		    	WriteThread writeThread =new WriteThread();
		    	writeThread.start();
		    	

		    }
		    else {
		    	Log.d(TAG, "Connecting failed");
		    	//在Main中显示重连对话框
		    	Intent reconnectIntent = new Intent("com.example.lock.RECONNECTING");
//		    	reconnectIntent.putExtra("Reconnect", RECONNECT);
//		    	reconnectIntent.setAction("com.example.lock.SocketClientService");
		    	sendBroadcast(reconnectIntent);
		    }
		    
		    		   

            

		}
	}
	
	class WriteThread extends Thread{
		public void run(){
			String MAC;
			String local_ip;
			MAC = getLocalMacAddress();
			Log.d(TAG, MAC);
			if(clientSocket.isConnected()&&(!clientSocket.isClosed())){
		    	Log.d(TAG, "Connected");
            	
		    
	            try {
	            	//输入流
	            	BufferedReader bff = new BufferedReader(new InputStreamReader(  
	    	            	clientSocket.getInputStream()));
	            	String line = null;
	            	//获取输出流 
	            	OutputStream out = clientSocket.getOutputStream();
		          //向服务器发送信息  
		            while(clientSocket.isConnected()&&(!clientSocket.isClosed())&&(isWiFiActive())){
		            	local_ip = getLocalIpAddress();
		            	Log.d(TAG, local_ip);

		            	out.write((MAC+","+local_ip+"\n").getBytes());  
		            	out.flush();
		            	
/*		            	try {
		            		WriteThread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//////////////////////////////////////
		            	
		            	if ((line = bff.readLine()) != null) {  
//			                buffer = line + buffer;  
		            		Log.d(TAG, line);
			            }
		            	else Log.d(TAG, "no connect");*/
		            	
		            	try {
		            		WriteThread.sleep(15000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//////////////////////////////////////
		            }
		            //跳出循环后关闭输出流
		            out.close();
//	                bundle.putString("msg", buffer.toString());  
//	               msg.setData(bundle);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
	            //关闭各种输入输出流  
	            

		    }

		}
	}
	
	//获取本机Mac
	public String getLocalMacAddress() {  
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);  
        WifiInfo info = wifi.getConnectionInfo();  
        return info.getMacAddress();  
    }  
	
	//获取本机ip
/*	public static String getLocalIpAddress(){ 
		InetAddress inetAddress = null;
        try{ 
             for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
                 NetworkInterface intf = en.nextElement();   
                    for (Enumeration<InetAddress> enumIpAddr = intf   
                            .getInetAddresses(); enumIpAddr.hasMoreElements();) {   
                        inetAddress = enumIpAddr.nextElement(); 
                        Log.d("Local_ip", inetAddress.getHostAddress().toString());
                        if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {   
                        	
//                            return inetAddress.getHostAddress().toString();   
                        }   
                    }   
             } 
             return inetAddress.getHostAddress().toString(); 
        }catch (SocketException e) { 
            // TODO: handle exception 
        	Log.e(TAG, e.toString()); 
        } 
         
        return null;  
    }*/
	public static String getLocalIpAddress(){ 
        
        try{ 
             for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
                 NetworkInterface intf = en.nextElement();   
                    for (Enumeration<InetAddress> enumIpAddr = intf   
                            .getInetAddresses(); enumIpAddr.hasMoreElements();) {   
                        InetAddress inetAddress = enumIpAddr.nextElement();   
                        if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {   
                             
//                            String[] Array = inetAddress.getHostAddress().toString().split(".");
//                            Log.d(TAG, Array[0]);
                            if(!inetAddress.getHostAddress().toString().equals("10.0.2.15")) return inetAddress.getHostAddress().toString();//照顾到小米手机！！！！！！！！
                        }   
                    }   
             } 
        }catch (SocketException e) { 
            // TODO: handle exception 
        	Log.e(TAG, e.toString()); 
        } 
         
        return null;
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
