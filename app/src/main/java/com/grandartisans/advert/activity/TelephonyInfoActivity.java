package com.grandartisans.advert.activity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.grandartisans.advert.utils.TelephonyUtils;

public class TelephonyInfoActivity extends Activity implements OnClickListener{
	private static final String TAG = "TelephonyInfoActivity";
	private EditText edit_ip;
	private EditText edit_yanma;
	private EditText edit_wangguan;
	private EditText edit_dns;
	private Button submit;
	private Button ignore;
	private static final String WHITE = "#FFFFFF";
	private static final String LBULE = "#97FFFF";
	private static final int NORMAL_FRONT_SIZE = 24;
	private static final int BIGGER_FRONT_SIZE = 26;
	boolean isFirst = true;
	private String type;
	private Bitmap b;
	private RelativeLayout main_rl;
	private Context mContext;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.telephonyinfo_activity);
		mContext = this;
		main_rl = (RelativeLayout) findViewById(R.id.main_rl);
		b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_rl.setBackground(bd);
		type = getIntent().getStringExtra("type");
		initView();
		ignore.setVisibility(View.GONE);
		initData();
		initOnClick();
	}
	
	private void initView(){
		edit_ip = (EditText) findViewById(R.id.edit_networkname);
		edit_yanma = (EditText) findViewById(R.id.edit_phonetype);
		edit_wangguan = (EditText) findViewById(R.id.edit_networktype);
		edit_dns = (EditText) findViewById(R.id.edit_simstate);
		submit = (Button) findViewById(R.id.network_detial_submit);
		
		ignore = (Button) findViewById(R.id.network_detial_ignore);
		
	}
	
	private void initData(){
		String name = TelephonyUtils.getNetworkName(mContext);
		String phonetype = TelephonyUtils.getPhoneType(mContext);
		String networktype = TelephonyUtils.getNetworkType(mContext);
		String simstate = TelephonyUtils.getSimState(mContext);
		
		edit_ip.setText(name);
		edit_yanma.setText(phonetype); 
		edit_wangguan.setText(networktype); 
		edit_dns.setText(simstate); 
		
	}
	private void initOnClick(){
		submit.setOnClickListener(this);
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
		return super.onKeyDown(arg0, arg1);
	}
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(b!=null) b.recycle();
		super.onDestroy();
	}
	
	
}
