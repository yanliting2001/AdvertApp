package com.grandartisans.advert.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.ljy.devring.other.RingLog;


/**
* TODO: log日志统计保存、上传-工具类
*
*/
 
public class LogcatHelper {
	private static String TAG = "LogcatHelper";
	private static LogcatHelper INSTANCE = null;
	private static String PATH_LOGCAT ;
	private LogDumper mLogDumper = null;
	private Context mContext;
	private int mPId;

	/**
	 * 初始化目录
	 * */
 
	public static void init(Context context)
	{ 
		StringBuffer LogPath = new StringBuffer();
		LogPath.append(Environment.getExternalStorageDirectory());
		LogPath.append("/Android/data/");
		LogPath.append(context.getPackageName()).append("/");
		LogPath.append("logs").append("/");
		PATH_LOGCAT = LogPath.toString();

		File file =new File(PATH_LOGCAT);
		if(!file.exists()){
			file.mkdirs();
		}
	}
 
	public static LogcatHelper getInstance(Context context)
	{
		if(INSTANCE == null){
			INSTANCE = new LogcatHelper(context);
		}
		return INSTANCE;
	}
 
	private LogcatHelper(Context context) {
		mContext = context;
		mPId = android.os.Process.myPid();
	}
 
	public void start() {
		if(mLogDumper==null){
			mLogDumper = new LogDumper(String.valueOf(mPId),PATH_LOGCAT);
			mLogDumper.start();
		}
 
	}
 
	public void stop()
	{
		if(mLogDumper!=null){
			mLogDumper.stopLogs();
			mLogDumper = null;
		}
	}

	public boolean isRunning(){
		if(mLogDumper!=null){
			return mLogDumper.isRunning();
		}
		return false;
	}
 
	public void sendLogMessageTask(final String url)
	{
		new Thread(new Runnable() {
			public void run() {
				sendLogMessage(url);
			}
		}).start();
	}
	public void sendLogMessage(String url)
	{
		if(mLogDumper!=null && mLogDumper.isLogFileLock()==false){
			mLogDumper.setLogFileLock(true);
			String file = mLogDumper.getLogFileName();
			File sendFile = new File(file);
			if(sendFile.exists() && sendFile.length()>2000){
				String zipFileName = mLogDumper.getZipFileName();
				File zipFile = new File(zipFileName);
				ArrayList<File> list=new ArrayList<File>();
				try{
					list.add(sendFile);
					ZipUtils.zipFiles(list, zipFile);
				}catch(Exception ex){
					ex.printStackTrace();
				}
				
				//doUpload(zipFile,url);
				uploadLogToOSS(zipFile,url);
				zipFile.delete();
			}
			mLogDumper.setLogFileLock(false);

			stop();
			start();
		}
	}
	
	public void reSetLogFile()
	{
		if(mLogDumper!=null){
			mLogDumper.setLogFileLock(true);
			String file = mLogDumper.getLogFileName();
			File newFile = new File(file);
			if(newFile.exists()) {
				newFile.delete();
			}
			try {
				newFile.createNewFile(); 
			} catch (IOException e) {
				e.printStackTrace();
			}
			mLogDumper.setLogFileLock(false);
		}
	}
 
	private class LogDumper extends Thread{
		String fileName;
		private Process logcatProc;
		private BufferedReader mReader = null;
		private boolean mRunning = false;
		String cmds=null;
		private final String mPID;
		private FileOutputStream out = null;
		private List<String> logsMessage = new ArrayList<String>();
		private boolean mLogFileLock = false; 
		private String logFileName;
		private String zipFileName = "logcat.zip";
		
		public void setLogFileLock(boolean lock){
			mLogFileLock = lock;
		}
 
		public boolean isLogFileLock()
		{
			return mLogFileLock;
		}
 
