package com.grandartisans.advert.activity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.common.Common;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.entity.post.HeartBeatParameter;
import com.grandartisans.advert.model.entity.res.HeartBeatResult;
import com.grandartisans.advert.service.UpgradeService;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.grandartisans.advert.utils.ValidUtils;
import com.grandartisans.advert.view.gifview.GifView;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.other.RingLog;

public class NetworkSetting_Detecting extends Activity{

	
	private ImageView local_network_icon,outside_network_icon,dns_network_icon,server_network_icon,tip_icon;
	private TextView server_network_text,dns_network_text,outside_network_text,local_network_text,tip_msg;
	private LinearLayout tip_ll;
	private static final int LOCALNETSUCCESS = 200;
	private static final int LOCALNETFAIL = 201;
	private static final int OUTSIDENETSUCCESS = 202;
	private static final int OUTSIDENETFAIL = 203;
	private static final int DNSSUCCESS = 204;
	private static final int DNSFAIL = 205;
	private static final int SEVERSUCCESS = 206;
	private static final int SEVERFAIL = 207;
	private static final int SHOWRESOUT = 208;
	private Context mContext;
	private String ip = "http://119.23.28.204/index.html";
	private String ip2 = "http://120.79.249.150";
	private String dns = "http://www.baidu.com";
	private String message = "";
	 private GifView left_gif;
	 private GifView right_gif;
	 private ImageView right_gif_finish,left_gif_finish;
	 private CheckThead myThead;
	 private Bitmap b;
	 private RelativeLayout main_rl;
	Handler h = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOCALNETSUCCESS:
				left_gif.setVisibility(View.GONE);
				left_gif_finish.setVisibility(View.VISIBLE);
				local_network_icon.setImageResource(R.drawable.lock_unlock);
				local_network_text.setText(mContext.getString(R.string.network_detect_local_network_success));
				CheckOutsideNet();
				break;
			case LOCALNETFAIL:
				left_gif.setVisibility(View.GONE);
				local_network_icon.setImageResource(R.drawable.red_wrong_icon);
				local_network_text.setText(mContext.getString(R.string.network_detect_local_network_fail));
				if(message.equals("")){
				 message = mContext.getString(R.string.network_detect_local_network_text);
				}
//				CheckOutsideNet();
				h.removeMessages(SHOWRESOUT);
				h.sendEmptyMessage(SHOWRESOUT);
				break;
			case OUTSIDENETSUCCESS:
				outside_network_icon.setImageResource(R.drawable.lock_unlock);
				outside_network_text.setText(mContext.getString(R.string.network_detect_outside_network_success));
				CheckDNS();
				break;
			case OUTSIDENETFAIL:
				outside_network_icon.setImageResource(R.drawable.red_wrong_icon);
				outside_network_text.setText(mContext.getString(R.string.network_detect_outside_network_fail));
				if(message.equals("")){
					 message = mContext.getString(R.string.network_detect_outside_network_text);
					}
				CheckDNS();
				break;
			case DNSSUCCESS:
				dns_network_icon.setImageResource(R.drawable.lock_unlock);
				dns_network_text.setText(mContext.getString(R.string.network_detect_dns_network_success));
				CheckSever();
				break;
			case DNSFAIL:
				dns_network_icon.setImageResource(R.drawable.red_wrong_icon);
				dns_network_text.setText(mContext.getString(R.string.network_detect_dns_network_fail));
				if(message.equals("")){
					 message = mContext.getString(R.string.network_detect_dns_network_text);
					}
				CheckSever();
				break;
			case SEVERSUCCESS:
				server_network_icon.setImageResource(R.drawable.lock_unlock);
				server_network_text.setText(mContext.getString(R.string.network_detect_server_network_success));
				h.removeMessages(SHOWRESOUT);
				h.sendEmptyMessageDelayed(SHOWRESOUT,1000);
				break;
			case SEVERFAIL:
				server_network_icon.setImageResource(R.drawable.red_wrong_icon);
				server_network_text.setText(mContext.getString(R.string.network_detect_server_network_fail));
				if(message.equals("")){
					 message = mContext.getString(R.string.network_detect_server_network_text);
					}
				h.removeMessages(SHOWRESOUT);
				h.sendEmptyMessageDelayed(SHOWRESOUT,1000);
				break;
			case SHOWRESOUT:
				local_network_icon.setVisibility(View.GONE);
				local_network_text.setVisibility(View.GONE);
				outside_network_icon.setVisibility(View.GONE);
				outside_network_text.setVisibility(View.GONE);
				dns_network_icon.setVisibility(View.GONE);
				dns_network_text.setVisibility(View.GONE);
				server_network_icon.setVisibility(View.GONE);
				server_network_text.setVisibility(View.GONE);
				
