package com.grandartisans.advert.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.DecodeImgUtil;

public class NoNetworkActivity extends Activity {
	private Bitmap b;
	private RelativeLayout main_rl;
	private TextView retry_tx;
	final private int START_COUNTDOWN_CMD = 100008;
	private int countdown = 30;
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case START_COUNTDOWN_CMD:
				if(countdown==0){
					NoNetworkActivity.this.finish();
				}else{
					updateRetryView();
					mHandler.sendEmptyMessageDelayed(START_COUNTDOWN_CMD, 1000);
					countdown--;
				}
				break;
			default:
				break;
			}
			
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_no_network);
		main_rl = (RelativeLayout) findViewById(R.id.no_network_rl);
		b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_rl.setBackground(bd);
			
		initView();	
		
		mHandler.sendEmptyMessageDelayed(START_COUNTDOWN_CMD, 1000);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(b!=null) b.recycle();
		mHandler.removeMessages(START_COUNTDOWN_CMD);
		super.onDestroy();
	}
	
	private void initView(){
		retry_tx = (TextView) findViewById(R.id.no_network_text_retry);
	}
	
	private void updateRetryView(){
		String message = String.format(getResources().getString(R.string.no_network_string_retry), countdown);
		retry_tx.setText(message);
	}
	
}
