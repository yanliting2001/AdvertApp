package com.grandartisans.advert.receiver;

import com.grandartisans.advert.activity.MediaPlayerActivity;
import com.grandartisans.advert.service.RemoteService;
import com.grandartisans.advert.service.UpgradeService;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppUpgradeReceiver extends BroadcastReceiver {

	private static final String TAG = "AppUpgradeReceiver";
	Context mContext = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "==++++++++++== AppUpgradeReceiver action : " + intent.getAction());
        String action = intent.getAction();
		if ("android.intent.action.PACKAGE_REPLACED".equals(action)) {
			String packageName = intent.getData().getSchemeSpecificPart();
			if(packageName.equals(Utils.getAppPackageName(context))) {
				/*
				Intent it = new Intent(context, MediaPlayerActivity.class);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(it);

				Intent intentService = new Intent(context,UpgradeService.class);
				context.startService(intentService);

				 */
				CommonUtil.reboot(context);
				//restartAPP(context);
				//System.exit(0);

			}else if(packageName.equals("com.tofu.locationinfo")){
                CommonUtil.reboot(context);
			}

		}
	}


	/**
	 * 重启整个APP
	 * @param context
	 */
	public static void restartAPP(Context context){
		Intent intent = context.getPackageManager()
				.getLaunchIntentForPackage(Utils.getAppPackageName(context));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

}
