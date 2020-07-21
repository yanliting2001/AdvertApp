package com.grandartisans.advert.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.grandartisans.advert.R;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.entity.UpgradeBean;
import com.grandartisans.advert.service.DownLoadService;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.other.RingLog;

public class RomUpgradeActivity extends Activity{

	private Bitmap b;
	private LinearLayout main_ll;
	private Context mContext;	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		setContentView(R.layout.enter_box_layout);
		mContext = RomUpgradeActivity.this;
		main_ll = (LinearLayout) findViewById(R.id.main_ll);
		b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_ll.setBackground(bd);
		GetRomUpgradeInfo();
		//handler.sendEmptyMessageDelayed(finishActivity, 5000);
	}
	
	@Override
	public void onDestroy() {
		if(b!=null) b.recycle();
		super.onDestroy();
	}
	
	private void startIpRemote(){
		Intent i = new Intent();
		i.putExtra("type","guide");
		i.setClass(this, SetTFMiniActivity.class);
		//i.putExtra("usbactivate", false);
		startActivity(i);
//		overridePendingTransition(R.anim.zoout, R.anim.zoin);
		this.finish();
	}

	private void startDownLoadService(){
		Intent i = new Intent(mContext,DownLoadService.class);
		startService(i);
	}
	/**
	 * 
	 * @描述:获取rom升级信息
	 * @方法名: GetRomUpgradeInfo
	 * @返回类型 void
	 * @创建人 Administrator
	 * @创建时间 2014-9-10下午5:21:54	
	 * @修改人 Administrator
	 * @修改时间 2014-9-10下午5:21:54	
	 * @修改备注 
	 * @since
	 * @throws
	 */
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
								startDownLoadService();
								boolean isNewVersion = true;
								String serverVersionContent = upgradetbean.getData().getDesc();
								String serverVersion = upgradetbean.getData().getRom_version();
								Intent i = new Intent();
								i.setClass(mContext, SystemUpdateSettings.class);
								i.putExtra("isNewVersion", isNewVersion);
								i.putExtra("serverVersion", serverVersion);
								i.putExtra("serverVersionContent", serverVersionContent);
								startActivity(i);
								needupgrade = true;
							}

						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
				if(needupgrade == false) {
					startIpRemote();
					//handler.sendEmptyMessageDelayed(finishActivity, 5000);

				}
			}

			@Override
			public void onError(int i, String s) {
				RingLog.d("system upgrade check error i = " + i + "msg = " + s );
				startIpRemote();
			}
		},null);
	}
}
