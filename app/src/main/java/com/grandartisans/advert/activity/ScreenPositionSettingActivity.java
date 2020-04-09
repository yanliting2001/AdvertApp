package com.grandartisans.advert.activity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.ALFu700ProjectManager;

public class ScreenPositionSettingActivity extends Activity {

	private ALFu700ProjectManager mProjectManager = null;
	private Bitmap b;
	private RelativeLayout pmjz_rl;
	private TextView mSizeTv;
	private String mSettingType="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_activity);
		Intent intent = getIntent();
		mSettingType = intent.getStringExtra("type");
		mProjectManager = ALFu700ProjectManager.getInstance(getApplicationContext());
		mProjectManager.openProject();
		if(mSettingType.equals("position")){
			mProjectManager.setPosition();
		}else if(mSettingType.equals("size")){
			mProjectManager.setScale();
		}else{
			mProjectManager.setFocus();
		}
	}
	
	
	@Override
   public boolean onKeyDown(int keyCode, KeyEvent event){
		  if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			  mProjectManager.sendRightSlow();
			  return true;
		  } else  if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			  mProjectManager.sendLeftSlow();
			  return true;
		  }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			  mProjectManager.sendUpSlow();
			  return true;
		  }else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			  mProjectManager.sendDownSlow();
			  return true;
		  }else if(keyCode == KeyEvent.KEYCODE_BACK) {
		  	if(mSettingType.equals("position")){
		  		mProjectManager.setPosition();
			}else if(mSettingType.equals("size")){
				mProjectManager.setScale();
			}else{
				mProjectManager.setFocus();
			}
		  }
	      return super.onKeyDown(keyCode, event);
   }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