				tip_ll.setVisibility(View.VISIBLE);
				//TODO:show two different results:success or fail
				if(message.equals("")){
				   right_gif.setVisibility(View.GONE);
				   right_gif_finish.setVisibility(View.VISIBLE);
					
					tip_icon.setImageResource(R.drawable.lock_unlock);
					tip_msg.setText(mContext.getString(R.string.network_detect_success));
				}else{
					right_gif.setVisibility(View.GONE);
					tip_icon.setImageResource(R.drawable.red_wrong_icon);
					tip_msg.setText(mContext.getString(R.string.network_detect_fail)+message);
				}
				break;
			
			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(arg0);
		setContentView(R.layout.network_detect_settings);
		//AgentApplication.getInstance().addActivity(this);
		mContext = this;
		main_rl = (RelativeLayout) findViewById(R.id.main_rl);
		b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_rl.setBackground(bd);
		initView();
		left_gif.setGifImage(R.drawable.detecting_icon);
		left_gif.setShowDimension(400, 40);	
		CheckStart();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(b!=null) b.recycle();
		
	}
	
	private void CheckStart(){ //start checking
		message = "";
		if(checkLocalNet()){
			h.sendEmptyMessageDelayed(LOCALNETSUCCESS, 2000);
		}else{
			h.sendEmptyMessageDelayed(LOCALNETFAIL, 2000);
		}
	}
	
	class CheckThead extends Thread{
		private String type ;
		public CheckThead(String type){
			this.type = type;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(type.equals("outside")){
				if(CommonUtil.isInternetConnected(ip)||CommonUtil.isInternetConnected(ip2)){
					h.sendEmptyMessageDelayed(OUTSIDENETSUCCESS, 2000);
				}else{
					h.sendEmptyMessageDelayed(OUTSIDENETFAIL, 2000);
				}
			}else if(type.equals("dns")){
				if( CommonUtil.isInternetConnected(dns)){
					h.sendEmptyMessageDelayed(DNSSUCCESS, 2000);
				}else{
					h.sendEmptyMessageDelayed(DNSFAIL, 2000);
				}
			}
			
			super.run();
		}
	}
	
	private void CheckOutsideNet(){ //check the Internet
		right_gif.setGifImage(R.drawable.detecting_icon);
		right_gif.setShowDimension(420, 40);
		myThead = new CheckThead("outside");
		myThead.start();

	}
	private void CheckDNS(){
		myThead = new CheckThead("dns");
		myThead.start();
	}
	private void CheckSever(){
		heardBeat(UpgradeService.mToken);
	}

	private void initView(){
		local_network_icon = (ImageView) findViewById(R.id.local_network_icon);
		outside_network_icon = (ImageView) findViewById(R.id.outside_network_icon);
		dns_network_icon = (ImageView) findViewById(R.id.dns_network_icon);
		server_network_icon = (ImageView) findViewById(R.id.server_network_icon);
		tip_icon = (ImageView) findViewById(R.id.tip_icon);
		
		server_network_text = (TextView) findViewById(R.id.server_network_text);
		dns_network_text = (TextView) findViewById(R.id.dns_network_text);
		outside_network_text = (TextView) findViewById(R.id.outside_network_text);
		local_network_text = (TextView) findViewById(R.id.local_network_text);
		tip_msg = (TextView) findViewById(R.id.tip_msg);
		left_gif = (GifView) findViewById(R.id.left_gif);
		right_gif = (GifView) findViewById(R.id.right_gif);
		tip_ll = (LinearLayout) findViewById(R.id.tip_ll);
		left_gif_finish = (ImageView) findViewById(R.id.left_gif_finish);
		right_gif_finish =  (ImageView) findViewById(R.id.right_gif_finish);
	}

	private boolean checkLocalNet(){
		return ValidUtils.isNetworkExist(mContext);
	}

	private void heardBeat(final String token ) {
		AdvertModel mIModel = new AdvertModel();
		HeartBeatParameter parameter = new HeartBeatParameter();
		String 	deviceId =  SystemInfoManager.getDeviceId(getApplicationContext());
		parameter.setDeviceClientid(deviceId);
		parameter.setTimestamp(System.currentTimeMillis());
		parameter.setToken(token);
		DevRing.httpManager().commonRequest(mIModel.sendHeartBeat(parameter), new CommonObserver<HeartBeatResult>() {
			@Override
			public void onResult(HeartBeatResult result) {
				h.sendEmptyMessageDelayed(SEVERSUCCESS, 2000);
			}
			@Override
			public void onError(int i, String s) {
				RingLog.d("send HeartBeat error  i = " + i + "msg = " + s );
				h.sendEmptyMessageDelayed(SEVERFAIL, 2000);
			}
		},null);
	}
}
