package com.example.lock;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

public class ReconReceiver extends BroadcastReceiver {
	
	private static final String TAG = "ReconReciever";
	
    @Override
    public void onReceive(final Context context, Intent intent) {
//     Bundle bundle = intent.getExtras();
//     String reconSig = bundle.getString("Reconnect");
     
   //若连接室内机失败，显示对话框重新启动SocketClientService重连
     Log.d(TAG, "reconnectIntent: ");
//       if(reconSig.equals(RECONNECT)){
     AlertDialog.Builder build = new AlertDialog.Builder(context);    	             
     build.setTitle("无法连接室内机");  	             
     build.setMessage("请检查网络是否正常以及ip地址是否正确输入");  	            
     build.setPositiveButton("重新连接",new DialogInterface.OnClickListener() {//添加确定按钮  
	          	@Override  
	          	public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
	          		// TODO Auto-generated method stub  
	          		
	          		Intent socketIntent = new Intent(context, SocketClientService.class);
	          		socketIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	          		context.stopService(socketIntent);
	          		context.startService(socketIntent);
	          		dialog.dismiss();
	          	}  
	          });  	  
     build.setNegativeButton("重置ip",new DialogInterface.OnClickListener() {//添加取消按钮  
	          	@Override  
	          	public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
	          		// TODO Auto-generated method stub  
	          		dialog.dismiss();
	    			Intent pairIntent = new Intent(context, Pair.class);
	    			pairIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    			context.startActivity(pairIntent);
	          	}  
	          });  	
     AlertDialog dialog = build.create();
     dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	          

     dialog.show();
	        }
        
    //}
}