		public LogDumper(String pid,String file) {
			mPID = String.valueOf(pid);
			fileName = file;
			File mFile = new File(fileName,"logcat.txt");
			if(mFile.exists()) {
				mFile.delete();
			} else { 
				try { 
					mFile.createNewFile();
				} catch (IOException e) { 
					e.printStackTrace(); 
				}
			}
			try { 
				logFileName = mFile.toString(); 
				out = new FileOutputStream(mFile,true);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
 
			/**
			 
			* 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
			 
			* 显示当前mPID程序的 E和W等级的日志.
			 
			* */
			 
			//cmds ="logcat *:e *:w | grep \"("+mPID+")\"";
			cmds = "logcat -v time";
		}
 
		public String getLogFileName()
		{
			return logFileName; 
		}
		public String getZipFileName()
		{
			return PATH_LOGCAT + "/"+ zipFileName;
		}
 
		public void stopLogs() {
			mRunning = false;
		}
		public boolean isRunning(){
			return mRunning;
		}
 
		private boolean checkFileMaxSize(String file){
			File sizefile = new File(file);
			if(sizefile.exists()){
				//1.5MB
				if(sizefile.length()>15728640){
					return true;
				} 
				else {
					return false;
				}
			}else {
				return false;
			}
		}

		@Override
		public void run() {
			System.out.println("LogCatHelper cmds= " + cmds);
			mRunning = true;
			try {
				logcatProc = Runtime.getRuntime().exec(cmds);
				mReader = new BufferedReader(new InputStreamReader(
						logcatProc.getInputStream()), 1024);
				String line = null; 
				while (mRunning && (line = mReader.readLine()) != null) {
					if (!mRunning) {
						break;
					}
					if (line.length() == 0) {
						continue;
					}
					if(!line.contains(mPID)) {
						continue;
					}
					synchronized (out) {
						if (out != null) {
							boolean maxSize = checkFileMaxSize(getLogFileName()); 
							if(maxSize){ 
								//文件大小超过1.5mb
								//sendLogMessage(mContext);
								reSetLogFile();
							}
							if (isLogFileLock()) {
								logsMessage.add(line.getBytes() + "\n");
							} else {
								if(logsMessage.size()>0){
									for(String _log:logsMessage){
										out.write(_log.getBytes());
									}
									logsMessage.clear();
								}
								/**
								 * 再次过滤日志，筛选当前日志中有 mPID 则是当前程序的日志.
								 * */
								out.write(line.getBytes());
								out.write("\n".getBytes());
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} finally {
				if (logcatProc != null) {
					logcatProc.destroy();
					logcatProc = null;
				}
				if (mReader != null) {
					try {
						mReader.close(); 
						mReader = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(out!=null){
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					out = null;
				}
			}
		}
	}

	private void uploadLogToOSS(File zipFile,String END_POINT) {
		String ACCESS_KEY_ID = "LTAIvIhIJ3JNzkRl";
		String ACCESS_KEY_SECRET = "7aZBMS42QqguHTF5cq5uPD7tle8dK3";
		String BUCKET_NAME = "gadsp";
		String OBJECT_KEY_DIR = "advert/crash_log/";
		RingLog.d(TAG, "Start to upload zip");
		boolean uploadSuccess = false;
		String zipFilePath = mLogDumper.getZipFileName();
		// OSS初始化
		OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(ACCESS_KEY_ID, ACCESS_KEY_SECRET);
		ClientConfiguration conf = new ClientConfiguration();
		conf.setConnectionTimeout(15 * 1000);   // 连接超时，默认15秒
		conf.setSocketTimeout(15 * 1000);   // socket超时，默认15秒
		conf.setMaxConcurrentRequest(5);    // 最大并发请求数，默认5个
		conf.setMaxErrorRetry(5);   // 失败后最大重试次数，默认2次
		OSS oss = new OSSClient(mContext, END_POINT, credentialProvider, conf);

		// 上传文件
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		long timestamp = new Date().getTime();
		String date = CommonUtil.stampToDate(timestamp);
		String mDeviceId =  SystemInfoManager.getDeviceId(mContext);
		String objectKey = OBJECT_KEY_DIR + Integer.toString(year) + "/" + Integer.toString(month) + "/" + Integer.toString(day) + "/Logcat_" + mDeviceId + "_" + date + ".zip";
		PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, objectKey, zipFilePath);
		try {
			PutObjectResult putObjectResult = oss.putObject(put);
			Log.d(TAG, "PubObject: Upload Success");
			Log.d(TAG, "ETag: " + putObjectResult.getETag());
			Log.d(TAG, "RequestId: " + putObjectResult.getRequestId());
			uploadSuccess = true;
		} catch (ClientException e) {
			// 本地异常如网络异常等
			e.printStackTrace();
			try {
				Thread.sleep(60 * 1000);
				//uploadLogToOSS();
			} catch (Exception c) {
				Log.d(TAG, "Sleep exception");
			}
		} catch (ServiceException e) {
			// 服务异常
			Log.d(TAG, "RequestId is: " + e.getRequestId());
			Log.d(TAG, "ErrorCode is: " + e.getErrorCode());
			Log.d(TAG, "HostId is: " + e.getHostId());
			Log.d(TAG, "RawMessage is: " + e.getRawMessage());
		}
	}
}