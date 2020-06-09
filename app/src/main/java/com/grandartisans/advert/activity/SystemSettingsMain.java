package com.grandartisans.advert.activity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.entity.UpgradeBean;
import com.grandartisans.advert.service.DownLoadService;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.grandartisans.advert.utils.DensityUtil;
import com.grandartisans.advert.utils.ScaleAnimEffect;
import com.grandartisans.advert.view.Myhsv;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.other.RingLog;

public class SystemSettingsMain extends Activity {

	private LinearLayout networksetting_ll, commSetting_ll, screenSettings_ll,
		 accountSettings_ll, about_ll, zhanghu_ll,themeSettings_ll,contactus_ll;
	private Context mContext;
	private Myhsv hsv;
	private ScaleAnimEffect animEffect;
	private float scaleX = 1.30f;
	private float scaleY = 1.30f;
	private ImageView network_settingsTip_icon, comm_settingsTip_icon,
			screen_settingsTip_icon, play_settingsTip_icon,
			version_settingsTip_icon, accout_settingsTip_icon,
			about_settingsTip_icon, zhanghu_settingsTip_icon,theme_settingsTip_icon,contactus_settingsTip_icon,gsensor_settingsTip_icon;
	private TextView about_settingsTip_title, accout_settingsTip_title,
			version_settingsTip_title, play_settingsTip_title,
			screen_settingsTip_title, comm_settingsTip_title,
			network_settingsTip_title, zhanghu_settingsTip_title,theme_settingsTip_title,contactus_settingsTip_title,gsensor_settingsTip_title;

	Map<String, String> map = null;

