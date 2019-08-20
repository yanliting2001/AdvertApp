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
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.github.faucamp.simplertmp.RtmpHandler;
import com.grandartisans.advert.activity.MediaPlayerActivity;
import com.grandartisans.advert.interfaces.RecorderEventListener;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.ljy.devring.other.RingLog;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class CameraService extends Service implements SrsRecordHandler.SrsRecordListener,RtmpHandler.RtmpListener {
    private SrsPublisher mPublisher;
    private Camera mCamera;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private String deviceId;
    private String recordPath;
    private String mYear;
    private String mMonth;
    private String mDay;

    private int mTimerCount = 0;

    private boolean isRecord = false;
    private boolean recordHasFinished = false;
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
                case START_RTMP:
                    startRtmp();
                    break;
                    /*
                case START_RECORD:
                    startCameraRecord();
                    break;
                    */
                case UPLOAD_FILE:
                    uploadRecord();
                    break;
                case RESTART_RECORD:
                    restartCameraRecord();
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
        if(CommonUtil.haveUdisk()){
            recordPath = "/storage/udisk0/";
        }else {
            recordPath = getApplicationContext().getExternalFilesDir(null).getPath();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onRecordPause() {
        RingLog.d(TAG, "Record paused");
        mTimer.cancel();
        isRecord = false;
    }

    @Override
    public void onRecordResume() {
        RingLog.d(TAG, "Record resumed");
        isRecord = true;
        startPublishRecordTimer();
    }

    @Override
    public void onRecordStarted(String msg) {
        RingLog.d(TAG, "Record start");
        // 打开计时器
        isRecord = true;
        startPublishRecordTimer();
        if(mRecorderEventListener!=null)
            mRecorderEventListener.onRecordStart();
    }


    @Override
    public void onRecordFinished(String msg) {
        RingLog.d(TAG, "Record finished");
        isRecord = false;
        mTimerCount = 0;
        if (cameraNeedStop){
            RingLog.d(TAG, "Record is forced to stop");
            destroyTimer();
            cameraNeedStop = false;
        } else {
            RingLog.d(TAG, "Now stop record, upload it to server");
            if(CommonUtil.haveUdisk()) {
                mHandler.sendEmptyMessageDelayed(RESTART_RECORD,3*1000);
            }else {
                destroyTimer();
                recordHasFinished = true;
                mHandler.sendEmptyMessage(UPLOAD_FILE);
            }
        }
        if (MediaPlayerActivity.firstStartRecord) {
            MediaPlayerActivity.firstStartRecord = false;
        }
    }

    @Override
    public void onRecordIOException(IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        e.printStackTrace();
    }

    @Override
    public void onRtmpConnecting(String msg) {
        RingLog.d(TAG, "Rtmp connecting");
    }

    @Override
    public void onRtmpConnected(String msg) {
        RingLog.d(TAG, "Rtmp connected");
    }

    @Override
    public void onRtmpVideoStreaming() {}

    @Override
    public void onRtmpAudioStreaming() {}

    @Override
    public void onRtmpStopped() {
        RingLog.d(TAG, "Rtmp has stopped");
    }

    @Override
    public void onRtmpDisconnected() {
        RingLog.d(TAG, "Rtmp has disconnected the server");
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {}

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {}

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {}

    @Override
    public void onRtmpSocketException(SocketException e) {
        e.printStackTrace();
    }

    @Override
    public void onRtmpIOException(IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        e.printStackTrace();
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        e.printStackTrace();
    }

    public class CamBinder extends Binder {
        public CameraService getService() {
            return CameraService.this;
        }
    }

    public void  registerListener(RecorderEventListener listener){
        if(listener!=null) mRecorderEventListener = listener;
    }

    public void startCameraRecord(SrsPublisher publisher) {
        RingLog.d(TAG, "Open Record Camera");
        recordHasFinished = false;
        if(CommonUtil.haveUdisk()) removeOlderFiles();
        deviceId = SystemInfoManager.readFromNandkey("usid");
        if (deviceId == null) {
            deviceId = "G50234001485210002";
        }
        deviceId=deviceId.toUpperCase();

        mPublisher = publisher;
        mPublisher.setRecordHandler(new SrsRecordHandler(this));
        mCamera = mPublisher.getCamera();
        if (mCamera != null) {
            RingLog.d(TAG, "Camera Id is: " + mPublisher.getCamraId());
            RingLog.d(TAG, "Start record");
            // 开始录像
            String fileName = "";
            if(CommonUtil.haveUdisk()) {
                fileName = recordPath + deviceId + getFileName() + ".mp4";
            }else {
                fileName = recordPath + "/" + deviceId + ".mp4";
            }
            mPublisher.startRecord(fileName);
        }
    }

    public void restartCameraRecord() {
        if(CommonUtil.haveUdisk()) removeOlderFiles();
        cameraNeedStop = false;
        if (recordHasFinished) {
            RingLog.d(TAG, "Not need record any more");
            return;
        }
        RingLog.d(TAG, "Restart record");
        mPublisher.startCamera();
        if (mPublisher.getCamera() != null) {
            RingLog.d(TAG, "Restart now");
            // 重新开始录像
            String fileName = "";
            if(CommonUtil.haveUdisk()) {
                fileName = recordPath + deviceId + getFileName() + ".mp4";
            }else {
                fileName = recordPath + "/" + deviceId + ".mp4";
            }
            RingLog.d(TAG, "Start record fileName = " + fileName);
            mPublisher.startRecord(fileName);
        }
    }

    public boolean getRecordStatus() {
        return isRecord;
    }

    public boolean getFinishStatus() {
        return recordHasFinished;
    }

    public boolean recordUploadSuccess() {
        return uploadSuccess;
    }

    public boolean getUploadStatus() {
        return isUploading;
    }

    public void startRtmp() {
        RingLog.d(TAG, "Open RTMP Camera");
        SrsCameraView cameraView = MediaPlayerActivity.mCameraView;
        // RTMP推流状态回调
		mPublisher.setRtmpHandler(new RtmpHandler(this));

        // 切换摄像头
        cameraView.stopCamera();
        cameraView.setCameraId((mPublisher.getCamraId() + 1) % Camera.getNumberOfCameras());
        cameraView.startCamera();

        String rtmpUrl = RTMP_CHANNEL + "/" + deviceId;
        RingLog.d(TAG, "The rtmp url is: " + rtmpUrl);
        mPublisher.startPublish(rtmpUrl);
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
                if(isRecord) mTimerCount += 1;
                if (isRecord && mTimerCount > stopTimeCount) {
                    // 已录制一分钟 停止录像
                    mPublisher.stopRecord();
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
    private void removeOlderFiles(){
        File diskFile = new File("/storage/udisk0");
        if(diskFile.exists()){
            File[] files = diskFile.listFiles();
            for (File file : files) {
                if(file.getAbsolutePath().contains(".mp4")) {
                    if(file.lastModified() < CommonUtil.getPastDate(7)){
                        file.delete();
                    }
                }
            }

        }
    }
}
