package com.grandartisans.advert.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.Surface;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.grandartisans.advert.activity.MediaPlayerActivity;
import com.grandartisans.advert.interfaces.RecorderEventListener;
import com.grandartisans.advert.recoder.RecorderManager;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.ljy.devring.other.RingLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

public class CameraService extends Service  {
    private RecorderManager mRecorderManager = null;
    private Camera mCamera;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private String deviceId;
    private String recordPath;
    private String mYear;
    private String mMonth;
    private String mDay;

    private int mTimerCount = 0;

    private boolean isRecording = false;
    private boolean isRecordPaused  = false;
    private boolean isUploading = false;
    private boolean uploadSuccess = false;
    public static boolean cameraNeedStop = false;

    private static final String TAG = CameraService.class.getSimpleName();
    private static final String RTMP_CHANNEL = "rtmp://119.23.28.204:1935/live";
    private static final String END_POINT = "http://oss-cn-shenzhen.aliyuncs.com";
    private static final String ACCESS_KEY_ID = "LTAIvIhIJ3JNzkRl";
    private static final String ACCESS_KEY_SECRET = "7aZBMS42QqguHTF5cq5uPD7tle8dK3";
    private static final String BUCKET_NAME = "gadsp";
    private static final String OBJECT_KEY_DIR = "advert/record/";

    private List<String> recordList = null;
    private int recordSegment = 0;

    private static final int START_RTMP = 100000;
    //private static final int START_RECORD = 100001;
    private static final int UPLOAD_FILE = 100002;
    private static final int RESTART_RECORD = 100003;

    private boolean haveUdisk = false;

    private CamBinder mCamBinder = new CamBinder();
    private Handler handler;

