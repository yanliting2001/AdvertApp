package com.grandartisans.advert.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.content.ComponentName;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.grandartisans.advert.R;
import com.grandartisans.advert.activity.SystemUpdateSettings;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.entity.UpgradeBean;
import com.grandartisans.advert.popupwindow.DialogCollection;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.Contacts;
import com.grandartisans.advert.utils.MD5;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.other.RingLog;

public class DownLoadService extends Service{

	private static final int DOWNLOAD_FAILED = 10;
	private static final int DOUPGRADE = 11;
	private static final int HIDEUPGRADEDIALOG = 12;
	private Context mContext;
	private String ServerMD5;
	private String ServerDownLoadUrl;
	private DialogCollection mDialogCollection;
	private Dialog d = null;
	private int forceUpdate = 0;
	AsyncTask<String, Integer, File> mDownLoad = null;
	private TimerTask updatechecktask;
	private int manualupdate = 0; 
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case DOWNLOAD_FAILED: //连接超时
				//SystemUpdateSettings.isUpdating = false;
				if(mDownLoad == null){
					mDownLoad = new DownLoadFirmware();
					if(mDownLoad.getStatus()==AsyncTask.Status.PENDING){
						mDownLoad.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ServerDownLoadUrl);
					}
				}
				break;
			case DOUPGRADE:
				doUpgrade();
				break;
			case HIDEUPGRADEDIALOG:
				hideUpgradeDialog();
				break;
			default:
				break;
			}
		};
	};
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d("DownloadService", "onCreate");
		CheckUpgrade(30*60);	
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d("DownloadService", "onStartCommand");
 		if(intent!=null) {
			manualupdate = intent.getIntExtra("manualupdate", 0);
			mContext = getApplicationContext();
			mDialogCollection = new DialogCollection(mContext);
			GetRomUpgradeInfo();
			
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private class DownLoadFirmware extends AsyncTask<String, Integer, File>{

		
		private int Total = 1;
		@Override
		protected File doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			try {
				SystemUpdateSettings.isUpdating = true;
				String firmwareUrl = arg0[0];
				long startPosition = 0;
				File file = new File(Contacts.DOWNLOAD_FILE_PATH_CACHE, Contacts.DEFAULT_DOWNLOAD_FILENAME_TEMP);
				RandomAccessFile rFile = null;
				URL url = new URL(firmwareUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000); 
				conn.setRequestMethod("GET");  
				conn.setRequestProperty("Accept-Encoding", "identity");
				if(file.exists()){
					FileInputStream fis = new FileInputStream(file);
					startPosition = fis.available();
					Log.d("DownloadService", "file is exits startPosition = " + startPosition);
					//文件下载位置   规定的格式 “byte=xxxx-”  
					String start = "bytes="+startPosition + "-";  
					//设置文件开始的下载位置  使用 Range字段设置断点续传
					conn.setRequestProperty("Range", start);  
					rFile=new RandomAccessFile(file,"rw"); 
					rFile.seek(startPosition);
				}else{
					rFile=new RandomAccessFile(file,"rw");
					rFile.seek(0);
				}
			
				int length = conn.getContentLength();// 主要这里耗时间
				Log.e("DownloadService", "getContentLength length = " + length);
				if(length <=0){
					Log.e("DownloadService", "getContentLength error length = " + length);
					if(length == 0) {
						deleteUpdateFile();
					}
					return null;
				}
				
				Total = (length+(int)startPosition) / 100;
				InputStream in = conn.getInputStream();

				//FileOutputStream fos = new FileOutputStream(rFile);
				byte[] buffer = new byte[1024];
				int len = 0;
				int completed = (int) startPosition;
				while ((len = in.read(buffer)) > 0 && getStatus() == Status.RUNNING) {
					completed += len;
					rFile.write(buffer, 0, len);
					onProgressUpdate(completed / Total);
				}
				in.close();
				rFile.close();
				return file;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//cancel(true);
				mDownLoad = null;
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(File result) {
			// TODO Auto-generated method stub
			
			//下载完成
			if (getStatus() == Status.RUNNING && result != null) {
				RenameTempDownloadFile();
				if(isLocalExist()){
					showUpdateDialog();
				}else{
					Toast.makeText(mContext, "md5错误或文件不存在", Toast.LENGTH_SHORT).show();
					//result.deleteOnExit();
					deleteUpdateFile();
					mHandler.sendEmptyMessageDelayed(DOWNLOAD_FAILED, 5 * 1000);
					mDownLoad = null;
				}
			}else {
				mDownLoad = null;
				cancel(true);
				mHandler.sendEmptyMessageDelayed(DOWNLOAD_FAILED, 5 * 1000);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			int newProgress = values[0];
			if(newProgress <= 100){
				
				sendBroadCast(newProgress);
			}else if(newProgress > 100){
				//下载完毕
			}
			
			super.onProgressUpdate(values);
		}
		
	}
	
	
	private void GetRomUpgradeInfo(){
		forceUpdate = 0;
		String rom_version = CommonUtil.getVersionInfo();
		String rmac = CommonUtil.getEthernetMac();
		String strguid = CommonUtil.getDeviceId();
		String hwver = CommonUtil.getModel();
		AdvertModel mIModel = new AdvertModel();
		DevRing.httpManager().commonRequest(mIModel.systemUpgrade(rom_version,strguid,hwver,"","",""), new CommonObserver<UpgradeBean>() {
			@Override
			public void onResult(UpgradeBean upgradetbean) {
				RingLog.d("system upgrade check ok status = " + upgradetbean.getResult().getCode());
				try {
					if(upgradetbean!=null){
						System.out.println("result = " + upgradetbean.getResult().getRet() + upgradetbean.getData().getDownload_link());
						if(upgradetbean.getResult().getRet()==0 && upgradetbean.getData().getDownload_link()!=null && upgradetbean.getData().getDownload_link().length()>0){
							ServerMD5 = upgradetbean.getData().getMd5();
							ServerDownLoadUrl = upgradetbean.getData().getDownload_link();
							forceUpdate = upgradetbean.getData().getForce();
							if(isLocalExist()){ //已存在，直接提示升级
								sendBroadCast(100);
								showUpdateDialog();
							}else{//需静默安装
								if(mDownLoad == null){
									ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
									ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
									String packageName = cn.getPackageName();
									if(manualupdate !=0 || forceUpdate!=0 || packageName.equals("com.projector.guide")) {
										if(forceUpdate!=0 && !packageName.equals("com.projector.guide")) showDialogBeforeUpdate();
										mDownLoad = new DownLoadFirmware();
										if(mDownLoad.getStatus()==AsyncTask.Status.PENDING){
											mDownLoad.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ServerDownLoadUrl);
										}
									}else{
										showUpgradeNotifyDialog();
									}
								}
							}
						}else{
							//GetRomUpgradeInfo1();
							if(isUpgradeFileExist()){ //已存在升级文件，则删除
								deleteUpdateFile();
							}
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onError(int i, String s) {
				RingLog.d("system upgrade check error i = " + i + "msg = " + s );
			}
		},null);
	}
	
	private boolean RenameTempDownloadFile() {
		File f =new File(Contacts.DOWNLOAD_FILE_PATH_CACHE + Contacts.DEFAULT_DOWNLOAD_FILENAME_TEMP);
		File file =new File(Contacts.DOWNLOAD_FILE_PATH_CACHE + Contacts.DEFAULT_DOWNLOAD_FILENAME);
		if(f.exists()) f.renameTo(file);
		return true;
	}

	private boolean isUpgradeFileExist(){
		File f =new File(Contacts.DOWNLOAD_FILE_PATH_CACHE + Contacts.DEFAULT_DOWNLOAD_FILENAME);
		if(f.exists()){
			return true;
		}
		return false;
	}

	private boolean isLocalExist(){
		File f =new File(Contacts.DOWNLOAD_FILE_PATH_CACHE + Contacts.DEFAULT_DOWNLOAD_FILENAME);
		if(f.exists()){
			MD5 mMD5 = new MD5();
			String localMD5 = mMD5.md5sum(Contacts.DOWNLOAD_FILE_PATH_CACHE + Contacts.DEFAULT_DOWNLOAD_FILENAME);
			if(localMD5!=null && localMD5.equals(ServerMD5)){//最新版本已下载，返回true，提示升级
				return true;
			}else {
				deleteUpdateFile();
			}
		}
		return false;
	}
	private void deleteUpdateFile() {
		File f =new File(Contacts.DOWNLOAD_FILE_PATH_CACHE + Contacts.DEFAULT_DOWNLOAD_FILENAME);
		if(f.exists()){
			f.delete();
		}
	}
	private void showUpgradeNotifyDialog() {
	if(d==null || d.isShowing()==false) {
    		d = new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.update_dialog_title)).
        	setMessage( mContext.getString(R.string.update_dialog_content)).setPositiveButton
        	(mContext.getString(R.string.update_dialog_noupdate_text),new DialogInterface.OnClickListener() {
                	@Override
                	public void onClick(DialogInterface arg0, int arg1) {
                        	// TODO Auto-generated method stub
				if(updatechecktask!=null) {
                			updatechecktask.cancel();
                			updatechecktask = null;
				}
                	}
        	}
        	).setNegativeButton( mContext.getString(R.string.update_dialog_update_text), new DialogInterface.OnClickListener() {
                	@Override
                	public void onClick(DialogInterface arg0, int arg1) {
                        	// TODO Auto-generated method stub
                		mHandler.sendEmptyMessageDelayed(DOWNLOAD_FAILED, 5 * 1000);
                	}
        	}).create();
    		int winType=0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
				winType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
			}else {
				winType =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			}
    		d.getWindow().setType(winType);
    		d.show();
		}
	}
	
	private void showUpdateDialog(){
		ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String packageName = cn.getPackageName();
		if(packageName.equals("com.projector.guide")) {
			Intent i = new Intent();
			i.setAction("com.txbox.downloadFinished");
			i.putExtra("progress", 100);
			mContext.sendBroadcast(i);
        }else if(forceUpdate !=0){
		if(d==null || d.isShowing()==false) {
        		mHandler.sendEmptyMessageDelayed(DOUPGRADE, 20*1000);
        	d = new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.update_dialog_title)).
        	setMessage( mContext.getString(R.string.update_dialog_forceupdate)).
            setNegativeButton( mContext.getString(R.string.update_dialog_update_text), new DialogInterface.OnClickListener() {
            	@Override
            	public void onClick(DialogInterface arg0, int arg1) {
            		// TODO Auto-generated method stub
            		doUpgrade();
            	}
            }).create();
			int winType=0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
				winType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
			}else {
				winType =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			}
			d.getWindow().setType(winType);
        	d.show();
		}
        }else {
		if(d==null || d.isShowing()==false) {
        		d = new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.update_dialog_title)).
            		setMessage( mContext.getString(R.string.update_dialog_reboot)).setPositiveButton
            (mContext.getString(R.string.update_dialog_noupdate_text),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
				if(updatechecktask!=null) {
					updatechecktask.cancel();
                    			updatechecktask = null;
				}
                    	}
            		}
            ).setNegativeButton( mContext.getString(R.string.update_dialog_update_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                            // TODO Auto-generated method stub
                            doUpgrade();
                    }
            }).create();
			int winType=0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
				winType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
			}else {
				winType =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			}
			d.getWindow().setType(winType);
        	d.show();
		}
        }
	}
	
	@SuppressLint("NewApi") 
	private void sendBroadCast(int progress){
		Intent i = new Intent();
		i.setAction("com.txbox.updateProgress");
		i.putExtra("progress", progress);
		mContext.sendBroadcast(i);
	}
	private void doUpgrade(){
		/*
    	Recovery.saveCommand(mContext, Contacts.DOWNLOAD_FILE_PATH_CACHE + Contacts.DEFAULT_DOWNLOAD_FILENAME);
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        pm.reboot("recovery");
		*/

		Intent intent = new Intent("com.android.56iq.otaupgrade");
		intent.putExtra("path",Contacts.ROM_UPDATE_PATH + Contacts.DEFAULT_DOWNLOAD_FILENAME);//otaPath为ota包所在的本地路径
		sendBroadcast(intent);

		//CommonUtil.runCmd("mv " + Contacts.DOWNLOAD_FILE_PATH_CACHE + Contacts.DEFAULT_DOWNLOAD_FILENAME + " " + Contacts.ROM_UPDATE_PATH);
		//CommonUtil.reboot(mContext);
	}
	private void CheckUpgrade(final int second){
		if(updatechecktask == null){
			updatechecktask = new TimerTask() {
				public void run() {
				 	Log.d("DownloadService", "CheckUpgrade");	
					GetRomUpgradeInfo();
				}
			};
			Timer timer = new Timer();
			timer.schedule(updatechecktask, 60*1000, second* 1000);
		}
	}
	private void showDialogBeforeUpdate(){
		mHandler.sendEmptyMessageDelayed(HIDEUPGRADEDIALOG, 20*1000);
		if(d==null || d.isShowing()==false) {
    			d = new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.update_dialog_title)).
    			setMessage( mContext.getString(R.string.update_dialog_beforeupdate)).
        		setNegativeButton( mContext.getString(R.string.logined_dialog_ok), new DialogInterface.OnClickListener() {
        		@Override
        		public void onClick(DialogInterface arg0, int arg1) {
        			// TODO Auto-generated method stub
        		}
        		}).create();
				int winType=0;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
					winType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
				}else {
					winType =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
				}
    			d.getWindow().setType(winType);
    			d.show();
		}
	}
	private void hideUpgradeDialog() {
		if(d!=null && d.isShowing()) {
			d.dismiss();
			d = null;
		}
	}
}
