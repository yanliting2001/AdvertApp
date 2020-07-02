package com.grandartisans.advert.activity;

import com.grandartisans.advert.R;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.entity.post.ActiveCheckParamsBean;
import com.grandartisans.advert.model.entity.post.ActiveCheckResultBean;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.grandartisans.advert.utils.EncryptUtil;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.other.RingLog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EnterBoxActivity extends Activity{

	private Bitmap b;
	private LinearLayout main_ll;
	private Context mContext;
	private final int FINISH_ACTIVITY_CMD = 100001;
	private final int ACTIVE_CHECK_CMD = 100002;
	private TextView active_tx;
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			switch (msg.what) {
			case FINISH_ACTIVITY_CMD:
				FinishSettings();
				//EnterBoxActivity.this.finish();
				break;
			case ACTIVE_CHECK_CMD:
				checkActive();
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
		
		setContentView(R.layout.enter_box_layout);
		mContext = EnterBoxActivity.this;
		main_ll = (LinearLayout) findViewById(R.id.main_ll);
		b = new DecodeImgUtil(this, R.raw.userguide_activate).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_ll.setBackground(bd);
		active_tx = (TextView)findViewById(R.id.active_result_tx);
		//handler.sendEmptyMessageDelayed(finishActivity, 2000);
		
		if(isUsbActivate()==1){
			active_tx.setText(R.string.active_check_sucess_string);
			handler.sendEmptyMessageDelayed(FINISH_ACTIVITY_CMD, 2000);
		}else{
			active_tx.setText(R.string.activate_message);
			checkActive();
		}
	}
	
	@Override
	public void onDestroy() {
		handler.removeMessages(FINISH_ACTIVITY_CMD);
		handler.removeMessages(ACTIVE_CHECK_CMD);
		if(b!=null) b.recycle();
		super.onDestroy();
	}
	private void checkActive(){
		ActiveCheckParamsBean checkParams = new ActiveCheckParamsBean();
		String deviceid = SystemInfoManager.getDeviceId(getApplicationContext());
		if(deviceid==null) deviceid = "";
		checkParams.setDeviceClientid(deviceid.toUpperCase());
		checkParams.setTimestamp(System.currentTimeMillis());
        StringBuilder sign = new StringBuilder();
        EncryptUtil encrypt = new EncryptUtil();
        sign.append(checkParams.getDeviceClientid()).append("$").append(checkParams.getTimestamp()).append("$123456");
        String signed = encrypt.MD5Encode(sign.toString(),"");
        checkParams.setSign(signed);
        checkParams.setRqeuestUuid(CommonUtil.getRandomString(50));
		AdvertModel mIModel = new AdvertModel();
		DevRing.httpManager().commonRequest(mIModel.activeCheck(checkParams), new CommonObserver<ActiveCheckResultBean>() {
			@Override
			public void onResult(ActiveCheckResultBean activeCheckHttpResult) {
				RingLog.d("active check ok status = " + activeCheckHttpResult.getStatus() );
				if(activeCheckHttpResult.isSuccess()==true){
					if(activeCheckHttpResult.isData()==true){
						active_tx.setText(R.string.active_check_sucess_string);
						handler.sendEmptyMessageDelayed(FINISH_ACTIVITY_CMD, 2000);
					}else {
						active_tx.setText(R.string.active_check_failed_string);
						handler.removeMessages(ACTIVE_CHECK_CMD);
						handler.sendEmptyMessageDelayed(ACTIVE_CHECK_CMD, 10*1000);
					}
				}else {
					//active_tx.setText(result.getMsg());
					handler.removeMessages(ACTIVE_CHECK_CMD);
					handler.sendEmptyMessageDelayed(ACTIVE_CHECK_CMD, 10*1000);
				}
			}

			@Override
			public void onError(int i, String s) {
				RingLog.d("active check error i = " + i + "msg = " + s );
				handler.removeMessages(ACTIVE_CHECK_CMD);
				handler.sendEmptyMessageDelayed(ACTIVE_CHECK_CMD, 10*1000);
			}
		},null);
	}
	
	private void FinishSettings(){
		/*设置完成，禁止下次开机时自动启动*/
		DevRing.cacheManager().spCache("AppStart").put("firstStart",0);
		Intent intent = new Intent(this, MediaPlayerActivity.class);
		startActivity(intent);
	}
	
	private int isUsbActivate(){
		 Uri uri = Uri.parse("content://com.grandartisans.advert.provider.UsbActivateProvider/query");
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            int activate = cursor.getInt(cursor
                    .getColumnIndex("activate"));
            Log.i("EnterBoxActivity",   "activate:" + activate);
            return activate;
        }
        return 0;
	}
}