    private RecorderEventListener mRecorderEventListener = null;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message paramMessage) {
            switch (paramMessage.what) {
                    /*
                case START_RECORD:
                    startCameraRecord();
                    break;
                    */
                case UPLOAD_FILE:
                    uploadRecord();
                    break;
            }
            super.handleMessage(paramMessage);
        }
    };

    public CameraService () {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return this.mCamBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        recordList = new LinkedList<String>();
        mRecorderManager = mRecorderManager = RecorderManager.getInstance(this);
        if(CommonUtil.haveUdisk()){
            recordPath = "/storage/udisk0/";
        }else {
            recordPath = getApplicationContext().getExternalFilesDir(null).getPath();
            removeOlderFiles(recordPath,false);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public class CamBinder extends Binder {
        public CameraService getService() {
            return CameraService.this;
        }
    }

    public void  registerListener(RecorderEventListener listener){
        if(listener!=null) mRecorderEventListener = listener;
    }
    public int getCameraNumber(){
        return mRecorderManager.getCameraNumber();
    }

    public void cameraRecordStart(Surface surface) {
        RingLog.d(TAG, "Open Record Camera");
        mRecorderManager.initialize();
        recordList.clear();
        recordSegment = 0;
        String fileName = getRecordName(recordSegment);
        recordList.add(fileName);
        mRecorderManager.recordStart(surface,fileName);
        isRecording = true;
        mTimerCount = 0;
        startPublishRecordTimer();
        if(mRecorderEventListener!=null)
            mRecorderEventListener.onRecordStart();
    }

    public void cameraRecordStop(){
        mRecorderManager.recordStop();
        RingLog.d(TAG, "Record finished");
        mTimerCount = 0;

        if(CommonUtil.haveUdisk()) {
            mHandler.sendEmptyMessageDelayed(RESTART_RECORD,3*1000);
        }else {
            destroyTimer();
        }
    }
    public void cameraRecordPause(){
        RingLog.d(TAG, "Record paused");
        //mTimer.cancel();
        isRecordPaused = true;
        mRecorderManager.recordStop();
    }
    public void cameraRecordResume(Surface surface){
        mRecorderManager.initialize();
        recordSegment ++ ;
        String fileName = getRecordName(recordSegment);
        recordList.add(fileName);
        mRecorderManager.recordStart(surface,fileName);
        isRecordPaused = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    private String getRecordName(int segnum){
        String fileName = "";
        if(CommonUtil.haveUdisk()) removeOlderFiles("/storage/udisk0",true);
        deviceId = SystemInfoManager.readFromNandkey("usid");
        if (deviceId == null) {
            deviceId = "G50234001485210002";
        }
        deviceId=deviceId.toUpperCase();
        if(CommonUtil.haveUdisk()) {
            fileName = recordPath + deviceId + getFileName() +"segment_" + segnum + ".mp4";
        }else {
            fileName = recordPath + "/" + deviceId +"segment_" + segnum+ ".mp4";
        }
        return fileName;
    }
    private void ComposerVideo() {
        ArrayList<EpVideo> epVideos = new ArrayList<>();
        int size = recordList.size();
        for(int i=0;i<size;i++) {
            String fileName = recordList.get(i);
            epVideos.add(new EpVideo(fileName));//视频
        }
        //输出选项，参数为输出文件路径(目前仅支持mp4格式输出)
        String outFile = recordPath + "/" + deviceId + ".mp4";
        EpEditor.OutputOption outputOption = new EpEditor.OutputOption(outFile);
        /*
        outputOption.setWidth(480);//输出视频宽，默认480
        outputOption.setHeight(360);//输出视频高度,默认360
        outputOption.frameRate = 30;//输出视频帧率,默认30
        outputOption.bitRate = 10;//输出视频码率,默认10
        */
        EpEditor.mergeByLc(this,epVideos, outputOption, new OnEditorListener() {
            @Override
            public void onSuccess() {
                RingLog.d(TAG, "video merge success");
                mHandler.sendEmptyMessage(UPLOAD_FILE);
            }

            @Override
            public void onFailure() {
                RingLog.d(TAG, "video merge fail");
                isRecording = false;
            }

            @Override
            public void onProgress(float progress) {
                //这里获取处理进度
            }
        });
    }
    private void startPublishRecordTimer() {
        destroyTimer();
        initTimer();
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    /**
     * 初始化Timer
     */
    private void initTimer() {
        int  stopTime = 0;
        if(CommonUtil.haveUdisk()){
            stopTime = 60*60;
            //stopTime = 60*3;
        }else {
            stopTime = 60;
        }
        final int  stopTimeCount = stopTime;
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if(isRecording && !isRecordPaused){
                    mTimerCount += 1;
                }
                if (isRecording && mTimerCount > stopTimeCount) {
                    // 已录制一分钟 停止录像
                    cameraRecordStop();
                    ComposerVideo();
                    return;
                }
            }
        };
        mTimer = new Timer();
    }

    /**
     * destroy上次使用的Timer
     */
    private void destroyTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    public void uploadRecord() {
        // OSS初始化
        isUploading = true;
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000);   // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000);   // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5);    // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(30);   // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(getApplicationContext(), END_POINT, credentialProvider, conf);
        RingLog.d(TAG, "OSS Init");
        uploadFile(oss);
        isUploading = false;
    }

    private void uploadFile(OSS oss) {
        // 上传文件
        RingLog.d(TAG, "Upload Start");
        String date = getCurrentDate();
        //String fileName = deviceId + "_" + date + ".mp4";
        final String objectKey = OBJECT_KEY_DIR + mYear + "/" + mMonth + "/" + mDay + "/" + deviceId +getFileName()+ ".mp4";
        String recordFilePath = recordPath + "/" + deviceId + ".mp4";
        RingLog.d(TAG, "File name: " + objectKey);
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, objectKey, recordFilePath);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/octet-stream");
        put.setMetadata(metadata);
        put.setCRC64(OSSRequest.CRC64Config.YES);
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                //Log.d(TAG, "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d(TAG,"uploadSuccess");
                if(mRecorderEventListener!=null) mRecorderEventListener.onRecordFinished(objectKey);
                uploadSuccess = true;
                isRecording = false;
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                uploadSuccess = false;
                Log.d(TAG,"uploadFailed");
                mHandler.removeMessages(UPLOAD_FILE);
                mHandler.sendEmptyMessageDelayed(UPLOAD_FILE,30*1000);
                // Request exception
                if (clientException != null) {
                    // Local exception, such as a network exception
                    clientException.printStackTrace();
                }
                if (serviceException != null) {
                    // Service exception
                    Log.d(TAG,"ErrorCode="+ serviceException.getErrorCode());
                    Log.d(TAG,"RequestId=" + serviceException.getRequestId());
                    Log.d(TAG,"HostId=" + serviceException.getHostId());
                    Log.d(TAG,"RawMessage=" + serviceException.getRawMessage());
                }
            }
            // task.cancel(); // Cancel the task

            // task.waitUntilFinished(); // Wait till the task is finished
        });
        /*
        try {
            PutObjectResult putObjectResult = oss.putObject(put);
            RingLog.d(TAG, "PubObject: Upload Success");
            RingLog.d(TAG, "ETag: " + putObjectResult.getETag());
            RingLog.d(TAG, "RequestId: " + putObjectResult.getRequestId());
            uploadSuccess = true;
            if(mRecorderEventListener!=null) mRecorderEventListener.onRecordFinished(objectKey);
        } catch (ClientException e) {
            // 本地异常如网络异常等
            RingLog.d(TAG, "PubObject: Upload failed,network error");
            e.printStackTrace();
            uploadSuccess = false;
        } catch (ServiceException e) {
            // 服务异常
            RingLog.d(TAG, "PubObject: Upload failed");
            RingLog.d(TAG, "RequestId is: " + e.getRequestId());
            RingLog.d(TAG, "ErrorCode is: " + e.getErrorCode());
            RingLog.d(TAG, "HostId is: " + e.getHostId());
            RingLog.d(TAG, "RawMessage is: " + e.getRawMessage());
            uploadSuccess = false;
        }
        */
    }

    private String getCurrentDate() {
        String date = "";
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        mYear = Integer.toString(year);
        mMonth = getIntegerFormat(month);
        mDay = getIntegerFormat(day);
        date = mYear + mMonth + mDay;
        return date;
    }

    private String getIntegerFormat(int integer) {
        String str;
        if (integer < 10)
            str = "0" + Integer.toString(integer);
        else
            str = Integer.toString(integer);
        return str;
    }

    private String getFileName(){
        Time time  =  new Time();
        time.setToNow();
        String str_time = time.format("%Y%m%d%H%M%S");
        return str_time;
    }
    private void removeOlderFiles(String filePath,boolean  byTime){
        //File diskFile = new File("/storage/udisk0");
        File diskFile = new File(filePath);
        if(diskFile.exists()){
            File[] files = diskFile.listFiles();
            for (File file : files) {
                if(file.getAbsolutePath().contains(".mp4")) {
                    if(byTime) {
                        if (file.lastModified() < CommonUtil.getPastDate(7)) {
                            file.delete();
                        }
                    }else {
                        file.delete();
                    }
                }
            }

        }
    }
}
