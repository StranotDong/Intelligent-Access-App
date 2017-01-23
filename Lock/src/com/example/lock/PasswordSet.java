package com.example.lock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class PasswordSet extends Activity{
	
	Button okButton;
	EditText passwordEditText;
	EditText repasswordEditText;
	public static final String PASSWORD_FILE = "passwordFile";
	public static final String REPASSWORD_FILE = "repasswordFile";
//	public String password;
//	public String repassword;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.password);
		passwordEditText = (EditText) findViewById(R.id.password);
		repasswordEditText = (EditText) findViewById(R.id.repassword);
		passwordEditText.requestFocus();
		
		repasswordEditText.clearFocus(); 
		
		okButton = (Button)this.findViewById(R.id.password_ok);
		//按下确定button，回到Main活动
		okButton.setOnClickListener(new Button.OnClickListener(){  
			public void onClick(View v) {  
				
				String password = passwordEditText.getText().toString();
				String repassword = repasswordEditText.getText().toString();
				if(password.equals(repassword)){
					SharedPreferences password_settings = getSharedPreferences(PASSWORD_FILE, 0);
					SharedPreferences.Editor editor = password_settings.edit();
					editor.putString("password", password);
					editor.commit();
				
				
					SharedPreferences repassword_settings = getSharedPreferences(REPASSWORD_FILE, 0);
					SharedPreferences.Editor reeditor = repassword_settings.edit();
					reeditor.putString("repassword", repassword);
					reeditor.commit();
				
					Intent mainIntent = new Intent(PasswordSet.this, Main.class);				
					startActivity(mainIntent);
				}
				else{
					new AlertDialog.Builder(PasswordSet.this)    	             
//			          .setTitle("您输入的的密码不相同")  	             
			          .setMessage("您输入的的密码不相同，请重新输入")  	            
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
		});
		
		SharedPreferences password_settings = getSharedPreferences(PASSWORD_FILE, 0);
		String password = password_settings.getString("password", "admin");
		passwordEditText.setText(password);
		passwordEditText.setSelection(password.length());
		//保存密码
		passwordEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				passwordEditText.requestFocus();
				repasswordEditText.clearFocus();
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					passwordEditText.clearFocus();
					repasswordEditText.requestFocus();
					String password = passwordEditText.getText().toString();
					SharedPreferences password_settings = getSharedPreferences(PASSWORD_FILE, 0);
					SharedPreferences.Editor editor = password_settings.edit();
					editor.putString("password", password);
					editor.commit();
					Log.d("PassSet", "real "+password);
				}
				return false;
			}
		});
		passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
            	
              if(hasFocus){
            	//获得焦点时文字的颜色
            	  passwordEditText.setTextColor(getResources().getColor(R.color.focus));//#FFFFFF,white

              }else{
            	  //失去焦点时文字的颜色
            	  passwordEditText.setTextColor(getResources().getColor(R.color.no_focus));//#555555
					String password = passwordEditText.getText().toString();
					SharedPreferences password_settings = getSharedPreferences(PASSWORD_FILE, 0);
					SharedPreferences.Editor editor = password_settings.edit();
					editor.putString("password", password);
					editor.commit();
              }
                
            }
        });
		
		SharedPreferences repassword_settings = getSharedPreferences(REPASSWORD_FILE, 0);
		String repassword = repassword_settings.getString("repassword", "admin");
		repasswordEditText.setText(repassword);
		passwordEditText.setSelection(repassword.length());
		//确认密码
		repasswordEditText.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String password = passwordEditText.getText().toString();		
					String repassword = repasswordEditText.getText().toString();
					Log.d("PassSet", "password"+password);
					Log.d("PassSet", "repassword"+repassword);
					SharedPreferences repassword_settings = getSharedPreferences(REPASSWORD_FILE, 0);
					SharedPreferences.Editor editor = repassword_settings.edit();
					editor.putString("repassword", repassword);
					editor.commit();
					if(!(repassword.equals(password))){

						new AlertDialog.Builder(PasswordSet.this)    	             
//			          .setTitle("您输入的的密码不相同")  	             
			          .setMessage("您输入的的密码不相同，请重新输入")  	            
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
				return false;
			}
		});
		repasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener(){
		            
		    @Override
		    public void onFocusChange(View v, boolean hasFocus){
		            	
		        if(hasFocus){
		          //获得焦点时文字的颜色
		           repasswordEditText.setTextColor(getResources().getColor(R.color.focus));//#FFFFFF,white
		         }else{
		            //失去焦点时文字的颜色
		           repasswordEditText.setTextColor(getResources().getColor(R.color.no_focus));//#555555
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
	
	
	
	

