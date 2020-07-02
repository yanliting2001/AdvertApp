package com.grandartisans.advert.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.grandartisans.advert.R;
import com.grandartisans.advert.network.NetworkManager;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.DecodeImgUtil;

public class NetworkCheckActivity  extends Activity{
	final private String TAG = "NetworkCheckActivity"; 
	private Context mContext;
	private NetworkManager networkManager = null;
	private LinearLayout main_ll;
	private Bitmap b;
	private  NetWorkReceiver mReceiver;
	final private int START_GUIDER_CMD = 100008;
	final private int START_CHECK_USBACTIVATE_CMD = 100009;
	
	private String ip = "http://www.baidu.com";
	private String ip2 = "http://www.qq.com";
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case START_GUIDER_CMD:
				startNoNetwork();
				break;
			case START_CHECK_USBACTIVATE_CMD:
				checkUsbActivate();
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
		networkManager = NetworkManager.getInstance(this);
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
    	if(isUsbActivate()==1){
    		startIpRemote(true);
    	}else{
    		checkNetwork();
    	}
		mHandler.sendEmptyMessageDelayed(START_GUIDER_CMD, 60*1000);
		mHandler.sendEmptyMessageDelayed(START_CHECK_USBACTIVATE_CMD, 1000);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mHandler.removeMessages(START_GUIDER_CMD);
		mHandler.removeMessages(START_CHECK_USBACTIVATE_CMD);
		if(mReceiver!=null){
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mHandler.removeMessages(START_GUIDER_CMD);
		if(b!=null) b.recycle();
		mHandler.removeMessages(START_CHECK_USBACTIVATE_CMD);
	}
	
	private void checkNetwork() {
		if(networkManager.isEthConnected()){
			Log.i(TAG,"Eth is connected ");
			new Thread(runableInternetCheck).start();
		}else if(networkManager.isDataConnected()){
			Log.i(TAG,"4G Net is connected ");
			new Thread(runableInternetCheck).start();
		}else {
			Log.i(TAG,"no network is connected ");
			netWorkReceiver(mContext);
			new Thread(runableInternetCheck).start();
		}
	}

	
	private void netWorkReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mReceiver = new NetWorkReceiver();
        context.registerReceiver(mReceiver, filter);
    }
	
	private void startRomUpgrade(){
		Intent i = new Intent();
		i.setClass(mContext, RomUpgradeActivity.class);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
		//NetworkCheckActivity.this.finish();
	}
	
	private void startIpRemote(boolean usbactivate){
		Intent i = new Intent();
		i.setClass(this, IPRemoteActivity.class);
		i.putExtra("usbactivate", usbactivate);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}
	
	private void startNoNetwork(){
		Intent i = new Intent();
		i.setClass(mContext, NoNetworkActivity.class);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
		//NetworkCheckActivity.this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		} 
		return super.onKeyDown(keyCode, event);   
	}
	
	Runnable runableInternetCheck = new Runnable() {
		@Override
		public void run() {
			Log.i(TAG,"runableInternetCheck  start ");
			if(CommonUtil.isInternetConnected(ip) || CommonUtil.isInternetConnected(ip)){
				Log.i(TAG,"runableInternetCheck  sucess ");
				startRomUpgrade();
			}
			Log.i(TAG,"runableInternetCheck  end ");
		}
	};
	
	class NetWorkReceiver extends BroadcastReceiver {
 		String TAG = "NetWorkReceiver";
 		@Override
 		public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();  
            if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            	if(isUsbActivate()==1){
            		startIpRemote(true);
            	}else{
	            	NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO); 
	            	ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);   
	                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
	                if(activeNetInfo!=null && activeNetInfo.isConnected()){
	                	Log.i(TAG,"CONNECTIVITY_CHANGE ActiveNetworkInfo type = " + activeNetInfo.getType());
	                	new Thread(runableInternetCheck).start();
	                }
            	}
            }
 		}
 	}
	
	private void checkUsbActivate(){
		if(isUsbActivate()==1){
    		startIpRemote(true);
    	}else{
    		mHandler.removeMessages(START_CHECK_USBACTIVATE_CMD);
    		mHandler.sendEmptyMessageDelayed(START_CHECK_USBACTIVATE_CMD, 1000);
    	}
	}
	
	private int isUsbActivate(){
		 Uri uri = Uri.parse("content://com.grandartisans.advert.provider.UsbActivateProvider/query");
         Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
         while (cursor.moveToNext()) {
             int activate = cursor.getInt(cursor
                     .getColumnIndex("activate"));
             Log.i(TAG,   "activate:" + activate);
             return activate;
         }
         return 0;
	}
}
