package com.grandartisans.advert.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.grandartisans.advert.R;
import com.grandartisans.advert.interfaces.NetworkEventListener;
import com.grandartisans.advert.network.NetworkDetailInfo;
import com.grandartisans.advert.network.NetworkManager;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.grandartisans.advert.utils.ScaleAnimEffect;
import com.grandartisans.advert.view.AlwaysMarqueeTextView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NetworkSetting extends Activity{

	private ArrayList<String> dataList = new ArrayList<String>();
	private int[] dataStr = {R.string.network_detect_BigTitle,R.string.network_menu_speed_detect,R.string.network_menu_blueTooth};
	//无线网络，有线网络，网络检测，网速检测，蓝牙//,R.string.network_menu_blueTooth
	private int curPosition = 0;
	private Context mContext;
	private static final int HASCONNECTED = 15;
	private static final int NOCONNECT = 9;
	private static final int DETECTENTHERNET_SUCCESS = 5;
	private static final int DETECTENTHERNET_FIAL = 6;
	private static final int DETECTWIRELESS = 7;
	private NetworkDetailInfo netInfor = null;
	private NetworkManager networkMgr;
	private ArrayList<Map<String, String>> networkInfo = null;
	private Bitmap b;
	private RelativeLayout main_rl;
	private LinearLayout network_item,network_detect_item,network_menu_speed_detect_item,network_menu_blueTooth_item;
	private TextView network_title,network_detect_title,network_detect_speed_title,network_detect_bluetooth_title;
	private ScaleAnimEffect animEffect;
	private float scaleX = 1.15f;
	private float scaleY = 1.15f;
	private ImageView netIcon;
	private static final int NET_REFRESH_WIFI_LIST = 1;
	private AlwaysMarqueeTextView netState;
	Handler h = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) { 
			case DETECTENTHERNET_SUCCESS:
				String title = mContext.getString(R.string.ethernet_bigTitle);
				String state = getResources().getString(R.string.detect_network_wirelessTip_connected);
				setPamams(true, title, state);
				break;
			case DETECTENTHERNET_FIAL:
				String title2 = mContext.getString(R.string.wireless_bigTitle);
				setPamams(false, title2, "");
				netInfor = null;
				break;
			case DETECTWIRELESS:
				String title1 = mContext.getString(R.string.wireless_bigTitle);
				
				if(networkMgr!=null){
					netInfor = networkMgr.getNetworkDetailInfo();
				}
				String state1 = netInfor.getSsid();
//				mAdapter.setConnected(true,netInfor.ssid );
				setPamams(true, title1, state1);
				break;
			case NOCONNECT:
				String title3 = mContext.getString(R.string.wireless_bigTitle);
				setPamams(false, title3, "");
				break;
			case NET_REFRESH_WIFI_LIST:
				initParam();
				break;
			default:
				break;
			}
			
		};
	};

	NetworkEventListener listener = new NetworkEventListener(){
		@Override
		public void onShowWifiList(List<ScanResult> list){

		}
		@Override
		public void onEthernetDisconnect(){
			String title2 = mContext.getString(R.string.wireless_bigTitle);
			setPamams(false, title2, "");
			netInfor = null;
		}
		@Override
		public void onNoNetwork(){
			String title3 = mContext.getString(R.string.wireless_bigTitle);
			setPamams(false, title3, "");
		}
		@Override
		public void onEthernetConnected(){
			String title = mContext.getString(R.string.ethernet_bigTitle);
			String state = getResources().getString(R.string.detect_network_wirelessTip_connected);
			setPamams(true, title, state);
		}
		@Override
		public void onWifiConnected(){
			String title1 = mContext.getString(R.string.wireless_bigTitle);
			if(networkMgr!=null){
				netInfor = networkMgr.getNetworkDetailInfo();
			}
			String state1 = netInfor.getSsid();
			setPamams(true, title1, state1);
		}
		@Override
		public void onWifiConnecteFailed(){

		}
		@Override
		public void onWifiConnecting(){

		}
		@Override
		public void onWifiListRefresh()
		{
			initParam();
		}
		@Override
		public void showPassword(ScanResult result){

		}
	};
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(arg0);
		//AgentApplication.getInstance().addActivity(this);
		setContentView(R.layout.network_settings);
		main_rl = (RelativeLayout) findViewById(R.id.main_rl);
		b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_rl.setBackground(bd);
