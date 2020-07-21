package com.grandartisans.advert.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.interfaces.ElevatorDoorEventListener;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.grandartisans.advert.utils.ElevatorDoorManager;
import com.grandartisans.advert.utils.SerialPortUtils;

public class SetTFMiniActivity extends Activity {
	final static String TAG = "SetTFMiniActivity";
	private Bitmap b;
	private LinearLayout main_rl;
	private SerialPortUtils serialPortUtils = null;
	private int distance = 0;
	private int strength = 0;
	private Dialog distanceSetDialog = null;
	private TextView messageTV=null;
	private Handler handler;
	private ElevatorDoorManager mElevatorDoorManager = null;
	private String mSource = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_activity);
		main_rl = (LinearLayout) findViewById(R.id.main_ll);
		b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_rl.setBackground(bd);
		handler = new Handler();
		mElevatorDoorManager = ElevatorDoorManager.getInstance();
		if(mElevatorDoorManager!=null)
			mElevatorDoorManager.registerListener(mElevatorDoorEventListener);
		initView();
		showSetDistanceDialog(SetTFMiniActivity.this);
		mSource = getIntent().getStringExtra("source");
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		showSetDistanceDialog(SetTFMiniActivity.this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(b!=null) b.recycle();
		
	}
	
	private void initView(){
		//retry_tx = (TextView) findViewById(R.id.no_network_text_retry);
	}

	ElevatorDoorEventListener mElevatorDoorEventListener = new ElevatorDoorEventListener(){
		@Override
		public void onElevatorDoorOpen(){

		}
		@Override
		public void onElevatorDoorClose(){

		}
		@Override
		public void onElevatorError(){

		}
		@Override
		public void onValueChanged(int value){
			distance = value;
			handler.post(runnable);
		}

	};

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Log.i(TAG, "distance = " + distance );
			if (distanceSetDialog != null && distanceSetDialog.isShowing()) {
				String message = String.format(getResources().getString(R.string.distmessage), strength, distance);
				if(messageTV!=null ){
					//Log.i(TAG, "set messageTV  = " + message );
					messageTV.setText(message);
				}
				else Log.i(TAG,"messageTV view is null");
			}
		}
	};
	private void showSetDistanceDialog(Context context) {
		if((distanceSetDialog==null||!distanceSetDialog.isShowing())){
			View view = View.inflate(getApplicationContext(), R.layout.dialog_layout, null);
			distanceSetDialog = new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.bt_distance_title_string)).
					setView(view).setPositiveButton(context.getResources().getString(R.string.bt_setting_string),new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							distanceSetDialog.dismiss();
							//distanceSetDialog = null;
							if(mElevatorDoorManager!=null)
								mElevatorDoorManager.setDefaultDistance(distance);
							finishSetting();
						}
					}
				).create();
			//distanceSetDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			distanceSetDialog.show();
			messageTV  = (TextView) distanceSetDialog.findViewById(R.id.distance_dialog);
			String message = String.format(getResources().getString(R.string.distmessage), strength, distance);
			//if(messageTV!=null ) messageTV.setText(message);
			//else Log.i(TAG,"messageTV view is null");
			
			distanceSetDialog.setOnKeyListener(keylistener);
		}
	}
	
	OnKeyListener keylistener = new OnKeyListener(){
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0)
            {
				distanceSetDialog.dismiss();
				SetTFMiniActivity.this.finish();
				return true;
            }
            else
            {
            	return false;
            }
		}
    } ;
	private void finishSetting(){
		if(isUsbActivate()==1){
			Intent i = new Intent();
			i.setClass(this, TimeSettingActivity.class);
			startActivity(i);
		}else{
			if(mSource!=null && mSource.equals("system")){
				SetTFMiniActivity.this.finish();
			}else {
				Intent i = new Intent();
				i.setClass(this, EnterBoxActivity.class);
				startActivity(i);
			}
		}
	}
	
	private int isUsbActivate(){
		 Uri uri = Uri.parse("content://com.grandartisans.advert.provider.UsbActivateProvider/query");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            int activate = cursor.getInt(cursor
                    .getColumnIndex("activate"));
            Log.i(TAG,   "activate:" + activate);
            return activate;
        }
        return 0;
	}
}
