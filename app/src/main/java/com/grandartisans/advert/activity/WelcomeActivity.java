package com.grandartisans.advert.activity;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.ljy.devring.DevRing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class WelcomeActivity extends Activity{
	private Context mContext;
	private LinearLayout main_ll;
	private Bitmap b;
	private static final int FIRSTENTERBOX = 1000;
	private static final int START_AD_PLAYER_CMD = 1001;

	Handler h = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case FIRSTENTERBOX:
					startNetworkCheck();
					break;
				case START_AD_PLAYER_CMD:
					startAdPlayer();
					break;
				default:
					break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.welcome_activity);
		mContext = this;
		main_ll = (LinearLayout) findViewById(R.id.main_ll);
		b = new DecodeImgUtil(mContext, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_ll.setBackground(bd);
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method
		super.onStart();
		int firstStart = DevRing.cacheManager().spCache("AppStart").getInt("firstStart",1);
		if(firstStart==1) {
			h.sendEmptyMessageDelayed(FIRSTENTERBOX, 3000);
		}else{
			h.sendEmptyMessageDelayed(START_AD_PLAYER_CMD, 3000);

		}
	}

	private void startAdPlayer(){
		Intent intent = new Intent(this, MediaPlayerActivity.class);
		startActivity(intent);
	}
	private void startNetworkCheck(){
		Intent i = new Intent();
		i.setClass(mContext, NetworkCheckActivity.class);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
		//WelcomeActivity.this.finish();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(b!=null) b.recycle();
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ESCAPE:
		case KeyEvent.KEYCODE_BACK:
				 return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