//		mListView = (ListView) findViewById(R.id.menuList);
		initView();
		mContext = this;
		animEffect = new ScaleAnimEffect();
		networkMgr = NetworkManager.getInstance(mContext);
		networkMgr.registerListener(listener);
		networkMgr.regiestbroadcast();
		initOnClick();
		initOnFocus();
		//initParam();
		network_item.requestFocus();
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		initParam();
		if(networkMgr!=null) {
			networkMgr.regiestbroadcast();
		}
		super.onResume();
	}
	
	private void initParam(){
		String title="";
		String state="";
		if(networkMgr.isEthConnected()==true){
			title = mContext.getString(R.string.ethernet_bigTitle);
			state = mContext.getString(R.string.detect_network_detectedNetworkTip_ethernet);
			setPamams(true, title, state);
		}else if(networkMgr.isDataConnected() == true) {
			title = mContext.getString(R.string.network_4G_bigTitle);
			state = mContext.getString(R.string.detect_network_detectedNetworkTip_4G);
			setPamams(true, title, state);
		} else {
			if(networkMgr.isWIFIEnable()==false){
                networkMgr.enableWIFI(true);
			}
			if(networkMgr.isWifiConnected()) {
				title = mContext.getString(R.string.wireless_bigTitle);
				state = mContext.getString(R.string.detect_network_detectedNetworkTip_wireless);
				setPamams(true, title, state);
			}else {
				title = mContext.getString(R.string.wireless_bigTitle);
				state = mContext.getString(R.string.detect_network_detectedNetworkTip_wireless);
				setPamams(false, title, state);
			}
		}
	}  
	
	private void setPamams(boolean b,String title,String state){
		if(!network_title.getText().toString().equals(title)){
			network_title.setText(title);
		}
		if(title.equals(mContext.getString(R.string.wireless_bigTitle))
				||title.equals(mContext.getString(R.string.ethernet_bigTitle))
				||title.equals(mContext.getString(R.string.network_4G_bigTitle))){
			if(b){
				if(title.equals(mContext.getString(R.string.wireless_bigTitle))){
					String ssid = networkMgr.getNetworkDetailInfo().getSsid();
					if(ssid!=null && ssid.contains("\"")){
						state = ssid.split("\"")[1];
						ScanResult mScanResult = networkMgr.getScanResultBySSID(ssid.split("\"")[1]);
						if(mScanResult!=null){
							int wifi_de = networkMgr.calSignalLevel(mScanResult.level, 4);
							if (wifi_de == 1) {
								netIcon.setImageResource(R.drawable.icon_wifi_1);
							} else if (wifi_de == 2) {
								netIcon.setImageResource(R.drawable.icon_wifi_2);
							} else if (wifi_de == 3) {
								netIcon.setImageResource(R.drawable.icon_wifi_3);
							} else {
								netIcon.setImageResource(R.drawable.icon_wifi);
							}
						}
					}
				}else{
					netIcon.setImageResource(R.drawable.lock_unlock);
				}
				netIcon.setVisibility(View.VISIBLE);
				if(!netState.getText().toString().equals(state)){
					netState.setText(state);
				}
				netState.setVisibility(View.VISIBLE);
			}else{
				netIcon.setImageResource(0);
				netState.setText("");
//				netIcon.setVisibility(View.INVISIBLE);
//				netState.setVisibility(View.INVISIBLE);
			}
		}
	}

	private void startNetworkSettingDetecting(){
		Intent i = new Intent();
		i.setClass(this, NetworkSetting_Detecting.class);
		startActivity(i);
	} 	

	private void startWireless(){
		Intent i  = new Intent();
		i.setClass(mContext, WirelessActivity.class);
		i.putExtra("type", "sys");
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}
	private void startEthernet(){
		Intent i  = new Intent();
		i.setClass(mContext, EthernetActivity.class);
		i.putExtra("type", "sys");
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}
	private void startTelephonyInfo(){
		Intent i  = new Intent();
		i.setClass(mContext, TelephonyInfoActivity.class);
		i.putExtra("type", "sys");
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(networkMgr!=null){
			networkMgr.unreigest();
		}
		if(b!=null) b.recycle();
		super.onDestroy();
	}
 	private void initView(){
 		network_item = (LinearLayout) findViewById(R.id.network_item);
 		network_detect_item = (LinearLayout) findViewById(R.id.network_detect_item);
 		network_menu_speed_detect_item = (LinearLayout) findViewById(R.id.network_menu_speed_detect_item);
 		network_menu_blueTooth_item = (LinearLayout) findViewById(R.id.network_menu_blueTooth_item);
 		network_title = (TextView) findViewById(R.id.network_title);
 		netIcon = (ImageView) findViewById(R.id.netIcon);
 		netState =  (AlwaysMarqueeTextView) findViewById(R.id.netState);
 		network_detect_title = (TextView) findViewById(R.id.network_detect_title);
 		network_detect_speed_title = (TextView) findViewById(R.id.network_detect_speed_title);
 		network_detect_bluetooth_title = (TextView) findViewById(R.id.network_detect_bluetooth_title);
 	}
 	private void initOnClick(){
 		network_item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String title = network_title.getText().toString();
				System.err.println("title = " +  title);
				if(title.equals(mContext.getString(R.string.ethernet_bigTitle))){
					System.err.println("eeeee");
					startEthernet();
				}else if(title.equals(mContext.getString(R.string.network_4G_bigTitle))){
					startTelephonyInfo();
				}
				else{
					startWireless();
				}
			}
		});
 		network_detect_item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startNetworkSettingDetecting();
			}
		});
 	}
 	private void initOnFocus(){
 		network_item.setOnFocusChangeListener(new setOnFocus(network_item, netIcon, network_title,netState));
 		network_detect_item.setOnFocusChangeListener(new setOnFocus(network_detect_item, null, network_detect_title,null));
 		network_menu_speed_detect_item.setOnFocusChangeListener(new setOnFocus(network_menu_speed_detect_item, null, network_detect_speed_title,null));
 		network_menu_blueTooth_item.setOnFocusChangeListener(new setOnFocus(network_menu_blueTooth_item, null, network_detect_bluetooth_title,null));
 	}
 	
 	private void startAnimation(final View v,final View icon,final View tv,final View tv2){
		this.animEffect.setAttributs(1.0F, scaleX, 1.0F, scaleY, 150L);
		Animation localAnimation = this.animEffect.createAnimation1();
		localAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				//v.setBackgroundResource(R.drawable.network_item_bg);
				if(tv!=null){
					tv.setAlpha(1.0f);
					((TextView)tv).setTextSize(32);
				}
				if(icon!=null){
					icon.setAlpha(1.0f);
				}
				if(tv2!=null){
					tv2.setAlpha(1.0f);
				}
			}
		});
		if(icon!=null){
		Animation localAnimation1 = this.animEffect.createAnimation1();
		icon.startAnimation(localAnimation1);
		}
		if(tv2!=null){
		Animation localAnimation2 = this.animEffect.createAnimation1();
		tv2.startAnimation(localAnimation2);
		}
		tv.startAnimation(localAnimation);
	}
	private void looseAnimation(final View v,final View icon,final View tv,final View tv2){
		this.animEffect.setAttributs(scaleX, 1.0F,scaleY, 1.0F, 150L);
		Animation localAnimation = this.animEffect.createAnimation1();
		localAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				//v.setBackgroundResource(R.drawable.network_item_bg);
				if(tv!=null){
					tv.setAlpha(0.5f);
					((TextView)tv).setTextSize(28);
				}
				if(icon!=null){
					icon.setAlpha(0.5f);
				}
				if(tv2!=null){
					tv2.setAlpha(0.5f);
				}
			}
		});
		if(icon!=null){
		Animation localAnimation1 = this.animEffect.createAnimation1();
		icon.startAnimation(localAnimation1);
		}
		if(tv2!=null){
		Animation localAnimation2 = this.animEffect.createAnimation1();
		tv2.startAnimation(localAnimation2);
		}
		tv.startAnimation(localAnimation);
	}
	class setOnFocus implements OnFocusChangeListener{
		
		private View v;
		private View icon;
		private View title;
		private View state;
		public setOnFocus(View v,View icon,View title,View state){
			this.v = v;
			this.icon = icon;
			this.title = title;
			this.state = state;
		}
		@Override
		public void onFocusChange(View arg0, boolean hasFocus) {
			// TODO Auto-generated method stub
			if(hasFocus){
				startAnimation(v,icon,title,state);
			}else{
				looseAnimation(v,icon,title,state);
			}
		}
		
	}

	private void startWifiList(){
		Intent i = new Intent("com.txbox.netSettings");
		i.putExtra("type", "other");
//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
}
