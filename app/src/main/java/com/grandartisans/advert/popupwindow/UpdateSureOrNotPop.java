package com.grandartisans.advert.popupwindow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.DecodeImgUtil;

public class UpdateSureOrNotPop {
	
	private Context mContext;
	private PopupWindow SureOrNotPop;
	private View Container = null;
	private Button sureBtn;
	private Button cancelBtn;
	private IButtonClick mIButtonClick ;
	private TextView content;
	
	private String contentText = "";
	private String sureText = "";
	private String cancelText = "";
	private String type= "";
	public UpdateSureOrNotPop(Context mContext,String type,String contentText,String sureText,String cancelText){
		this.mContext = mContext;
		this.contentText = contentText;
		this.sureText = sureText;
		this.cancelText = cancelText;
		this.type = type;
	}
	
	public void showListPop(){
		if(SureOrNotPop==null){
			SureOrNotPop = initSureOrNotPop();
		}
		if(SureOrNotPop!=null&&SureOrNotPop.isShowing()){
			SureOrNotPop.dismiss();
		}else{
			SureOrNotPop.showAtLocation(Container, Gravity.BOTTOM, 0, 30);
		}
	}
	
	private PopupWindow initSureOrNotPop(){
		PopupWindow Pop = null;
		if(type.equals("screen")){
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			Container = layoutInflater.inflate(R.layout.system_update_pop_sure_cancel1, null);
		}else{
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		    Container = layoutInflater.inflate(R.layout.system_update_pop_sure_cancel, null);
		}
		LinearLayout main_ll = (LinearLayout) Container.findViewById(R.id.main_ll);
		Bitmap b = new DecodeImgUtil(mContext, R.raw.logout_dialog_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_ll.setBackground(bd);
		sureBtn = (Button) Container.findViewById(R.id.sure);
	    cancelBtn = (Button) Container.findViewById(R.id.cancel);
	    content = (TextView) Container.findViewById(R.id.content);
	    initView();
		initOnClick();
		initFoucs();
		Pop = new PopupWindow(Container, android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT, false);
		Pop.setBackgroundDrawable(new BitmapDrawable());//
		Pop.setOutsideTouchable(true);
		Pop.setFocusable(true);
		return Pop;
	}
	
	
	
	private void initOnClick(){
		sureBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mIButtonClick.onSureClick();
				SureOrNotPop.dismiss();
			}
		});
		
		cancelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mIButtonClick.onCancelClick();
				SureOrNotPop.dismiss();
			}
		});
	}
	
	public interface IButtonClick{
		void onSureClick();
		void onCancelClick();
	}
	
	public void setOnButtonClick(IButtonClick mIButtonClick){
		this.mIButtonClick = mIButtonClick;
	}
	private void initView(){
		content.setText(contentText);
		sureBtn.setText(sureText);
		cancelBtn.setText(cancelText);
	}
	public PopupWindow getPop(){
		return SureOrNotPop;
	}
	private void initFoucs(){
		sureBtn.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					//sureBtn.setBackgroundResource(R.drawable.btn_focus_bg);
					sureBtn.setTextColor(Color.parseColor("#FFFFFF"));
				}else{
					//sureBtn.setBackgroundResource(0);
					sureBtn.setTextColor(Color.parseColor("#4c5c6a"));
				}
			}
		});
		cancelBtn.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					//cancelBtn.setBackgroundResource(R.drawable.btn_focus_bg);
					cancelBtn.setTextColor(Color.parseColor("#FFFFFF"));
				}else{
					//cancelBtn.setBackgroundResource(0);
					cancelBtn.setTextColor(Color.parseColor("#4c5c6a"));
				}
			}
		});
	}
	
}
