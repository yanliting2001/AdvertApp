package com.grandartisans.advert.popupwindow;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.TextView;

import com.grandartisans.advert.R;

public class DialogCollection {
	
		private Context c;
		private Dialog d;
		private IButtonClick Ibc = null;
		public DialogCollection(Context c){
			this.c  = c ;
		}
		
	  public interface IButtonClick{
		  void OnOkClick();
		  void OnCancelClick();
	  }
		
	  public void setOnButtonClick(IButtonClick Ibc){
		  this.Ibc = Ibc;
	  }
	  public IButtonClick getButtonClick(){
		 return this.Ibc ;
	  }
	  
	  public Dialog showWarningDialog(String contentTitle,String contentStr,String okStr,String cancelStr){  //has two click button
			d = new Dialog(c, R.style.userDialog);
			
			Window dialogWindow = d.getWindow();
		    WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		    dialogWindow.setGravity(Gravity.CENTER);
		    d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			d.setContentView(R.layout.warning_dialog);
			
			TextView content = (TextView) d.findViewById(R.id.warningContent);
			final Button ok = (Button) d.findViewById(R.id.warningOk);
			final Button cancel = (Button) d.findViewById(R.id.warningCancel);
			TextView title = (TextView) d.findViewById(R.id.warningTitle);
			title.setText(contentTitle);
			content.setText(contentStr);
			ok.setText(okStr);
			cancel.setText(cancelStr);
			ok.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View arg0, boolean arg1) {
					// TODO Auto-generated method stub
					if(arg1){
						ok.setBackground(c.getResources().getDrawable(R.drawable.update_button_selected));
						ok.setTextColor(c.getResources().getColor(R.color.common_color_white));
					}else{
						ok.setBackgroundResource(0);
						ok.setTextColor(c.getResources().getColor(R.color.common_color_grey));
					}
				}
			});
			cancel.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View arg0, boolean arg1) {
					// TODO Auto-generated method stub
					if(arg1){
						cancel.setBackground(c.getResources().getDrawable(R.drawable.update_button_selected));
						cancel.setTextColor(c.getResources().getColor(R.color.common_color_white));
					}else{
						cancel.setBackgroundResource(0);
						cancel.setTextColor(c.getResources().getColor(R.color.common_color_grey));
					}
				}
			});
			ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					d.dismiss();
					if(Ibc!=null) Ibc.OnOkClick();
				}
			});
			cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					d.dismiss();
					if(Ibc!=null) Ibc.OnCancelClick();
				}
			});
			d.show();
			return d;
		}	
}
