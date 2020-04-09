package com.grandartisans.advert.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.grandartisans.advert.R;
import com.grandartisans.advert.network.WifiUtils;
import com.grandartisans.advert.utils.DecodeImgUtil;

public class IPRemoteActivity  extends Activity{
	final private String TAG = "IPRemoteActivity"; 
	private Context mContext;
	private LinearLayout main_ll;
	private Bitmap b;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.welcome_activity);
		mContext = this;
		Intent intent = getIntent();
		boolean usbactivate = intent.getBooleanExtra("usbactivate",false);
		main_ll = (LinearLayout) findViewById(R.id.main_ll);
		if(usbactivate){
			b = new DecodeImgUtil(mContext, R.raw.userguide_usb).getBitmap();
		}else{
			b = new DecodeImgUtil(mContext, R.raw.userguide_qrcode).getBitmap();
		}
		BitmapDrawable bd= new BitmapDrawable(b);
		main_ll.setBackground(bd);

		/*
		WifiUtils.setWifiApEnabled(mContext,true);
		Intent intentService = new Intent(mContext,RemoteService.class);
		startService(intentService);
		*/
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(b!=null) b.recycle();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ESCAPE:
		case KeyEvent.KEYCODE_BACK:			
				 return true;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			startScreenSetting();
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void startScreenSetting(){
		Intent i = new Intent(mContext, ScreenSettingsActivity.class);
        startActivity(i);
        IPRemoteActivity.this.finish();
	}
}
