package com.grandartisans.advert.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.grandartisans.advert.utils.ScaleAnimEffect;

public class ScreenSettingsActivity extends Activity{
	
	
	private LinearLayout screen_ratio_settingsTip_ll,screen_size_settingsTip_ll,screen_position_settingsTip_ll,next_ll;
	private ScaleAnimEffect animEffect;
	private float scaleX = 1.225f;
	private float scaleY = 1.225f;
	private TextView next_text;
	private RelativeLayout main_rl;
	private Bitmap b;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_screen_settings);
		main_rl = (RelativeLayout) findViewById(R.id.main_rl);
		b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_rl.setBackground(bd);
		animEffect = new ScaleAnimEffect();
		Intent intent = getIntent();
		String type = intent.getStringExtra("type");
		initView(type);
		initOnClick();
		initOnfocusChange();
		//screen_ratio_settingsTip_ll.requestFocus();
	}
	
	private void initView(String type){
		screen_ratio_settingsTip_ll = (LinearLayout) findViewById(R.id.screen_ratio_settingsTip_ll);
		screen_size_settingsTip_ll = (LinearLayout) findViewById(R.id.screen_size_settingsTip_ll);
		screen_position_settingsTip_ll = (LinearLayout) findViewById(R.id.screen_position_settingsTip_ll);
		next_ll = (LinearLayout) findViewById(R.id.next_ll);
		next_text = (TextView) findViewById(R.id.next_text);
		if(type.equals("guide")){
			next_ll.setVisibility(View.VISIBLE);
		}else{
			next_ll.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
    protected void onDestroy() {
		// TODO Auto-generated method stub
        super.onDestroy();
        if(b!=null) b.recycle();
    }
	
	private void startCheckActive(){
		Intent i = new Intent();
		i.setClass(this, EnterBoxActivity.class);
		startActivity(i);
		//overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}
	private void startRatioSetting(){
		Intent i = new Intent();
		i.setClass(this, ScreenPositionSettingActivity.class);
		i.putExtra("type","focus");
		startActivity(i);
		//overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}
	private void startSizeSetting(){
		Intent i = new Intent();
		i.setClass(this, ScreenPositionSettingActivity.class);
		i.putExtra("type","size");
		startActivity(i);
		//overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}
	private void startPositionSetting(){
		Intent i = new Intent();
		i.putExtra("type","position");
		i.setClass(this, ScreenPositionSettingActivity.class);
		startActivity(i);
		//overridePendingTransition(R.anim.zoout, R.anim.zoin);
	}
	private void initOnClick(){
		next_ll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startCheckActive();
			}
		});
		screen_ratio_settingsTip_ll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startRatioSetting();
			}
		});
		screen_size_settingsTip_ll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startSizeSetting();
			}
		});
		screen_position_settingsTip_ll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startPositionSetting();
			}
		});
	}
	/**
	 * 
	 * @描述: hasFocus:startAnim,otherwise looseAnim
	 * @方法名: initOnfocusChange
	 * @返回类型 void
	 * @创建人 huang
	 * @创建时间 Sep 3, 201410:50:12 AM	
	 * @修改人 huang
	 * @修改时间 Sep 3, 201410:50:12 AM	
	 * @修改备注 
	 * @since
	 * @throws
	 */
	private void initOnfocusChange(){
		screen_ratio_settingsTip_ll.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					startAnimation(v);
				}else{
					looseAnimation(v);
				}
			}
		});
		screen_size_settingsTip_ll.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					startAnimation(v);
				}else{
					looseAnimation(v);
				}
			}
		});
		screen_position_settingsTip_ll.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					startAnimation(v);
				}else{
					looseAnimation(v);
				}
			}
		});
		next_ll.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					//next_ll.setBackgroundResource(R.drawable.btn_focus_bg);
					next_text.setTextColor(Color.parseColor("#FFFFFF"));
				}else{
					//next_ll.setBackgroundResource(0);
					next_text.setTextColor(Color.parseColor("#4c5c6a"));
				}
			}
		});
	}
	
	private void startAnimation(final View view){
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
				//view.setBackgroundResource(R.drawable.login_focus_bg);
				view.setAlpha(1.0f);
			}
		});
		view.startAnimation(localAnimation);
	}
	private void looseAnimation(final View view){
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
				//view.setBackgroundResource(R.drawable.login_default_bg);
				view.setAlpha(0.5f);
			}
		});
		view.startAnimation(localAnimation);
	}
	
}
