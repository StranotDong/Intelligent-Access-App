package com.example.indoor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.example.indoor.Call.TimerThread;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class SocketServerService extends Service{

//	Socket socket = null;
//	public String ip = "192.168.191.2";
	public final int port = 30000;
	ServerSocket server = null;
	ServerAcceptThread serverAcceptThread =new ServerAcceptThread();
//	SocketServerThread socketServerThread =new SocketServerThread();//新的socketThread对象
	private static final String TAG = "SocketServerService";
	
	public boolean stopThread = false;
	
	SQLiteDatabase db;
	
	public static int maxUserNumber = 5;
	//统计接入的远端设备数量以及在一定时间内各心跳包数目
	private String[] remote_macip = new String[maxUserNumber];
	private static int[] remote_macip_counter = new int[maxUserNumber];
	private int remote_macip_sum = 0;
	
	TimerThread timer = new TimerThread();
	private boolean timerDone = false;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		super.onCreate();
		
		Log.d(TAG, "Create");
		serverAcceptThread.start();//循环接收连接
		timer.start();
		
	}
	
	public void onDestory(){
		super.onDestroy();
		stopThread = true;//关闭线程
		
		//断开连接
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		db.close();
		timer.exit = true;
	}
	
	class ServerAcceptThread extends Thread {  
		
		public void run(){
			Log.d(TAG, "ServerAcceptThread start");
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
					Log.d(TAG, "ServerAcceptThread connect");
					Socket serverSocket = server.accept();
					new Thread(new HeartRunable(serverSocket)).start();  
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public class HeartRunable implements Runnable {  
		  
	    Socket serverSocket = null;  
	  
	    public HeartRunable(Socket serverSocket) {  
	        this.serverSocket = serverSocket;  
	    }  
	  
	    @Override  
	    public void run() {  
	        // 向android客户端输出hello worild  
	    	Log.d(TAG, "Thread Run");
	        String line = null;  
	        InputStream input;  
	        OutputStream output;  
	        
	        String remoteMac = null;
            String remoteIp = null;
            
//	        String str = "hello world!";  
	        try {  
	            //向客户端发送信息  
	            output = serverSocket.getOutputStream();  
	            input = serverSocket.getInputStream();  
	            BufferedReader bff = new BufferedReader(  
	                    new InputStreamReader(input));  
	            Log.d(TAG, "Try start");
//	            output.write(str.getBytes("gbk"));  
//	            output.flush();  
	            //半关闭socket    
//	            serverSocket.shutdownOutput();  
	            //获取客户端的信息  
//	            while ((line = bff.readLine()) != null) {  
	            
//	            Log.d(TAG, line); 
	            while ((line = bff.readLine()) != null) {
	                Log.d(TAG, line);  
	                line = line.replaceAll("\n", "");
	                String[] macipArray = line.split(",");
	                remoteMac = macipArray[0];
	                remoteIp = macipArray[1];
	                
	            	int i;	
	            	
	            	if(timerDone == true){
		            	for(i = 0; i < remote_macip_sum; i++){
		            		if(remote_macip_counter[i] == 0) {

		            			db.execSQL("delete from tb_user where name=?",new String[] {remote_macip[i]});
		            		}
		            		remote_macip_counter[i] = 0;
//		            		remote_macip[i] = "";
		            	}
		            	synchronized(this) {  
	                        timerDone = false; 
	                    }  
		            	try{
		            		Cursor cursor = db.query   ("tb_user",null,null,null,null,null,null); 
		            	remote_macip_sum = cursor.getCount();		            	
//		            	remote_macip_sum = 0;
		            	cursor.moveToFirst();
		            	for(i = 0; i < remote_macip_sum; i++){
		            		cursor.move(i);
		            		remote_macip[i]=cursor.getString(cursor.getColumnIndex("name"));
		            	}
		            	}catch(Exception e){
//		            		createDb();
		            	}
	            	}
	            	
	            	for(i = 0; i < remote_macip_sum; i++){
	            		if(remoteMac.equals(remote_macip[i])) break;
	            	}
	            	
	            	if(i < remote_macip_sum) remote_macip_counter[i]++;
	            	else {
	            		if(remote_macip_sum < maxUserNumber)
	            		 Log.d(TAG, line);
	            		remote_macip[i] = remoteMac;
	            		remote_macip_counter[i]++;
	            		remote_macip_sum++;
	            	}

	            	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	            	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	                
	                if(findUser(remoteMac)){
	                	if(findUser2(remoteMac, remoteIp)){
	                		db.execSQL("delete from tb_user where name=?",new String[] {remoteMac});
	                		if (!(remoteMac.equals("") && remoteIp.equals(""))) {
	                			if (addUser(remoteMac, remoteIp)) {
	                				Log.d("TAG", "MAC IP add"+line);
	                			} else {
	                				Log.d("TAG", "MAC IP add failed"+line);
	                			}
	                		} else {
	                			Log.d("TAG", "MAC IP can't be empty"+line);
	                		}
	                	}
					}else
					if (!(remoteMac.equals("") && remoteIp.equals(""))) {
						if (addUser(remoteMac, remoteIp)) {
							Log.d("TAG", "MAC IP add"+line);

						} else {
							Log.d("TAG", "MAC IP add failed"+line);
						}
					} else {
						Log.d("TAG", "MAC IP can't be empty"+line);
					}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                
//	                System.out.println(line);
//	                output.write("ACK\n".getBytes());
//	                output.flush();
	            }  
	            Log.d(TAG, "No input");
	            
//	            db.execSQL("delete from tb_user where name=?",new String[] {remoteMac});
	            //关闭输入输出流  
	            output.close();  
	            bff.close();  
	            input.close();  
	            serverSocket.close();  
	  
	        } catch (IOException e) {  
//	        	Log.d(TAG, "No input");
	            e.printStackTrace();  
	        }  
	  
	    }  
	} 
	
	// 添加用户
		public boolean addUser(String MAC, String IP) {
			String str = "insert into tb_user values(?,?) ";
//			Starting main = new Starting();
			db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString()
					+ "/network.dbs", null);	//如果如果目录下有test.dbs数据库则是连接没有就是创建
//			main.db = db;
			try {
				db.execSQL(str, new String[] { MAC, IP });
				return true;
			} catch (Exception e) {
//				if (!db.tabIsExist("tb_user")) 
//					createDb();
			}
			return false;
		}
		
		public boolean findUser(String MAC) {
			String str = "select * from tb_user where name=?";
			
//			Starting main = new Starting();
			db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString()
					+ "/network.dbs", null);	//如果如果目录下有test.dbs数据库则是连接没有就是创建
//			main.db = db;
			Cursor cursor = db.rawQuery(str, new String []{MAC});
				if(cursor.getCount()<=0){
					return false;
				}else{
					return true;
			}
			
		}
		
		public boolean findUser2(String MAC,String IP) {
			String str = "select * from tb_user where name=?and password<>?";
//			MainActivity main = new MainActivity();
			db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString()
					+ "/network.dbs", null);	//如果如果目录下有test.dbs数据库则是连接没有就是创建
//			main.db = db;
			Cursor cursor = db.rawQuery(str, new String []{MAC,IP});
				if(cursor.getCount()<=0){
					return false;
				}else{
					return true;
			}
			
		}

		public void createDb() {
			db.execSQL("create table tb_user( name varchar(30) primary key,password varchar(30))");
		}
		
		//若一定时间内没有收到某个客户端的心跳包则判定该客户端断开连接
		class TimerThread extends Thread{
	    	public volatile boolean exit = false; 
	    	public volatile int i = 0; 
	    	public final long TIMER = 30000;//定时30s
			public void run(){
				while(!exit){
				try {  
	                Thread.sleep(1);  
	                i++;
	                if(i >= TIMER){
	                	 synchronized(this) {  
	                         timerDone = true; 
	                    }  
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


