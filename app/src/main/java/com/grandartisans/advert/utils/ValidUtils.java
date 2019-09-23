package com.grandartisans.advert.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @类描述：校验工具类
 * @项目名称：go3cLauncher
 * @包名： com.go3c.utils
 * @类名称：ValidUtils
 * @创建人：gao
 * @创建时间：2014年6月12日上午11:34:54
 * @修改人：gao
 * @修改时间：2014年6月12日上午11:34:54
 * @修改备注：
 * @version v1.0
 * @see [nothing]
 * @bug [nothing]
 * @Copyright go3c
 * @mail 25550836@qq.com
 */
public class ValidUtils {

	/**
	 * 
	 * @描述:判断是否是允许后台运行或开机启动的应用
	 * @方法名: validIsBoot
	 * @param path
	 * @param packagename
	 * @return
	 * @返回类型 boolean
	 * @创建人 gao
	 * @创建时间 2014年7月21日下午4:06:28
	 * @修改人 gao
	 * @修改时间 2014年7月21日下午4:06:28
	 * @修改备注
	 * @since
	 * @throws
	 */
	public static boolean validIsBoot(String path, String packagename) {
		boolean b = false;
		/*
		File file = new File(path);
		if (file.exists()) {
			try {
				StringBuilder json = FileUtils.convertStreamToString(path);
				Type type = new TypeToken<List<App>>() {
				}.getType();
				Gson gson = new Gson();
				List<App> apps = gson.fromJson(json.toString(), type);

				for (App app : apps) {
					if (packagename.equals(app.getPackageName())) {
						// 控制指定应用进行开机启动
						b = true;
						break;
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		*/
		return b;
	}

	/**
	 * 
	 * @描述:判断应用是否正在运行
	 * @方法名: validIsRunning
	 * @param packageName
	 * @return
	 * @返回类型 boolean
	 * @创建人 gao
	 * @创建时间 2014年7月21日下午3:57:21
	 * @修改人 gao
	 * @修改时间 2014年7月21日下午3:57:21
	 * @修改备注
	 * @since
	 * @throws
	 */
	public static boolean validIsRunning(Context context, String packageName) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> l = am.getRunningAppProcesses();
		boolean isAppRunning = false;
		for (RunningAppProcessInfo runningAppProcessInfo : l) {
			if (runningAppProcessInfo.processName.equals(packageName)) {
				isAppRunning = true;
				break;
			}
		}

		return isAppRunning;
	}

	/**
	 * 
	 * @描述:判断有线是否连接
	 * @方法名: checkEthernet
	 * @param context
	 * @return
	 * @返回类型 boolean
	 * @创建人 gao
	 * @创建时间 2014年7月14日下午3:45:50
	 * @修改人 gao
	 * @修改时间 2014年7月14日下午3:45:50
	 * @修改备注
	 * @since
	 * @throws
	 */
	public static boolean checkEthernet(Context context) {
		if (context != null) {
			ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
			return networkInfo.isConnected();
		}
		return false;
	}

	/**
	 * 
	 * @描述:判断WIFI是否连接
	 * @方法名: checkWifi
	 * @param context
	 * @return
	 * @返回类型 boolean
	 * @创建人 gao
	 * @创建时间 2014年7月14日下午3:46:04
	 * @修改人 gao
	 * @修改时间 2014年7月14日下午3:46:04
	 * @修改备注
	 * @since
	 * @throws
	 */
	public static boolean checkWifi(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 
	 * @描述:判断当前是否连接网络（这里的网络指的是外网）
	 * @方法名: isInternetConnected
	 * @return
	 * @返回类型 boolean
	 * @创建人 gao
	 * @创建时间 2014年9月15日上午11:50:28
	 * @修改人 gao
	 * @修改时间 2014年9月15日上午11:50:28
	 * @修改备注
	 * @since
	 * @throws
	 */
	public static boolean isInternetConnected(String urlStr) {
		boolean result = false;
		try {
			// froyo之前的系统使用httpurlconnection存在bug
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
				System.setProperty("http.keepAlive", "false");
			}

			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);// 设置为允许输出
			conn.setConnectTimeout(3 * 1000);// 3S超时

			Map<String, List<String>> headerMap = conn.getHeaderFields();
			Iterator<String> iterator = headerMap.keySet().iterator();
			while (iterator.hasNext()) {
				result = true;
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * @描述:判断网线/无线是否连接（这里不确定是否有外网）
	 * @方法名: isNetworkExist
	 * @param context
	 * @return
	 * @返回类型 boolean
	 * @创建人 gao
	 * @创建时间 2014年8月1日下午12:11:03
	 * @修改人 gao
	 * @修改时间 2014年8月1日下午12:11:03
	 * @修改备注
	 * @since
	 * @throws
	 */
	public static boolean isNetworkExist(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 
	 * @描述:没连接网络的弹出提示框
	 * @方法名: validnetwork
	 * @param mContext
	 * @返回类型 void
	 * @创建人 gao
	 * @创建时间 2014年6月12日上午11:34:07
	 * @修改人 gao
	 * @修改时间 2014年6月12日上午11:34:07
	 * @修改备注
	 * @since
	 * @throws
	 */
	/*
	public static SysDialog validnetwork(final Context mContext, String title, String message, String btnText1, String btnText2) {
		Go3cApplication app = (Go3cApplication) mContext.getApplicationContext();
		SysDialog s = new SysDialog(app);
		s.setInterface(new IDialogInterface() {
			@Override
			public void onPositive() {
				startSettings(mContext);
			}

			@Override
			public void onNegative() {
			}
		});
		s.showDialog1(title, message, btnText1, btnText2);

		return s;
	}
	*/
	/**
	 * 
	 * @描述:打开网络设置
	 * @方法名: startSettings
	 * @param context
	 * @返回类型 void
	 * @创建人 gao
	 * @创建时间 2014年6月12日上午11:33:54
	 * @修改人 gao
	 * @修改时间 2014年6月12日上午11:33:54
	 * @修改备注
	 * @since
	 * @throws
	 */
	private static void startSettings(Context context) {
		/*
		Intent it = new Intent();
		it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		it.setClass(context, NetworkSettingsActivity.class);
		context.startActivity(it);
		*/
	}
}