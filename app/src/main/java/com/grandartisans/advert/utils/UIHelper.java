package com.grandartisans.advert.utils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.grandartisans.advert.R;

public class UIHelper {
	private static Toast toast = null;
	private static AlertDialog mloadingDataDialog;
	private static Toast mytoast = null;
	private static Toast shorttoast = null;

	public static void displayToast(String text, Context context) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void displayToast(String text, Context context, int times) {
		Toast.makeText(context, text, times).show();
	}


	public static void showToast(Context context, String message) {
		if (toast == null) {
			toast = Toast.makeText(context.getApplicationContext(), message,
					Toast.LENGTH_SHORT);
		} else {
			toast.setText(message);
		}
		toast.show();
	}
	private static Activity thisact;
	public static void ErrorDialog(Context context, String message) {
		Builder errordialog = new Builder(context);
		errordialog.setTitle(R.string.system_tips);
		errordialog.setMessage(message);
		errordialog.setPositiveButton(R.string.confirm,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		AlertDialog dialog = errordialog.create();
		dialog.getWindow()
				.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}
	public static void ErrorDialogFinish(Context context, String message) {
		thisact = (Activity) context;
		Builder errordialog = new Builder(context);
		errordialog.setTitle(R.string.system_tips);
		errordialog.setMessage(message);
		errordialog.setPositiveButton(R.string.confirm,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						thisact.finish();
					}
				});
		AlertDialog dialog = errordialog.create();
		dialog.getWindow()
				.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}
	public static void showLoadingDialog(Context context, String message) {
		Builder builder = new Builder(context);
		final View viewdialog = LayoutInflater.from(context).inflate(
				R.layout.loading_data_progress, null);
		TextView messageview = (TextView) viewdialog.findViewById(R.id.message);
		messageview.setText(message);
		builder.setView(viewdialog);
		mloadingDataDialog = builder.create();
		mloadingDataDialog.getWindow().setType(
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		mloadingDataDialog.show();
	}

	public static void clearLoadingDialog() {
		if (mloadingDataDialog != null && mloadingDataDialog.isShowing())
			mloadingDataDialog.dismiss();
	}
	public static void showMyToast(Context mContext,String info) {
		if(mytoast==null){
			LayoutInflater inflater = LayoutInflater.from(mContext);
			View toastRoot = inflater.inflate(R.layout.menu_toast, null);
			TextView message = (TextView) toastRoot.findViewById(R.id.message);
			message.setText(info);
			mytoast = new Toast(mContext);
			mytoast.setGravity(Gravity.BOTTOM, 0, 0);
			mytoast.setDuration(Toast.LENGTH_SHORT);
			mytoast.setView(toastRoot);
		}else{
			//mytoast.setText(info);
			TextView message = (TextView) mytoast.getView().findViewById(R.id.message);
			message.setText(info);
		}
		mytoast.show();
	}
	public static void cancelMyToast(){
		if(mytoast!=null){
			mytoast.cancel();
		}
	}
	public static void showShortToast(Context mContext,String info) {
		if(shorttoast==null){
			LayoutInflater inflater = LayoutInflater.from(mContext);
			View toastRoot = inflater.inflate(R.layout.short_toast, null);
			TextView message = (TextView) toastRoot.findViewById(R.id.message);
			message.setText(info);
			shorttoast = new Toast(mContext);
			shorttoast.setGravity(Gravity.BOTTOM, 0, 120);
			shorttoast.setDuration(Toast.LENGTH_SHORT);
			shorttoast.setView(toastRoot);
		}else{
			//mytoast.setText(info);
			TextView message = (TextView) shorttoast.getView().findViewById(R.id.message);
			message.setText(info);
		}
		shorttoast.show();
	}
}