	private RelativeLayout playSettings_ll,versionSettings_ll,gsensorSettings_ll;
	private ImageView update_red_icon;
	private TextView update_red_num;
	private boolean isNewVersion = false;
    private static final int UPDATETIME = 1024;
    private Bitmap b;
    private RelativeLayout main_rl;
    private boolean isLeft = false;
    private String serverVersion;
    private String serverVersionContent;
	private String dns = "http://www.baidu.com";
	private static final int INTERNET_CONNECTED_CMD = 10001;
	
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case INTERNET_CONNECTED_CMD:
				zhanghu_ll.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		//AgentApplication.getInstance().addActivity(this);
		setContentView(R.layout.system_settings_main_activity);
		mContext = this;
		main_rl = (RelativeLayout) findViewById(R.id.main_rl);
		b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_rl.setBackground(bd);
		initData();
		initView();
		animEffect = new ScaleAnimEffect();
		initParams();
		initOnkey();
		initOnfocusChange();
		initOnClick();
		/*
		CheckThead myThead = new CheckThead("dns");
		myThead.start();
		*/
	}
	
	private void initData(){
		/*
		networkInfo.clear();
		map = new HashMap<String, String>();
		map.put("netName", getResources().getString(R.string.wireless_bigTitle));
		map.put("netState", getResources().getString(R.string.detect_network_wirelessTip_disconnected));
		map.put("hasConnected", "false");
		networkInfo.add(map);
		app.setNetworkInfo(networkInfo);
		*/
	}
	
	private void initView(){
		networksetting_ll = (LinearLayout) findViewById(R.id.networksetting_ll);
		hsv =  (Myhsv) findViewById(R.id.hsv);
		commSetting_ll = (LinearLayout) findViewById(R.id.commSetting_ll);
		screenSettings_ll = (LinearLayout) findViewById(R.id.screenSettings_ll);
		playSettings_ll = (RelativeLayout) findViewById(R.id.playSettings_ll);
		versionSettings_ll = (RelativeLayout) findViewById(R.id.versionSettings_ll);
		gsensorSettings_ll = (RelativeLayout) findViewById(R.id.gsensorSettings_ll);
		accountSettings_ll = (LinearLayout) findViewById(R.id.accountSettings_ll);
		about_ll = (LinearLayout) findViewById(R.id.about_ll);
		zhanghu_ll = (LinearLayout) findViewById(R.id.zhanghu_ll);
		themeSettings_ll = (LinearLayout) findViewById(R.id.touyingSetting_ll);
		contactus_ll = (LinearLayout) findViewById(R.id.contactus_ll);

		network_settingsTip_icon = (ImageView) findViewById(R.id.network_settingsTip_icon);
		comm_settingsTip_icon = (ImageView) findViewById(R.id.comm_settingsTip_icon);
		screen_settingsTip_icon = (ImageView) findViewById(R.id.screen_settingsTip_icon);
		play_settingsTip_icon = (ImageView) findViewById(R.id.play_settingsTip_icon);
		gsensor_settingsTip_icon = (ImageView) findViewById(R.id.gsensor_settingsTip_icon);
		version_settingsTip_icon = (ImageView) findViewById(R.id.version_settingsTip_icon);
		accout_settingsTip_icon = (ImageView) findViewById(R.id.accout_settingsTip_icon);
		about_settingsTip_icon = (ImageView) findViewById(R.id.about_settingsTip_icon);
		zhanghu_settingsTip_icon = (ImageView) findViewById(R.id.zhanghu_settingsTip_icon);
		theme_settingsTip_icon = (ImageView) findViewById(R.id.touying_settingsTip_icon);
		contactus_settingsTip_icon = (ImageView) findViewById(R.id.contactus_settingsTip_icon);
		

		about_settingsTip_title = (TextView) findViewById(R.id.about_settingsTip_title);
		accout_settingsTip_title = (TextView) findViewById(R.id.accout_settingsTip_title);
		version_settingsTip_title = (TextView) findViewById(R.id.version_settingsTip_title);
		play_settingsTip_title = (TextView) findViewById(R.id.play_settingsTip_title);
		gsensor_settingsTip_title = (TextView) findViewById(R.id.gsensor_settingsTip_title);
		screen_settingsTip_title = (TextView) findViewById(R.id.screen_settingsTip_title);
		comm_settingsTip_title = (TextView) findViewById(R.id.comm_settingsTip_title);
		network_settingsTip_title = (TextView) findViewById(R.id.network_settingsTip_title);
		zhanghu_settingsTip_title = (TextView) findViewById(R.id.zhanghu_settingsTip_title);
		theme_settingsTip_title = (TextView) findViewById(R.id.touying_settingsTip_title);
		contactus_settingsTip_title = (TextView) findViewById(R.id.contactus_settingsTip_title);
		update_red_icon = (ImageView) findViewById(R.id.update_red_icon);
		update_red_num = (TextView) findViewById(R.id.update_red_num);
		GetRomUpgradeInfo();
	}
	
	private void startNetworkSettings(){
		Intent i = new Intent();
		i.setClass(mContext, NetworkSetting.class);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}

	private void startScreenSettings(){
		Intent i = new Intent();
		//i.putExtra("type", "screen");
		i.putExtra("type","system");
		i.setClass(mContext, ScreenSettingsActivity.class);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}
	private void startVersionSettings(){
		Intent i = new Intent();
		i.setClass(mContext, SystemUpdateSettings.class);
		i.putExtra("isNewVersion", isNewVersion);
		i.putExtra("serverVersion", serverVersion);
		i.putExtra("serverVersionContent", serverVersionContent);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}
	private void startAccountSettings(){
		/*
		Intent i = new Intent();
		i.setClass(mContext, AccountSetting.class);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
		*/
	}
	private void startAboutSettings(){
		Intent i = new Intent();
		i.setClass(mContext, About.class);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}

	/**
	 * 
	 * @鎻忚�? hasFocus:startAnim,otherwise looseAnim
	 * @鏂规硶鍚�? initOnfocusChange
	 * @杩斿洖绫诲�?void
	 * @鍒涘缓浜�?huang
	 * @鍒涘缓鏃堕棿 Sep 3, 201410:50:12 AM	
	 * @淇敼浜�?huang
	 * @淇敼鏃堕棿 Sep 3, 201410:50:12 AM	
	 * @淇敼澶囨敞 
	 * @since
	 * @throws
	 */
	private void initOnfocusChange() {
		networksetting_ll.setOnFocusChangeListener(new setOnFocus(
				networksetting_ll, network_settingsTip_icon,
				network_settingsTip_title));
		commSetting_ll.setOnFocusChangeListener(new setOnFocus(commSetting_ll,
				comm_settingsTip_icon, comm_settingsTip_title));
		screenSettings_ll.setOnFocusChangeListener(new setOnFocus(
				screenSettings_ll, screen_settingsTip_icon,
				screen_settingsTip_title));
		playSettings_ll
				.setOnFocusChangeListener(new setOnFocus(playSettings_ll,
						play_settingsTip_icon, play_settingsTip_title));
		gsensorSettings_ll
		.setOnFocusChangeListener(new setOnFocus(gsensorSettings_ll,
				gsensor_settingsTip_icon, gsensor_settingsTip_title));
		
		versionSettings_ll.setOnFocusChangeListener(new setOnFocus(
				versionSettings_ll, version_settingsTip_icon,
				version_settingsTip_title));
		accountSettings_ll.setOnFocusChangeListener(new setOnFocus(
				accountSettings_ll, accout_settingsTip_icon,
				accout_settingsTip_title));
		zhanghu_ll.setOnFocusChangeListener(new setOnFocus(zhanghu_ll,
				zhanghu_settingsTip_icon, zhanghu_settingsTip_title));
		about_ll.setOnFocusChangeListener(new setOnFocus(about_ll,
				about_settingsTip_icon, about_settingsTip_title));
		themeSettings_ll.setOnFocusChangeListener(new setOnFocus(themeSettings_ll,
				theme_settingsTip_icon, theme_settingsTip_title));
		contactus_ll.setOnFocusChangeListener(new setOnFocus(contactus_ll,
				contactus_settingsTip_icon, contactus_settingsTip_title));

	}
	
	class setOnFocus implements OnFocusChangeListener{
		
		private View v;
		private View icon;
		private View tv;
		public setOnFocus(View v,View icon,View tv){
			this.v = v;
			this.icon = icon;
			this.tv = tv;
		}
		@Override
		public void onFocusChange(View arg0, boolean hasFocus) {
			// TODO Auto-generated method stub
			if(hasFocus){
				startAnimation(v,icon,tv);
			}else{
				looseAnimation(v,icon,tv);
			}
		}
		
	}
	
	private void startAnimation(final View v,final View icon,final View tv){
		this.animEffect.setAttributs(1.0F, scaleX, 1.0F, scaleY, 200L);
		Animation localAnimation = this.animEffect.createAnimation();
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
				//v.setBackgroundResource(R.drawable.item_bg);
				tv.setAlpha(1.0f);
				icon.setAlpha(1.0f);
			}
		});
		v.startAnimation(localAnimation);
	}
	private void looseAnimation(final View v,final View icon,final View tv){
		this.animEffect.setAttributs(scaleX, 1.0F,scaleY, 1.0F, 200L);
		Animation localAnimation = this.animEffect.createAnimation();
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
				//v.setBackgroundResource(R.drawable.login_default_bg);
				tv.setAlpha(0.5f);
				icon.setAlpha(0.5f);
			}
		});
		v.startAnimation(localAnimation);
	}
	private void initOnClick(){
		networksetting_ll.setOnClickListener(new onItemClick());
		commSetting_ll.setOnClickListener(new onItemClick());
		screenSettings_ll.setOnClickListener(new onItemClick());
		playSettings_ll.setOnClickListener(new onItemClick());
		gsensorSettings_ll.setOnClickListener(new onItemClick());
		versionSettings_ll.setOnClickListener(new onItemClick());
		accountSettings_ll.setOnClickListener(new onItemClick());
		about_ll.setOnClickListener(new onItemClick());
		zhanghu_ll.setOnClickListener(new onItemClick());
		themeSettings_ll.setOnClickListener(new onItemClick());
		contactus_ll.setOnClickListener(new onItemClick());
	}
	class onItemClick implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.networksetting_ll:
				 startNetworkSettings();
				break;
			case R.id.commSetting_ll:
				break;
			case R.id.screenSettings_ll:
				 startScreenSettings();
				break;
			case R.id.playSettings_ll:
				break;
			case R.id.gsensorSettings_ll:
				break;
			case R.id.versionSettings_ll:
				 startVersionSettings();
				break;
			case R.id.accountSettings_ll:
				 startAccountSettings();
				break;
			case R.id.about_ll:
				 startAboutSettings();
				break;
			case R.id.zhanghu_ll://鏀逛负鐢ㄦ埛鍙嶉
