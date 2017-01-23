package com.example.indoor;

import com.example.indoor.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Pair extends Activity{
	Button okButton;
	EditText outipEditText;
	
	//默认室外机ip地址
	public static final String OUTIP_ADDRESS = "192.168.191.3";
	public static final String OUTIP_ADDRESS_FILE = "outipAddressFile";
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);  
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pair);
		outipEditText = (EditText) findViewById(R.id.outipAddress);
//		outipEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);  
		
		
		okButton = (Button)this.findViewById(R.id.ok);
		//按下确定button，回到Main活动
		okButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				String outip_address = outipEditText.getText().toString();

				// save new address
				SharedPreferences outip_settings = getSharedPreferences(OUTIP_ADDRESS_FILE, 0);
				SharedPreferences.Editor editor = outip_settings.edit();
				editor.putString("outip_address", outip_address);
				editor.commit();
				
				Intent mainIntent = new Intent(Pair.this, Main.class);
				startActivity(mainIntent);
				stopService(new Intent(Pair.this, ListenService.class));//需要重新连接树莓派的gpio
			}  
		});
		
		// get the remote monitor address, use default if not saved before
		SharedPreferences outip_settings = getSharedPreferences(OUTIP_ADDRESS_FILE, 0);
		String outip_address = outip_settings.getString("outip_address", OUTIP_ADDRESS);
		
		outipEditText.setText(outip_address);
		outipEditText.setSelection(outip_address.length());
		outipEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		outipEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					
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
              }
                
            }
        });
	}
	
	//屏蔽掉Back键
	public boolean onKeyDown(int keyCode ,KeyEvent event){
		
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true ;

		else
			return super.onKeyDown(keyCode, event);
		
	}
}
