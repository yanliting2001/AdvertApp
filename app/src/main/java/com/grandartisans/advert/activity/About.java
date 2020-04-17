package com.grandartisans.advert.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.interfaces.IMultKeyTrigger;
import com.grandartisans.advert.popupwindow.UpdateSureOrNotPop;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.grandartisans.advert.utils.MyMultKeyTrigger;
import com.grandartisans.advert.utils.Utils;

public class About extends Activity{
	
	private TextView box_name,box_version,box_mac,box_wifimac,box_ip,box_seriesNumb,box_versonInfo,box_ProductNumb;
	private Button restFactoryBtn;
	private Context mContext;
	private UpdateSureOrNotPop mUpdateSureOrNotPop = null;
	private Bitmap b;
	private RelativeLayout main_rl;

	private IMultKeyTrigger multKeyTrigger= new MyMultKeyTrigger(this);

		Handler h = new Handler(){
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
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
			//AgentApplication.getInstance().addActivity(this);
			setContentView(R.layout.about_settings);
			mContext = this;
			main_rl = (RelativeLayout) findViewById(R.id.main_rl);
			b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
			BitmapDrawable bd= new BitmapDrawable(b);
			main_rl.setBackground(bd);
			initView();
			initOnClick();
		}

		@Override
		protected void onDestroy() {
		// TODO Auto-generated method stub
			super.onDestroy();
			if(b!=null) b.recycle();
		}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			if(handlerMultKey(keyCode,event)){
				startMyApp(About.this,"app_browser");
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean handlerMultKey(int keyCode, KeyEvent event) {
		boolean vaildKey = false;
		if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
				|| keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
				&& multKeyTrigger.allowTrigger()) {
			// 是否是有效按键输入
			vaildKey = multKeyTrigger.checkKey(keyCode, event.getEventTime());
			// 是否触发组合键
			if (vaildKey && multKeyTrigger.checkMultKey()) {
				//执行触发
				multKeyTrigger.onTrigger();
				//触发完成后清除掉原先的输入
				multKeyTrigger.clearKeys();
				return true;
			}

		}
		return false;
	}

	private void startMyApp(Context context, String mode) {
		Intent it = new Intent();
		it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		it.setClass(context, MyAppActivity.class);
		it.putExtra("appmode", mode);
		context.startActivity(it);
	}
		
		private void initView(){
			box_name = (TextView) findViewById(R.id.box_name);
			box_version = (TextView) findViewById(R.id.box_version);
			box_mac = (TextView) findViewById(R.id.box_mac);
			box_wifimac = (TextView) findViewById(R.id.box_wifimac);
			box_ip = (TextView) findViewById(R.id.box_ip);
			box_seriesNumb = (TextView) findViewById(R.id.box_seriesNumb);
			box_ProductNumb = (TextView) findViewById(R.id.box_ProductNumb);
			
			restFactoryBtn = (Button) findViewById(R.id.restFactoryBtn);
			box_versonInfo = (TextView) findViewById(R.id.box_versonInfo);
			initParam();
		}
		private void initParam(){
			String versionName = Utils.getAppVersionName(mContext);
			String wlanmac= CommonUtil.getEthernetMac();
			String wifimac = CommonUtil.getWifiMac(mContext);
			box_mac.setText(wlanmac);
			box_wifimac.setText(wifimac);
			box_ip.setText(versionName);
			//box_version.setText(SystemInfoManager.getModelNumber());
			box_version.setText(CommonUtil.getModel());
			box_versonInfo.setText(CommonUtil.getVersionInfo());
			box_name.setText(CommonUtil.getProductName());
			box_ProductNumb.setText(CommonUtil.getDeviceId());
		}
		private void initOnClick(){
			restFactoryBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					mUpdateSureOrNotPop = new UpdateSureOrNotPop(mContext,"",mContext.getString(R.string.about_Restfactory_pop_content),
							mContext.getString(R.string.system_update_pop_sure_text),mContext.getString(R.string.system_update_pop_cancel_text));
					mUpdateSureOrNotPop.showListPop();
					mUpdateSureOrNotPop.setOnButtonClick(new UpdateSureOrNotPop.IButtonClick() {
						@Override
						public void onSureClick() {
							// TODO rest factory
							if (Build.VERSION.SDK_INT < 26) {
								sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
							} else {
								Intent intent = new Intent("android.intent.action.FACTORY_RESET");
								intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
								intent.setPackage("android");
								sendBroadcast(intent);
							}

//							Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
//				            intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
						}
						
						@Override
						public void onCancelClick() {
							// TODO cancel
							
						}
					});
				}
			});
		}
}
