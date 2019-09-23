package com.grandartisans.advert.view;

import com.grandartisans.advert.R;
import com.grandartisans.advert.interfaces.IDialogInterface;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SysDialog {
	private Dialog d;
	private Context context;
	private IDialogInterface dialogIf = null;

	public SysDialog(Context c) {
		this.context = c;
	}

	public void setInterface(IDialogInterface dialogIf) {
		this.dialogIf = dialogIf;
	}

	public Dialog showDialog1(String title, String message, String btnText1, String btnText2) {// 有两个按钮的提示框
		d = new Dialog(context, R.style.CommonDialog);

		Window dialogWindow = d.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER);
		d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

		d.setContentView(R.layout.dialog_common);

		TextView titletTextView = (TextView) d.findViewById(R.id.title);
		titletTextView.setText(title);
		TextView msgTextView = (TextView) d.findViewById(R.id.message);
		msgTextView.setText(message);
		Button btn1 = (Button) d.findViewById(R.id.positiveButton);
		btn1.setText(btnText1);
		btn1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
				if (dialogIf != null)
					dialogIf.onPositive();
			}
		});
		Button btn2 = (Button) d.findViewById(R.id.negativeButton);
		btn2.setText(btnText2);
		btn2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
				if (dialogIf != null)
					dialogIf.onNegative();
			}
		});

		d.show();
		return d;
	}

	public boolean isShow() {
		if (d != null && d.isShowing()) {
			return true;
		} else {
			return false;
		}
	}

	public Dialog showDialog2(String title, String message) {// 没按钮的提示框
		d = new Dialog(context, R.style.CommonDialog);
		Window dialogWindow = d.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER);
		d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

		d.setContentView(R.layout.dialog_nit_auto);

		TextView titletTextView = (TextView) d.findViewById(R.id.title);
		titletTextView.setText(title);
		TextView msgTextView = (TextView) d.findViewById(R.id.message);
		msgTextView.setText(message);

		d.show();
		return d;
	}

	public Dialog showDialog3(String message, String btnText1) {// 一个按钮的提示框
		d = new Dialog(context, R.style.CommonDialog);
		Window dialogWindow = d.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER);
		d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

		d.setContentView(R.layout.dialog_notify);

		TextView msgTextView = (TextView) d.findViewById(R.id.message);
		msgTextView.setText(message);
		Button btn1 = (Button) d.findViewById(R.id.positiveButton);
		btn1.setText(btnText1);
		btn1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
				if (dialogIf != null)
					dialogIf.onPositive();
			}
		});

		d.show();
		return d;
	}

	public void closeDialog() {
		if (d != null) {
			d.dismiss();
		}
	}

}
