package com.example.lock;

import com.example.lock.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Pair extends Activity{
	Button okButton;
	EditText outipEditText;
	EditText inipEditText;
	
	//默认室外机ip地址
	public static final String OUTIP_ADDRESS = "192.168.191.3";
	public static final String OUTIP_ADDRESS_FILE = "outipAddressFile";
	public static final String INIP_ADDRESS = "192.168.191.2";
	public static final String INIP_ADDRESS_FILE = "inipAddressFile";
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.pair);
		outipEditText = (EditText) findViewById(R.id.outipAddress);
		inipEditText = (EditText) findViewById(R.id.inipAddress);
//		outipEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);  
		outipEditText.requestFocus();
		inipEditText.clearFocus(); 
		okButton = (Button)this.findViewById(R.id.ok);
		//按下确定button，回到Main活动
		okButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				String outip_address = outipEditText.getText().toString();
				String inip_address = inipEditText.getText().toString();

				// save new outdoor address
				SharedPreferences outip_settings = getSharedPreferences(OUTIP_ADDRESS_FILE, 0);
				SharedPreferences.Editor editor = outip_settings.edit();
				editor.putString("outip_address", outip_address);
				editor.commit();
				
				// save new indoor address	
				SharedPreferences inip_settings = getSharedPreferences(INIP_ADDRESS_FILE, 0);
				SharedPreferences.Editor ineditor = inip_settings.edit();
				ineditor.putString("inip_address", inip_address);
				ineditor.commit();
				
				//重新连接室内机
				Intent socketIntent = new Intent(Pair.this, SocketClientService.class);
				stopService(socketIntent);
          		startService(socketIntent);
				
				Intent mainIntent = new Intent(Pair.this, Main.class);
				startActivity(mainIntent);
//				stopService(new Intent(Pair.this, ListenService.class));//需要重新连接树莓派的gpio
				
				
			}  
		});
		
		// get the remote monitor address, use default if not saved before
		SharedPreferences outip_settings = getSharedPreferences(OUTIP_ADDRESS_FILE, 0);
		String outip_address = outip_settings.getString("outip_address", OUTIP_ADDRESS);
		
		outipEditText.setText(outip_address);
		outipEditText.setSelection(outip_address.length());
		outipEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				outipEditText.requestFocus();
				inipEditText.clearFocus(); 
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					outipEditText.clearFocus();
					inipEditText.requestFocus(); 
					String outip_address = outipEditText.getText().toString();

					// save new address
					SharedPreferences outip_settings = getSharedPreferences(OUTIP_ADDRESS_FILE, 0);
					SharedPreferences.Editor editor = outip_settings.edit();
					editor.putString("outip_address", outip_address);
					editor.commit();
				}
				return false;
			}
		});
		outipEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
            	
              if(hasFocus){
            	//获得焦点时文字的颜色
            	  outipEditText.setTextColor(getResources().getColor(R.color.focus));//#FFFFFF,white
              }else{
            	  //失去焦点时文字的颜色
            	  outipEditText.setTextColor(getResources().getColor(R.color.no_focus));//#555555
					String outip_address = outipEditText.getText().toString();

					// save new address
					SharedPreferences outip_settings = getSharedPreferences(OUTIP_ADDRESS_FILE, 0);
					SharedPreferences.Editor editor = outip_settings.edit();
					editor.putString("outip_address", outip_address);
					editor.commit();
              }
                
            }
        });
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//室内机匹配
		SharedPreferences inip_settings = getSharedPreferences(INIP_ADDRESS_FILE, 0);
		String inip_address = inip_settings.getString("inip_address", INIP_ADDRESS);
	
		inipEditText.setText(inip_address);
		inipEditText.setSelection(inip_address.length());
		inipEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE) {
				
					String inip_address = inipEditText.getText().toString();

					// save new address
					SharedPreferences inip_settings = getSharedPreferences(INIP_ADDRESS_FILE, 0);
					SharedPreferences.Editor editor = inip_settings.edit();
					editor.putString("inip_address", inip_address);
					editor.commit();
				}
				return false;
			}
		});
		inipEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
        
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
        	
				if(hasFocus){
					//获得焦点时文字的颜色
					inipEditText.setTextColor(getResources().getColor(R.color.focus));//#FFFFFF,white
				}else{
					//失去焦点时文字的颜色
					inipEditText.setTextColor(getResources().getColor(R.color.no_focus));//#555555
				}
            
			}
		});
	}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	//屏蔽掉Back键
	public boolean onKeyDown(int keyCode ,KeyEvent event){
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true ;

		else
			return super.onKeyDown(keyCode, event);
		
	}
}  