//				startLoginSettings();
				startTimeSettings();
//				startFeedBackSettings();
				break;
			case R.id.touyingSetting_ll:
				startScreenSettings();
				break;
			case R.id.contactus_ll:
				startContactusSettings();
				break;
			default:
				break;
			}
		}
		
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(b!=null) b.recycle();
	}
	private void initParam(){
		if(isNewVersion){
			update_red_icon.setVisibility(View.VISIBLE);
			update_red_num.setVisibility(View.VISIBLE);
		}else{
			update_red_icon.setVisibility(View.INVISIBLE);
			update_red_num.setVisibility(View.INVISIBLE);
		}
	}
	class onkey implements OnKeyListener{
		private View v;
		public onkey(View v){
			this.v = v;
		}
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			if (arg1 == KeyEvent.KEYCODE_DPAD_LEFT
					&& arg2.getAction() == KeyEvent.ACTION_DOWN) {
				if (arg2.getRepeatCount() > 0) {
					return true;
				}
				if (v.getId() == R.id.playSettings_ll) {
					hsv.smoothScrollBy(DensityUtil.dip2px(mContext, -130), 0);
				} else if (v.getId() == R.id.commSetting_ll) {
					// hsv.getLocalVisibleRect(r)
					hsv.smoothScrollBy(DensityUtil.dip2px(mContext, -390), 0);
				} else if (v.getId() == R.id.touyingSetting_ll) {
					hsv.smoothScrollBy(DensityUtil.dip2px(mContext, -240), 0);
				} else if (v.getId() == R.id.screenSettings_ll) {
					hsv.smoothScrollBy(DensityUtil.dip2px(mContext, -240), 0);
				}
				
			} else if (arg1 == KeyEvent.KEYCODE_DPAD_RIGHT
					&& arg2.getAction() == KeyEvent.ACTION_DOWN) {
				if (arg2.getRepeatCount() > 0) {
					return true;
				}
				if (v.getId() == R.id.playSettings_ll) {
					hsv.smoothScrollBy(DensityUtil.dip2px(mContext, 240), 0);
				} else if (v.getId() == R.id.zhanghu_ll) {
					hsv.smoothScrollBy(DensityUtil.dip2px(mContext, 240), 0);
				} else if (v.getId() == R.id.versionSettings_ll) {
					hsv.smoothScrollBy(DensityUtil.dip2px(mContext, 390), 0);
				} else if (v.getId() == R.id.accountSettings_ll) {
					hsv.smoothScrollBy(DensityUtil.dip2px(mContext, 150), 0);
				}
			}
			return false;
		}
		
	}
	private void initOnkey(){
		networksetting_ll.setOnKeyListener(new onkey(networksetting_ll));
		commSetting_ll.setOnKeyListener(new onkey(commSetting_ll));
		screenSettings_ll.setOnKeyListener(new onkey(screenSettings_ll));
		playSettings_ll.setOnKeyListener(new onkey(playSettings_ll));
		gsensorSettings_ll.setOnKeyListener(new onkey(gsensorSettings_ll));
		versionSettings_ll.setOnKeyListener(new onkey(versionSettings_ll));
		accountSettings_ll.setOnKeyListener(new onkey(accountSettings_ll));
		zhanghu_ll.setOnKeyListener(new onkey(zhanghu_ll));
		about_ll.setOnKeyListener(new onkey(about_ll));
		themeSettings_ll.setOnKeyListener(new onkey(themeSettings_ll));
	}

	private void startTimeSettings(){
		Intent i = new Intent(mContext,TimeSettingActivity.class);
		startActivity(i);
	}
	private void initParams(){
		version_settingsTip_title.setText(CommonUtil.getVersionInfo());
	}

	private void startContactusSettings(){
		/*
		Intent i = new Intent();
		i.setClass(mContext, ContactUsActivity.class);
		startActivity(i);
		overridePendingTransition(R.anim.zoout, R.anim.zoin);
		*/
	}
	
	class CheckThead extends Thread{
		private String type ;
		public CheckThead(String type){
			this.type = type;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(isInternetConnected(dns)){
				mHandler.sendEmptyMessage(INTERNET_CONNECTED_CMD);
			}
			super.run();
		}
	}
	public static boolean isInternetConnected(String urlStr) {
		boolean result = false;
		try {
			// froyo之前的系统使用httpurlconnection存在bug
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
				System.setProperty("http.keepAlive", "false");
			}
	
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);// 设置为允许输出
			conn.setConnectTimeout(3 * 1000);// 3S超时
	
			Map<String, List<String>> headerMap = conn.getHeaderFields();
			if (headerMap != null) {
				Iterator<String> iterator = headerMap.keySet().iterator();
				if (iterator==null) {
					System.err.println("iterator==null");
					System.out.println("iterator==null");
				}
				while (iterator.hasNext()) {
					result = true;
					break;
				}
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return result;
	}

	private void GetRomUpgradeInfo(){
		String rom_version = CommonUtil.getVersionInfo();
		String rmac = CommonUtil.getEthernetMac();
		String strguid = CommonUtil.getDeviceId();
		String hwver = CommonUtil.getModel();
		AdvertModel mIModel = new AdvertModel();
		DevRing.httpManager().commonRequest(mIModel.systemUpgrade(rom_version,strguid,hwver,"","",""), new CommonObserver<UpgradeBean>() {
			@Override
			public void onResult(UpgradeBean upgradetbean) {
				RingLog.d("system upgrade check ok status = " + upgradetbean.getResult().getCode());
				boolean needupgrade = false;
				try {
					if(upgradetbean!=null){
						if(upgradetbean.getResult()!=null){
							RingLog.d("result = " + upgradetbean.getResult().getRet() + upgradetbean.getData().getDownload_link());
							if(upgradetbean.getResult().getRet()==0 && upgradetbean.getData().getDownload_link()!=null && upgradetbean.getData().getDownload_link().length()>0){
								isNewVersion = true;
								serverVersionContent = upgradetbean.getData().getDesc();
								serverVersion = upgradetbean.getData().getRom_version();
								needupgrade = true;
							}

						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(int i, String s) {
				RingLog.d("system upgrade check error i = " + i + "msg = " + s );
			}
		},null);
	}
	
}
