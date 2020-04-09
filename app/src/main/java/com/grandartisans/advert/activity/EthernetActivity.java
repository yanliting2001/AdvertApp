package com.grandartisans.advert.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.network.NetworkDetailInfo;
import com.grandartisans.advert.network.NetworkManager;
import com.grandartisans.advert.utils.DecodeImgUtil;

public class EthernetActivity extends Activity implements OnClickListener{
	private final String TAG = "EthernetActivity";
	private LinearLayout ip_ll;
	private LinearLayout yanma_ll;
	private LinearLayout wangguan_ll;
	private LinearLayout dns_ll;
	private EditText edit_ip;
	private EditText edit_yanma;
	private EditText edit_wangguan;
	private EditText edit_dns;
	private Button submit;
	private Button ignore;
	private TextView text_ip;
	private TextView text_yanma;
	private TextView text_wangguan;
	private TextView text_dns;
	private NetworkDetailInfo netInfor;
	private LinearLayout network_detial,network_detial_buttons,network_detial_ll;
	private static final int DISCONNECTETHERNET = 12;
	private RelativeLayout network_detial_openbtn;
	private static final String WHITE = "#FFFFFF";
	private static final String LBULE = "#97FFFF";
	private static final int NORMAL_FRONT_SIZE = 24;
	private static final int BIGGER_FRONT_SIZE = 26;
	boolean isFirst = true;
	private String type;
	private ArrayList<Map<String, String>> networkInfo = new ArrayList<Map<String,String>>();
	Map<String, String> map = null;
	private Bitmap b;
	private RelativeLayout main_rl;
	private NetworkInfoReceiver networkInfoReceiver=null;
	Handler h = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case DISCONNECTETHERNET://�������������߱���
				if(isFirst){
					if(type.equals("boot")){
						EthernetActivity.this.finish();
						//startDetectNetwork();
					}else{
						EthernetActivity.this.finish();
					}
				isFirst = false;
				}
				initDataNet();
				//app.setNetworkInfo(networkInfo);
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
		setContentView(R.layout.ethernet_activity);
		main_rl = (RelativeLayout) findViewById(R.id.main_rl);
		b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_rl.setBackground(bd);
		type = getIntent().getStringExtra("type");
		initView();
		initFocus();
		ignore.setVisibility(View.GONE);
		initData();
		initOnClick();
		initFocus();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent("com.ynh.getip");
		sendBroadcast(intent);
		Log.i(TAG,"send broadcast com.ynh.getip");
	}

	private void initView(){
		network_detial = (LinearLayout) findViewById(R.id.network_detial);
		network_detial_buttons = (LinearLayout) findViewById(R.id.network_detial_buttons);
		ip_ll = (LinearLayout) findViewById(R.id.network_detial_ip_ll);
		yanma_ll = (LinearLayout) findViewById(R.id.network_detial_yanma_ll);
		wangguan_ll = (LinearLayout) findViewById(R.id.network_detial_wangguan_ll);
		dns_ll = (LinearLayout) findViewById(R.id.network_detial_dns_ll);
		edit_ip = (EditText) findViewById(R.id.edit_ip);
		edit_yanma = (EditText) findViewById(R.id.edit_yanma);
		edit_wangguan = (EditText) findViewById(R.id.edit_wangguan);
		edit_dns = (EditText) findViewById(R.id.edit_dns);
		submit = (Button) findViewById(R.id.network_detial_submit);
		ignore = (Button) findViewById(R.id.network_detial_ignore);
		text_ip = (TextView) findViewById(R.id.text_ip);
		text_yanma = (TextView) findViewById(R.id.text_yanma);
		text_wangguan = (TextView) findViewById(R.id.text_wangguan);
		text_dns = (TextView) findViewById(R.id.text_dns);
		
		network_detial_openbtn = (RelativeLayout) findViewById(R.id.network_detial_openbtn);
		network_detial_ll = (LinearLayout) findViewById(R.id.network_detial_ll);
		network_detial_openbtn = (RelativeLayout) findViewById(R.id.network_detial_openbtn);
		network_detial_openbtn.setVisibility(View.GONE);
	}
	
	private void initData(){
		//netInfor = networkMgr.getNetworkDetailInfo();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.ynh.broadcast.ip");
		networkInfoReceiver = new NetworkInfoReceiver();
		registerReceiver(networkInfoReceiver, intentFilter);

		/*
		edit_ip.setText(netInfor.getIp());
		edit_yanma.setText(netInfor.getNetmask());
		edit_wangguan.setText(netInfor.getGateway());
		edit_dns.setText(netInfor.getDns1());
		*/
	}
	private void initOnClick(){
		ip_ll.setOnClickListener(this);
		yanma_ll.setOnClickListener(this);
		wangguan_ll.setOnClickListener(this);
		dns_ll.setOnClickListener(this);
		submit.setOnClickListener(this);
		network_detial_openbtn.setOnClickListener(this);
	}
	
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		
		case R.id.network_detial_submit:
			startDetectingNetwork();
			break;
		
		default:
			break;
		}
	}
	private void startDetectingNetwork(){
		Intent i = new Intent();
		i.setClass(this, NetworkSetting_Detecting.class);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}

	@Override
	public boolean onKeyDown(int arg0, KeyEvent arg1) {
		// TODO Auto-generated method stub
		if(arg0==KeyEvent.KEYCODE_BACK&&arg1.getAction()==arg1.ACTION_DOWN){
			if(network_detial.getVisibility()==View.VISIBLE){
				/*
				if(type.equals("boot")){
					startDetectNetwork();
				}
				*/
			}
		}
		return super.onKeyDown(arg0, arg1);
	}
	
	
	class FocusChange implements OnFocusChangeListener{
		private EditText et;
		
		public FocusChange(EditText et){
			this.et = et;
		}
		@Override
		public void onFocusChange(View arg0, boolean b) {
			// TODO Auto-generated method stub
			if(b){
				et.setTextColor(Color.parseColor(LBULE));
				et.setTextSize(BIGGER_FRONT_SIZE);
			}else{
				et.setTextColor(Color.parseColor(WHITE));
				et.setTextSize(NORMAL_FRONT_SIZE);
			}
		}
		
	}
	class NetworkInfoReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG,"receive broadcat for getip");
			String ip = intent.getStringExtra("ipAddr");
			String netMask = intent.getStringExtra("netMask");
			String gateWay = intent.getStringExtra("gateWay");
			String dns1 = intent.getStringExtra("dns1");
			edit_ip.setText(ip);
			edit_yanma.setText(netMask);
			edit_wangguan.setText(gateWay);
			edit_dns.setText(dns1);
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//networkMgr.unreigest();
		unregisterReceiver(networkInfoReceiver);
		if(b!=null) b.recycle();
		super.onDestroy();
	}
	
	private void initDataNet(){
		networkInfo.clear();
		map = new HashMap<String, String>();
		map.put("netName", getResources().getString(R.string.detect_network_detectedNetworkTip_wireless));
		map.put("netState", getResources().getString(R.string.detect_network_wirelessTip_disconnected));
		map.put("hasConnected", "false");
		networkInfo.add(map);
	}
	 private void initFocus(){
	    	submit.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					// TODO Auto-generated method stub
					if(hasFocus){
						submit.setBackgroundResource(R.drawable.btn_focus_bg);
						submit.setTextColor(Color.parseColor("#FFFFFF"));
					}else{
						submit.setBackgroundResource(0);
						submit.setTextColor(Color.parseColor("#4c5c6a"));
					}
				}
			});
	    	ignore.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					// TODO Auto-generated method stub
					if(hasFocus){
						ignore.setBackgroundResource(R.drawable.btn_focus_bg);
						ignore.setTextColor(Color.parseColor("#FFFFFF"));
					}else{
						ignore.setBackgroundResource(0);
						ignore.setTextColor(Color.parseColor("#4c5c6a"));
					}
				}
			});
	    }
}
