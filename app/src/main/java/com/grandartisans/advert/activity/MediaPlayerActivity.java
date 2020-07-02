package com.grandartisans.advert.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.grandartisans.advert.app.AdvertApp;
import com.grandartisans.advert.common.Common;
import com.grandartisans.advert.common.ScheduleTimesCache;
import com.grandartisans.advert.dbutils.PlayRecord;
import com.grandartisans.advert.dbutils.dbutils;
import com.grandartisans.advert.interfaces.AdListEventListener;
import com.grandartisans.advert.interfaces.ElevatorDoorEventListener;
import com.grandartisans.advert.interfaces.ElevatorEventListener;
import com.grandartisans.advert.interfaces.IMultKeyTrigger;
import com.grandartisans.advert.interfaces.RecorderEventListener;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.PlayerCmd;
import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.grandartisans.advert.model.entity.event.AppEvent;
import com.grandartisans.advert.model.entity.post.EventParameter;
import com.grandartisans.advert.model.entity.post.PlayerStatusParameter;
import com.grandartisans.advert.model.entity.post.RecorderUpdateEventData;
import com.grandartisans.advert.model.entity.post.ReportEventData;
import com.grandartisans.advert.model.entity.res.AdvertInfoData;
import com.grandartisans.advert.model.entity.res.AdvertWeatherData;
import com.grandartisans.advert.model.entity.res.NetworkOnOffData;
import com.grandartisans.advert.model.entity.res.PowerOnOffData;
import com.grandartisans.advert.model.entity.res.ReportInfoResult;
import com.grandartisans.advert.model.entity.res.TemplateReginVo;
import com.grandartisans.advert.model.entity.res.TemplateRegion;
import com.grandartisans.advert.model.entity.res.TerminalAdvertPackageVo;
import com.grandartisans.advert.receiver.AlarmReceiver;
import com.grandartisans.advert.recoder.RecorderManager;
import com.grandartisans.advert.server.M3u8Server;
import com.grandartisans.advert.service.CameraService;
import com.grandartisans.advert.service.DownLoadService;
import com.grandartisans.advert.service.NetworkService;
import com.grandartisans.advert.service.UpgradeService;
import com.grandartisans.advert.utils.ALFu700ProjectManager;
import com.grandartisans.advert.utils.AdPlayListManager;
import com.grandartisans.advert.utils.AlarmEventManager;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.ElevatorDoorManager;
import com.grandartisans.advert.utils.ElevatorStatusManager;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.MyMultKeyTrigger;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.grandartisans.advert.utils.Utils;
import com.grandartisans.advert.utils.ViewUtils;
import com.grandartisans.advert.view.MarqueeTextView;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.other.RingLog;
import com.ljy.devring.util.FileUtil;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.prj.utils.PrjSettingsManager;
import com.westone.cryptoSdk.Api;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import gartisans.hardware.pico.PicoClient;

public class MediaPlayerActivity extends Activity implements SurfaceHolder.Callback{
	private final String TAG = "MediaPlayerActivity";
	private PLMediaPlayer mMediaPlayer;
	private SurfaceView surface;
	private SurfaceView surface_record;
	private SurfaceHolder surfaceHolder_record;
	private ImageView mImageMain ;
	private SurfaceHolder surfaceHolder;
    private RelativeLayout relativeLayout;
	private MarqueeTextView marqueeTextView;
	private LinearLayout linearLayoutText;
	private ImageView imageViewWeather;
	private TextView textViewTime;
	private TextView textViewTemp;
	private TextView textViewTempCode;
	private boolean surfaceDestroyedFlag = true;

	private int surfaceDestroyedCount = 0;
	private int mediaplayerDestroyedCount = 0;
	private int menuKeyPressedCount = 0;

	private int playindex = 0;

	private Handler handler;
	private int threshold_distance = 0;
	private PrjSettingsManager prjmanager = null;
	private int screenStatus = 1;

	private volatile boolean isPowerOff = false;//定时待机状态

	PowerManager mPowerManager;
	PowerManager.WakeLock mWakeLock;

	private final  int threshold_temperature = 58;
	private  final int temperature_read_times = 200; //连续获取到温度超过指定值次数大于该值，则认为温度过高了
	private int temperature_read_count=0;
	private final int IMAGE_AD_SHOW_DURATION=10*1000;

	private PicoClient pClient= null;

	private boolean  player_first_time = true;

	private final int SET_SCREEN_ON_CMD = 100010;
	private final int START_PLAYER_CMD = 100011;
	private final int START_REPORT_EVENT_CMD= 100012;
	private final int ON_PAUSE_EVENT_CMD = 100014;
	private final int SET_POWER_ALARM_CMD = 100015;
	private final int START_OPEN_SERIALPORT = 100016;
	private final int START_PUSH_CMD = 100017;
	private final int STOP_PUSH_CMD = 100018;

	private final int START_REPORT_PLAYSTATUS_CMD= 100019;

	private final int START_SERVICE_CMD = 100020;

	private final int SET_LIFT_STOP_CMD = 100021;
	private final int START_FIRST_RECORD_CMD = 100022;

	private final int START_CAMERACHECK_CMD = 100023;

    private final int UPDATE_IMAGE_AD_CMD = 100028;
    private final int SHOW_NEXT_ADVERT_CMD = 100029;

	private final int UPDATE_TIME_INFO_CMD = 100030;
	private final int HIDE_UGRENT_INFO_CMD = 100031;

	private final int AD_PLAYER_CMD = 100032;
    private final int CHECK_PLAYER_START_CMD = 100033;
	private final int RECORDER_PAUSE_CMD = 100034;

	private final int RECORDER_FINISHED_CMD = 100035;
	private final int SET_NETWORK_ALARM_CMD = 100036;
	private final int REBOOT_CMD = 100037;
    private final int SEND_ENTER_KEY_CMD = 100038;
    private final int PROJECT_WACKUP_CMD = 100039;
	private String mMode ="";

	static final int PLAYER_STATE_INIT = 0;
	static final int PLAYER_STATE_PREPARING = 1;
	static final int PLAYER_STATE_PLAYING = 2;
	static final int PLAYER_STATE_PAUSED = 3;
	static final int PLAYER_STATE_STOPED = 4;
	static final int PLAYER_STATE_RELEASED = 5;
	private int mPlayState = PLAYER_STATE_INIT;

	private CameraService mCameraService;
	private ServiceConnection mCamServiceConn;
	private float mInitZ = 0;
	private ElevatorStatusManager mElevatorStatusManager;
	private ElevatorDoorManager mElevatorDoorManager=null;


	private int mReportEventTimeInterval=5*60*1000;
	private int mIntentId = 0;

	private boolean  activate_started = false;


	AdPlayListManager mPlayListManager = null;

    private TerminalAdvertPackageVo mTerminalAdvertPackageVo;
    private long mMainPositionId = 0;

	private Dialog mUrgentInfoDialog = null;


	private Long mStartPlayTime = 0L ;
	private Long mPausePlayTime=0L;
	private Long mPlayedTime = 0L;
	private int mPlayingAdDuration=0;
	private Long mPlayingAdType = 2L;

	private boolean mContinuePlayWhenScreenOff = false;

	private AlarmReceiver mAlarmReceiver = null;
	private ALFu700ProjectManager mProjectManager = null;

	private AlarmEventManager mAlarmEventManager = null;
	private int project_wackup_cmd_count = 0;

	private  Api encApi=null;
	private Handler mPlayerHandler = new Handler() {
		public void handleMessage(Message paramMessage) {
			switch (paramMessage.what) {
				case AD_PLAYER_CMD:
					PlayerCmd playerCmd = (PlayerCmd) paramMessage.obj;
					processPlayerCmd(playerCmd.getCmd(), playerCmd.getUrl());
					break;
                case CHECK_PLAYER_START_CMD:
                    if(getPlayerState()==PLAYER_STATE_PREPARING){
                        onVideoPlayCompleted(true);
                    }
                    break;
				case RECORDER_PAUSE_CMD:
					mCameraService.cameraRecordPause();
					break;
				case RECORDER_FINISHED_CMD:
					mCameraService.cameraRecordStop();
					break;
			}
		}
	};

	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message paramMessage)
		{
			switch (paramMessage.what)
			{
				case SET_SCREEN_ON_CMD:
					setScreenOn();
					break;
				case START_PLAYER_CMD:
					//initPlayer();
                    initAdFiles();
					break;
				case START_OPEN_SERIALPORT:
					mElevatorDoorManager.openSerialPort();
					break;
				case START_REPORT_EVENT_CMD:
					ReportPlayRecordAll();
					break;
				case ON_PAUSE_EVENT_CMD:
					onPauseEvent();
					break;
				case SET_POWER_ALARM_CMD:
					if(mAlarmEventManager.getPowerAlarmStatus()==false) {
						mAlarmEventManager.SetPowerAlarm(0, 0);
					}
					break;
				case SET_NETWORK_ALARM_CMD:
					if(mAlarmEventManager.getNetWorkAlarmStatus()==false) {
						mAlarmEventManager.SetNetworkAlarm(-1, -1);
					}
					break;
				case STOP_PUSH_CMD:
                    break;
				case START_REPORT_PLAYSTATUS_CMD:
					ReportPlayStatus();
					break;
				case START_SERVICE_CMD:
					initCameraService();
					break;
				case SET_LIFT_STOP_CMD:
					//setLiftState(LIFT_STATE_STOP);
					setScreenOff(false);
					break;
				case START_FIRST_RECORD_CMD:
					startFirstRecord();
					break;
                case START_CAMERACHECK_CMD:
                    checkCamera();
                    break;
				case UPDATE_IMAGE_AD_CMD:
					ImageView imageview = (ImageView)paramMessage.obj;
					showImageWithIndex(imageview);
					savePlayRecord((Long)imageview.getTag(R.id.image_key));
					break;
				case SHOW_NEXT_ADVERT_CMD:
					onVideoPlayCompleted(true);
					break;
				case UPDATE_TIME_INFO_CMD:
					updateTimeInfo();
					break;
				case HIDE_UGRENT_INFO_CMD:
					HideUgrentInfo();
				case REBOOT_CMD:
					CommonUtil.reboot(MediaPlayerActivity.this);
					break;
                case SEND_ENTER_KEY_CMD:
                    CommonUtil.runCmd("input keyevent 23");
                    CommonUtil.runCmd("input keyevent 23");
                    break;
				case PROJECT_WACKUP_CMD:
					if(!isPowerOff){
						if(mProjectManager!=null)
							mProjectManager.WakeUpProject();
					}
					project_wackup_cmd_count++;
					if(project_wackup_cmd_count<10) {
						mHandler.sendEmptyMessageDelayed(PROJECT_WACKUP_CMD, 1000 * 5);
					}
					break;
				default:
					break;
			}
			super.handleMessage(paramMessage);
		}
	};

	/**
	 * <功能描述> 保持屏幕常亮
	 *
	 * @return void [返回类型说明]
	 */
	private void keepScreenWake() {
		// 获取WakeLock锁，保持屏幕常亮
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//mPowerManager.wakeUp(SystemClock.uptimeMillis());
		mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, this
				.getClass().getCanonicalName());
		mWakeLock.acquire();
	}
	/**
	 * <功能描述> 释放WakeLock
	 *
	 * @return void [返回类型说明]
	 */
	private void releaseWakeLock() {
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}
    ReentrantLock lock = new ReentrantLock();
    //开线程更新UI


	Runnable runableSetPowerOff = new Runnable() {
		@Override
		public void run() {
			lock.lock();
			//CommonUtil.sleep(MediaPlayerActivity.this);
			if(mProjectManager!=null)
				mProjectManager.SleepProject();
            if(isPowerOff==false) {
				isPowerOff = true;
				setScreenOff(false);
				mHandler.sendEmptyMessageDelayed(START_REPORT_EVENT_CMD,mReportEventTimeInterval);
			}
            lock.unlock();
		}
	};

	Runnable runableSetPowerOn = new Runnable() {
		@Override
		public void run() {
			lock.lock();
			if(isPowerOff==true) {
				CommonUtil.runCmd("ifconfig eth0 up");//打开有线网络
				mAlarmEventManager.setPowerAlarmStatus(false);
				mAlarmEventManager.setNetWorkAlarmStatus(false);
				mHandler.sendEmptyMessageDelayed(SET_POWER_ALARM_CMD,1000*60*5);
				mHandler.sendEmptyMessageDelayed(SET_NETWORK_ALARM_CMD,1000*60*5);
				isPowerOff = false;
				setScreen(1);
				onVideoPlayCompleted(false);
				project_wackup_cmd_count = 0;
				mHandler.sendEmptyMessage(PROJECT_WACKUP_CMD);

			}
			lock.unlock();
			//initPowerOnAlarm(13,10,00);
			//initPowerOnAlarm(13,10,00);
		}
	};

	private void onPlayerCmd(String cmd,String url) {
		PlayerCmd playerCmd = new PlayerCmd();
		playerCmd.setCmd(cmd);
		playerCmd.setUrl(url);
		Message msg = new Message();
		msg.what = AD_PLAYER_CMD;
		msg.obj = playerCmd;
		mPlayerHandler.sendMessage(msg);
	}
	private void processPlayerCmd(String cmd,String url){
		Log.d(TAG,"processPlayerCmd cmd:" + cmd + "PlayerState :"  + getPlayerState());
		if(cmd.equals("reset")){
			if(getPlayerState()==PLAYER_STATE_PLAYING
					||getPlayerState()==PLAYER_STATE_PAUSED){
				PlayerStop();
			}
			PlayerRelease();
			initFirstPlayer();
		}else if(cmd.equals("source")){
			if(getPlayerState()==PLAYER_STATE_INIT||getPlayerState()==PLAYER_STATE_STOPED) {
				setDataSource(url);
			}
		}
		else if(cmd.equals("start")){
			if(getPlayerState()==PLAYER_STATE_PREPARING) {
				PlayerStart();
			}
		}
		else if(cmd.equals("pause")){
			if(getPlayerState()==PLAYER_STATE_PLAYING){
				PlayerPause();
			}
		}else if(cmd.equals("resume")) {
			if(getPlayerState()==PLAYER_STATE_PAUSED){
				PlayerResume();
			}
		}else if(cmd.equals("stop")){
			if(getPlayerState()==PLAYER_STATE_PLAYING
					|| getPlayerState()==PLAYER_STATE_PAUSED) {
				PlayerStop();
			}
			setPlayerState(PLAYER_STATE_STOPED);
		}else if(cmd.equals("release")){
			if(getPlayerState()==PLAYER_STATE_PLAYING
					|| getPlayerState()==PLAYER_STATE_PAUSED) {
				PlayerStop();
			}
			PlayerRelease();
		}

	}
	private void setPlayerState(int state){
		mPlayState = state;
	}
	private int getPlayerState(){
		return mPlayState;
	}
	private void PlayerStart(){
		if(mMediaPlayer!=null){
			mMediaPlayer.start();
			setPlayerState(PLAYER_STATE_PLAYING);
		}
	}
	private void PlayerPause(){
		if(mMediaPlayer!=null) {
			Log.d(TAG,"onPlayer Pause ");
			mMediaPlayer.pause();
			setPlayerState(PLAYER_STATE_PAUSED);
		}
	}
	private void PlayerResume(){
		if(mMediaPlayer!=null) {
			mMediaPlayer.start();
			Log.d(TAG,"onPlayer Resume isPlaying = " + mMediaPlayer.isPlaying() + "position = " + mMediaPlayer.getCurrentPosition() + "duration = " + mMediaPlayer.getDuration());
			setPlayerState(PLAYER_STATE_PLAYING);
		}
	}
	private void PlayerStop(){
		if(mMediaPlayer!=null) {
			mMediaPlayer.stop();
		}
		setPlayerState(PLAYER_STATE_STOPED);
	}
	private void PlayerRelease(){
		if(mMediaPlayer!=null){
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		setPlayerState(PLAYER_STATE_RELEASED);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_surface_player);
		Log.i(TAG,"onCreate");
        initBaseView();

        /*音创990板卡启动时可能会出现system无响应情况，发送回车键消除*/
        CommonUtil.runCmd("input keyevent 23");
        CommonUtil.runCmd("input keyevent 23");

		//mProjectManager  = ALFu700ProjectManager.getInstance(getApplicationContext());
		if(mProjectManager!=null)
			mProjectManager.openProject();

        /*
		AdvertApp app = (AdvertApp) getApplication();
		app.initApps();
        */
		setCurrentTime();
		keepScreenWake();

		M3u8Server.execute();

		mPlayListManager = AdPlayListManager.getInstance(getApplicationContext());
		mPlayListManager.registerListener(mAdListEventListener);
		mPlayListManager.init(this);

		mAlarmEventManager = AlarmEventManager.getInstance(getApplicationContext());

		handler = new Handler();
		mHandler.sendEmptyMessageDelayed(SET_POWER_ALARM_CMD,1000*60*5);
		mHandler.sendEmptyMessageDelayed(SET_NETWORK_ALARM_CMD,1000*60*5);
		if(SystemInfoManager.isClassExist("android.prj.PrjManager")) {
			prjmanager = PrjSettingsManager.getInstance(this);
		}

		mMode = CommonUtil.getModel();
		if(mMode.equals("GAPEDS4A3") || mMode.equals("GAPEDS4A6")) {
			if (prjmanager != null) prjmanager.setBrightness(10);
		}
		float gsensordefault =0;
		if(prjmanager!=null) {
			gsensordefault = Float.valueOf(prjmanager.getGsensorDefault());
			mElevatorStatusManager = new ElevatorStatusManager(this,mMode,gsensordefault);
			mElevatorStatusManager.registerListener(mElevatorEventListener);
			initTFMini();//初始化激光测距模块
		}else{
			gsensordefault = 200;
			mElevatorStatusManager = new ElevatorStatusManager(this,mMode,gsensordefault);
			mElevatorStatusManager.registerListener(mElevatorEventListener);
			initTFMini();//初始化激光测距模块

            Intent intentService = new Intent(MediaPlayerActivity.this,UpgradeService.class);
            startService(intentService);

            Intent i = new Intent(MediaPlayerActivity.this, DownLoadService.class);
            startService(i);
        }
		initEventBus();//注册事件接收

		encApi = new Api();
        mHandler.sendEmptyMessageDelayed(SEND_ENTER_KEY_CMD,1000*3);
		/*
		PicoClient.OnEventListener mPicoOnEventListener = new PicoClient.OnEventListener() {
		    @Override
            public void onEvent(PicoClient client, int etype, Object einfo) {
		        //Log.d(TAG, "PicoClient onEvent" + "etype " + etype + " einfo " + (Float)einfo);

		        if((Float)einfo  > threshold_temperature ) {
					Log.d(TAG, "read temperature onEvent" + "etype " + etype + " einfo " + (Float)einfo +  "is to high" + "read count = " + temperature_read_count);
					//LogToFile.i(TAG,"read temperature onEvent" + "etype " + etype + " einfo " + (Float)einfo +  "is to high "+"read count = " + temperature_read_count );
					temperature_read_count ++ ;
				}else {
					temperature_read_count = 0;
				}

				if(temperature_read_count > temperature_read_times) {
					//LogToFile.i(TAG, "temperature is too high , device will power off ");
				}

		    }
        };
		pClient = new PicoClient(mPicoOnEventListener, null);
		*/
		//mHandler.sendEmptyMessage(START_REPORT_PLAYSTATUS_CMD);
	}
	private void initBaseView(){
		relativeLayout = (RelativeLayout) findViewById(R.id.rootframeview);
		surface = (SurfaceView) findViewById(R.id.surface);
		mImageMain =(ImageView)findViewById(R.id.image_main);
		mImageMain.setVisibility(View.INVISIBLE);
		surface_record = (SurfaceView) findViewById(R.id.surfaceview_record);
		marqueeTextView = (MarqueeTextView) findViewById(R.id.tv_scroll);
		marqueeTextView.setVisibility(View.GONE);
		linearLayoutText = (LinearLayout) findViewById(R.id.ll_info);
		linearLayoutText.setVisibility(View.GONE);
		imageViewWeather = (ImageView) findViewById(R.id.icon_weather);
		textViewTime = (TextView) findViewById(R.id.tv_time);
		textViewTime.setVisibility(View.GONE);
		textViewTemp = (TextView) findViewById(R.id.tv_temperature);
		textViewTempCode = (TextView) findViewById(R.id.tv_temperature_code);
	}
	private void initView(){
		/*
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		*/
		/*
		Intent intent = new Intent("android.intent.action.hideNaviBar");
		intent.putExtra("hide",true);
		sendBroadcast(intent);
		*/


		Intent intent = new Intent("android.intent.always.hideNaviBar");
		intent.putExtra("always",true);//true为一直隐藏，false为取消一直隐藏
		sendBroadcast(intent);



		surfaceHolder_record = surface_record.getHolder();
		surfaceHolder = surface.getHolder();// SurfaceHolder是SurfaceView的控制接口
		surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.RGBX_8888);
		//surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		initAdView();

		AdvertInfoData data = mPlayListManager.getAdvertInfo();
		showAdvertInfo(data);
		AdvertWeatherData weatherData = mPlayListManager.getWeatherInfo();
		if(weatherData!=null) setWeatherInfo(weatherData);
	}
	/*
	 * 初始化播放首段视频的player
	 */
	private void initFirstPlayer() {
		mMediaPlayer = new PLMediaPlayer(this);
		//mMediaPlayer.reset();
		//mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setDisplay(surfaceHolder);
		mMediaPlayer
				.setOnCompletionListener(new PLMediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(PLMediaPlayer mp) {
						//onVideoPlayCompleted(true);
					}
				});
		mMediaPlayer.setOnPreparedListener(new PLMediaPlayer.OnPreparedListener(){
			@Override public void onPrepared(PLMediaPlayer mp,int preparedTime) {
                mPlayerHandler.removeMessages(CHECK_PLAYER_START_CMD);
                Log.i(TAG, "video width = " + mMediaPlayer.getVideoWidth() + "video height = " + mMediaPlayer.getVideoHeight() + "screenStatus = " + getScreenStatus());
                //mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                //mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
				onPlayerCmd("start","");
                if(!mContinuePlayWhenScreenOff) {
					if (getScreenStatus() == 0) {
						onPlayerCmd("pause","");
					}
				}
			}
		});

		mMediaPlayer.setOnErrorListener(new PLMediaPlayer.OnErrorListener(){
		    @Override public boolean onError(PLMediaPlayer mp,int errorcode)
            {
                Log.d(TAG, "OnError - Error code: " + errorcode );
				//onVideoPlayCompleted(false);
                return false;
            }

        });

		mMediaPlayer.setOnVideoSizeChangedListener(new PLMediaPlayer.OnVideoSizeChangedListener(){
			@Override public void onVideoSizeChanged(PLMediaPlayer mp,int width,int height,int videoSar, int videoDen) {
				Log.d(TAG, "setOnVideoSizeChangedListener  width: " + width + "height: " + height);
				//surfaceHolder.setFixedSize(width,height);
			}
		});
		setPlayerState(PLAYER_STATE_INIT);
	}
	private void setDataSource(String url) {
		try {
            //url = "http://test.res.dsp.grandartisans.cn/1d92cc66-d6f8-4776-b794-bb90e6683f43/playlist.m3u8";
			Log.d(TAG, "start play: url = " + url );
			mMediaPlayer.setDataSource(url);
			try{
				mMediaPlayer.prepareAsync();
				setPlayerState(PLAYER_STATE_PREPARING);
				//mPlayerHandler.sendEmptyMessageDelayed(CHECK_PLAYER_START_CMD,2*1000);
			}catch(IllegalStateException e) {
				onPlayerCmd("release","");
				onVideoPlayCompleted(true);
			}
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

	private void initAdFiles(){
        Log.i(TAG,"initAdFiles ");
		onPlayerCmd("reset","");
        int childCount = relativeLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (relativeLayout.getChildAt(i) instanceof ImageView) {
                ImageView imageView = (ImageView) relativeLayout.getChildAt(i);

				showImageWithIndex(imageView);
            } else if(relativeLayout.getChildAt(i) instanceof SurfaceView){
                SurfaceView surfaceView = (SurfaceView) relativeLayout.getChildAt(i);
                Log.i(TAG,"initAdFiles surfaceView mMainPositionId =  " + mMainPositionId);
                initPlayer(mMainPositionId);
            }
        }
    }

	private void initPlayer(Long posid){
        PlayingAdvert item  = mPlayListManager.getValidPlayUrl(posid,true);
        if(item!=null) {
			String url = item.getPath();
			Log.i(TAG,"player advertFile vType = " + item.getvType() + "url = " + item.getPath());
			mPlayingAdType = item.getvType();
			mPlayedTime = 0L;
        	if(item.getvType()==2) { //视频广告
				//surface.setVisibility(View.VISIBLE);
				mImageMain.setVisibility(View.GONE);
				if (url != null && url.length() > 0) {
					onPlayerCmd("source",url);
					int duration = item.getDuration();
					mPlayingAdDuration = duration;
					mStartPlayTime = System.currentTimeMillis();
					mHandler.removeMessages(SHOW_NEXT_ADVERT_CMD);
					mHandler.sendEmptyMessageDelayed(SHOW_NEXT_ADVERT_CMD,duration*1000);
				}
			}else if(item.getvType()==1) {
				//surface.setVisibility(View.GONE);
        		mImageMain.setVisibility(View.VISIBLE);
				showImageWithPath(mImageMain,item.getPath(),item.getDuration(),item.isEncrypt());
				mPlayingAdDuration = item.getDuration();
				mStartPlayTime = System.currentTimeMillis();
				RingLog.d(TAG, "remainingTime mPausePlayTime " + mStartPlayTime);
			}
		}
    }
	private void onVideoPlayCompleted(boolean completedNormal) {
		//get next player
		Log.i(TAG,"onVideoPlayCompleted format  isPowerOff =  " + isPowerOff + "isCompleted Normal = " + completedNormal);
		Long posid = mMainPositionId;
		if(completedNormal) {
			savePlayRecord(posid);
		}
		if(!isPowerOff) {
			onPlayerCmd("reset","");
			initPlayer(posid);
		}else{
			setScreenOff(false);
		}
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder,  int format, int width,int height) {
		// TODO 自动生成的方法存根
		Log.i(TAG,"surfaceChanged111 format = " + format + "width = "  + width  + "height = " + height);
		//holder.setFixedSize(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		//然后初始化播放手段视频的player对象
		//initPlayer();
		surfaceDestroyedFlag = false;
		if(prjmanager!=null) {
			CommonUtil.setScreenVideoMode("1");
		}
		String status = DevRing.cacheManager().spCache("PowerStatus").getString("status","on");
		Log.i(TAG,"surfaceCreated power status is :" + status);
		if(status.equals("off")){
			handler.post(runableSetPowerOff);
		}else {
			//mProjectManager.WakeUpProject();
			project_wackup_cmd_count = 0;
			mHandler.sendEmptyMessage(PROJECT_WACKUP_CMD);
			onResumeEvent();
			if (activate_started == false) {
				mHandler.sendEmptyMessage(START_PLAYER_CMD);
				if (mMode.equals("GAPEDS4A4") || mMode.equals("GAPEDS4A6") ||
						mMode.equals("GAPADS4A1") || mMode.equals("GAPADS4A2") || mMode.equals("GAPEDS4A3")) {
					mHandler.sendEmptyMessageDelayed(START_OPEN_SERIALPORT, 5 * 1000);
				} else {
					if (mElevatorDoorManager != null)
						mElevatorDoorManager.openSerialPort();

				}
				activate_started = true;
				//mHandler.sendEmptyMessageDelayed(REBOOT_CMD,1000*60*4);
			} else {
				mHandler.sendEmptyMessageDelayed(START_PLAYER_CMD, 1000);
				if (mElevatorDoorManager != null)
					mElevatorDoorManager.openSerialPort();
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO 自动生成的方法存根
		Log.i(TAG,"surfaceDestroyed");
		surfaceDestroyedFlag = true;
        onPauseEvent();
	}
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG,"onStart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG,"onStop");
	}


	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG,"onPause");
		unregisterAlarmReceiver();
		//mHandler.sendEmptyMessageDelayed(ON_PAUSE_EVENT_CMD,1000);
	}
	private void onPauseEvent(){
		Log.i(TAG,"onPauseEvent");
		mHandler.removeMessages(START_PLAYER_CMD);
		mHandler.removeMessages(SHOW_NEXT_ADVERT_CMD);
		mHandler.removeMessages(UPDATE_IMAGE_AD_CMD);
		if(mMode.equals("GAPEDS4A4") || mMode.equals("GAPEDS4A6")||
				mMode.equals("GAPADS4A1") || mMode.equals("GAPADS4A2") || mMode.equals("GAPEDS4A3")) {
			mHandler.removeMessages(START_OPEN_SERIALPORT);
		}
		if(mElevatorDoorManager!=null) mElevatorDoorManager.closeSeriaPort();
		onPlayerCmd("release","");
		//surface = null;
		surfaceHolder = null;
		if ( mCameraService != null && mCameraService.isRecording()) {
			CameraService.cameraNeedStop = true;
			mCameraService.cameraRecordPause();
		}
		setScreen(1);
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG,"onResume");
		initView();
		registerAlarmReceiver();
	}

	private void registerAlarmReceiver(){
		mAlarmReceiver = new AlarmReceiver();
		IntentFilter filter =  new IntentFilter();
		filter.addAction("POWER_OFF_ALARM");
		filter.addAction("POWER_ON_ALARM");
		filter.addAction("SET_WLAN_ON_ALARM");
		filter.addAction("SET_WLAN_OFF_ALARM");
		registerReceiver(mAlarmReceiver,filter);
	}
	private void unregisterAlarmReceiver(){
		unregisterReceiver(mAlarmReceiver);
	}

	private void onResumeEvent(){
        if(!mMode.equals("AOSP on p313")) {
			if(prjmanager!=null) {
				threshold_distance = Integer.valueOf(prjmanager.getDistance());
			}
			if(mElevatorDoorManager!=null)
				mElevatorDoorManager.setDefaultDistance(threshold_distance);
        }
		if(prjmanager!=null) {
			mInitZ = Float.valueOf(prjmanager.getGsensorDefault());
		}
		if(mElevatorDoorManager!=null)
        	mElevatorStatusManager.setAccSensorDefaultValue(mInitZ);

        if ( mCameraService!=null && mCameraService.isRecording()) {
        	mCameraService.cameraRecordResume(surfaceHolder_record.getSurface());
        }else {
			mHandler.sendEmptyMessageDelayed(START_CAMERACHECK_CMD, 30 * 1000);
		}
	}
	private IMultKeyTrigger multKeyTrigger= new MyMultKeyTrigger(this);
	Handler h = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				default:
					break;
			}
		};
	};
	public boolean handlerMultKey(int keyCode, KeyEvent event) {
		boolean vaildKey = false;
		if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
				|| keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
				&& multKeyTrigger.allowTrigger()) {
			// 是否是有效按键输入
			vaildKey = multKeyTrigger.checkKey(keyCode, event.getEventTime());
			// 是否触发组合键
			if (vaildKey && multKeyTrigger.checkMultKey()) {
				//执行触发
				multKeyTrigger.onTrigger();
				//触发完成后清除掉原先的输入
				multKeyTrigger.clearKeys();
				return true;
			}

		}
		return false;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG,"onKeyDown keyCode = " + keyCode);
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			if(handlerMultKey(keyCode,event)){
				//startMyApp(MediaPlayerActivity.this,"app_browser");
				startSysSetting(MediaPlayerActivity.this);
				return true;
			}
		}

		if(keyCode == KeyEvent.KEYCODE_MENU) {
			menuKeyPressedCount +=1;
			//startSysSetting(MediaPlayerActivity.this);
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER){
			//startMyApp(MediaPlayerActivity.this,"app_browser");
		}
		else if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BACKSLASH){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void startMyApp(Context context, String mode) {
		Intent it = new Intent();
		it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		it.setClass(context, MyAppActivity.class);
		it.putExtra("appmode", mode);
		context.startActivity(it);
	}

	private void initEventBus() {
		EventBus.getDefault().register(this);
        //DevRing.busManager().register(MediaPlayerActivity.class);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG,"onDestroy");
		releaseWakeLock();
		onPlayerCmd("release","");
		EventBus.getDefault().unregister(this);
		if(mElevatorDoorManager!=null) mElevatorDoorManager.closeSeriaPort();

		if (mCameraService!=null && mCameraService.isRecording()) {
			CameraService.cameraNeedStop = true;
			mCameraService.cameraRecordStop();
			unbindService(mCamServiceConn);
		}
	}

	private void initTFMini() {
		if(mMode.equals("AOSP on p313")) {
			threshold_distance = DevRing.cacheManager().spCache("TFMini").getInt("threshold_distance",0);
		}else {
			if(prjmanager!=null) {
				threshold_distance = Integer.valueOf(prjmanager.getDistance());
			}
			threshold_distance = 200;
		}
		mElevatorDoorManager = new ElevatorDoorManager(threshold_distance);
		mElevatorDoorManager.registerListener(mElevatorDoorEventListener);
	}
	private void startSysSetting(Context context) {
		Intent intent = new Intent(context, SystemSettingsMain.class);
		startActivity(intent);
	}
	private void savePlayRecord(Long posid) {
		PlayingAdvert item = mPlayListManager.getPlayingAd(posid);
		if(item!=null) {
			PlayRecord record = new PlayRecord();
			record.setTpid(item.getTemplateid());
			record.setApid(item.getAdPositionID());
			record.setAdid(item.getAdvertid());
			long currentTime = System.currentTimeMillis();
			record.setStarttime(currentTime);
			record.setEndtime(currentTime);
			record.setCount(1);
			dbutils.updatePlayCount(record);

			List<PlayRecord> records = dbutils.selectById(PlayRecord.class,record.getTpid(),record.getApid(),record.getAdid());
			if(records!=null && records.size()>0) {
				PlayRecord recordItem = records.get(0);
				if(recordItem.getCount()>=30){
					ReportPlayRecord(recordItem);
				}
			}
		}
	}

	private void ReportPlayRecordAll() {
		List<PlayRecord> records = dbutils.getPlayRecordAll(PlayRecord.class);
		if(records!=null && records.size()>0) {
			for(int i=0;i<records.size();i++) {
				PlayRecord recordItem = records.get(i);
				if (recordItem.getCount() >0) {
					ReportPlayRecord(recordItem);
				}
			}
			mHandler.removeMessages(START_REPORT_EVENT_CMD);
			mHandler.sendEmptyMessageDelayed(START_REPORT_EVENT_CMD,mReportEventTimeInterval);
		}

	}

	private void ReportPlayStatus () {
		EventParameter parameter = new EventParameter();
		String 	deviceId = SystemInfoManager.getDeviceId(getApplicationContext());
		parameter.setSn(deviceId.toUpperCase());
		parameter.setSessionid(CommonUtil.getRandomString(50));
		parameter.setTimestamp(System.currentTimeMillis());
		parameter.setToken(UpgradeService.mToken);
		parameter.setApp(Utils.getAppPackageName(MediaPlayerActivity.this));
		parameter.setEvent("playStatus");
		parameter.setEventtype(0000);

		parameter.setMac(CommonUtil.getEthernetMac());

		PlayerStatusParameter eventData = new PlayerStatusParameter();
		if(mMediaPlayer!=null) {
			eventData.setPlayerHandler(true);
			eventData.setPlaying(mMediaPlayer.isPlaying());
		}
		else {
			eventData.setPlayerHandler(false);
			eventData.setPlaying(false);
		}
		eventData.setSurfaceDestroyedFlag(surfaceDestroyedFlag);
		eventData.setgSensorDefaultValue(mInitZ);
		eventData.setGtfminiDefaultValue(threshold_distance);
		eventData.setMediaplayerDestroyedCount(mediaplayerDestroyedCount);
		eventData.setSurfaceDestroyedCount(surfaceDestroyedCount);
		eventData.setMenukeyPressedCount(menuKeyPressedCount);
		parameter.setEventData(eventData);
		//parameter.setIp();
		parameter.setTimestamp(System.currentTimeMillis());
		AdvertModel mIModel = new AdvertModel();

		DevRing.httpManager().commonRequest(mIModel.reportEvent(parameter), new CommonObserver<ReportInfoResult>() {
			@Override
			public void onResult(ReportInfoResult result) {
				RingLog.d("reportPlayer  status ok = " + result.getStatus() );
			}

			@Override
			public void onError(int i, String s) {
				RingLog.d("reportPlayer error i = " + i + "msg = " + s );
			}
		},null);

		mHandler.removeMessages(START_REPORT_PLAYSTATUS_CMD);
		mHandler.sendEmptyMessageDelayed(START_REPORT_PLAYSTATUS_CMD,mReportEventTimeInterval);
	}

	private void ReportPlayRecord(final PlayRecord record)
	{
		EventParameter parameter = new EventParameter();
		String 	deviceId = SystemInfoManager.getDeviceId(getApplicationContext());
		parameter.setSn(deviceId.toUpperCase());
		parameter.setSessionid(CommonUtil.getRandomString(50));
		parameter.setTimestamp(System.currentTimeMillis());
		parameter.setToken(UpgradeService.mToken);
		parameter.setApp(Utils.getAppPackageName(MediaPlayerActivity.this));
		parameter.setEvent("playRecord");
		parameter.setEventtype(4000);

		parameter.setMac(CommonUtil.getEthernetMac());

        ReportEventData eventData = new ReportEventData();
        eventData.setCount(record.getCount());
        eventData.setAdvertid(record.getAdid());
        eventData.setAdPositionID(record.getApid());
        eventData.setTemplateid(record.getTpid());
        eventData.setStartTime(record.getStarttime());
        eventData.setEndTime(record.getEndtime());
        parameter.setEventData(eventData);
		//parameter.setIp();
		parameter.setTimestamp(System.currentTimeMillis());
		AdvertModel mIModel = new AdvertModel();

		DevRing.httpManager().commonRequest(mIModel.reportEvent(parameter), new CommonObserver<ReportInfoResult>() {
			@Override
			public void onResult(ReportInfoResult result) {
				RingLog.d("reportEvent ok status = " + result.getStatus() );
				dbutils.deletePlayRecord(PlayRecord.class,record.getId());
			}

			@Override
			public void onError(int i, String s) {
				RingLog.d("reportEvent error i = " + i + "msg = " + s );
			}
		},null);
	}

	private int getScreenStatus(){
	    return screenStatus;
    }

	private void setScreen(int enable){
        screenStatus = enable;
		Log.i(TAG,"setScreen enable :" + enable);
		if(prjmanager!=null) {
			prjmanager.setScreen(enable);
		}
	}
	private void setScreenOff(boolean continuePlay){
		if (getScreenStatus() != 0) {
			setScreen(0);
			if (mCameraService!=null && mCameraService.isRecording() && !CommonUtil.haveUdisk()) {
				RingLog.d(TAG, "Player is paused, so pause record");
				mCameraService.cameraRecordPause();
			}
			if(!continuePlay) {
				if (mPlayingAdType == 2) {
					mHandler.removeMessages(SHOW_NEXT_ADVERT_CMD);
					onPlayerCmd("pause","");
					mPausePlayTime = System.currentTimeMillis();
					RingLog.d(TAG, "remainingTime mPausePlayTime " + mPausePlayTime);
				} else if (mPlayingAdType == 1) {
					mHandler.removeMessages(SHOW_NEXT_ADVERT_CMD);
					mPausePlayTime = System.currentTimeMillis();
					RingLog.d(TAG, "remainingTime mPausePlayTime " + mPausePlayTime);
				}
			}
		}
	}
	private void setScreenOn() {
		if(getScreenStatus()!=1 ) {
			setScreen(1);
			if (mPlayingAdType == 2) {
					onPlayerCmd("resume","");
					Long playedTime = mPausePlayTime - mStartPlayTime;
					mPlayedTime += playedTime;
					Long remainingTime = mPlayingAdDuration * 1000 - mPlayedTime;
					if (remainingTime < 0) remainingTime = Long.valueOf(mPlayingAdDuration * 1000);
					RingLog.d(TAG, "Player is resumed, Duration= " + mPlayingAdDuration +"playeredTime = " +mPlayedTime+ "remainingTime = " + remainingTime);
					mHandler.sendEmptyMessageDelayed(SHOW_NEXT_ADVERT_CMD, remainingTime);
					mStartPlayTime = System.currentTimeMillis();
			} else if (mPlayingAdType == 1) {
					Long playedTime = mPausePlayTime - mStartPlayTime;
					mPlayedTime += playedTime;
					Long remainingTime = mPlayingAdDuration * 1000 - mPlayedTime;
					if (remainingTime < 0) remainingTime = Long.valueOf(mPlayingAdDuration * 1000);
					RingLog.d(TAG, "Player is resumed, remainingTime = " + remainingTime);
					mHandler.sendEmptyMessageDelayed(SHOW_NEXT_ADVERT_CMD, remainingTime);
					mStartPlayTime = System.currentTimeMillis();
			}
			if (mCameraService != null && mCameraService.isRecording() && !CommonUtil.haveUdisk()) {
				RingLog.d(TAG, "Player is resumed, now resume record");
				mCameraService.cameraRecordResume(surfaceHolder_record.getSurface());
			}
		}
	}


	/*设置当前时间*/
	private void setCurrentTime(){
		if(System.currentTimeMillis()<0){
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR,1970);
			c.set(Calendar.MONTH,1);
			c.set(Calendar.DAY_OF_MONTH,1 );
			c.set(Calendar.HOUR_OF_DAY, 1);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			long when = c.getTimeInMillis();
			if(when / 1000 < Integer.MAX_VALUE) {
				((AlarmManager) getSystemService(Context.ALARM_SERVICE)).setTime(when);
			}
		}
		/*
		if(CommonUtil.compareDateState("2015-01-01 00:00:00","2016-01-01 23:59:00")){
			long when = DevRing.cacheManager().spCache("SysTime").getLong("timeInMillis",0);
			if(when!=0){
					when +=  30*1000;
					if(when / 1000 < Integer.MAX_VALUE){
						((AlarmManager)getSystemService(Context.ALARM_SERVICE)).setTime(when);
					}
			}
		}
		*/
	}
	/*保存当前时间*/
	private void saveCurrentTime(){
		DevRing.cacheManager().spCache("SysTime").put("timeInMillis",System.currentTimeMillis());
	}

	private void showAdvertInfo(AdvertInfoData data){
		if(data!=null) {
			if (data.getvTime() == 1) { /*需要显示时间信息*/
				showTimeInfo(true);
			} else {
				showTimeInfo(false);
			}
			if (data.getWeather() == 1) {
				showWeatherInfo(true);
			} else {
				showWeatherInfo(false);
			}
			if (data.getWriting() != null && !data.getWriting().isEmpty()) {
				if (CommonUtil.compareDateState(data.getStartTime(), data.getEndTime())) { /*需要显示滚动信息*/
					setAdInfoScroll(true, data);
				} else {
					setAdInfoScroll(false, data);
				}
			} else {
				setAdInfoScroll(false, data);
			}
			if (data.getUrgentwriting() != null && !data.getUrgentwriting().isEmpty()) {/*需要显示紧急通知*/
				showUrgentInfo(data, MediaPlayerActivity.this);
			}
		}else{
			showTimeInfo(false);
			showWeatherInfo(false);
			setAdInfoScroll(false, data);
		}
	}

    //接收事件总线发来的事件
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN) //如果使用默认的EventBus则使用此@Subscribe
    public void onAlarmEvent(AppEvent event) {
		int msg = event.getMessage();
		Log.i(TAG,"received event = " + msg);
		switch (msg) {
			case AppEvent.ADVERT_DOWNLOAD_FINISHED_EVENT:
				Log.i(TAG,"received event data = " + event.getData());
				break;
			case AppEvent.ADVERT_LIST_UPDATE_EVENT:
				break;
			case AppEvent.ADVERT_LIST_DOWNLOAD_FINISHED_EVENT:
				break;
			case AppEvent.POWER_SET_ALARM_EVENT:
				if ("POWER_OFF_ALARM".equals(event.getData())) {
					handler.post(runableSetPowerOff);
					DevRing.cacheManager().spCache("PowerStatus").put("status","off");

				}else if("POWER_ON_ALARM".equals(event.getData())){
					DevRing.cacheManager().spCache("PowerStatus").put("status","on");
					handler.post(runableSetPowerOn);
					//CommonUtil.wakeup(MediaPlayerActivity.this);
					//mHandler.sendEmptyMessageDelayed(REBOOT_CMD,1000*5);
					CommonUtil.reboot(MediaPlayerActivity.this);
				}else if("SET_WLAN_ON_ALARM".equals(event.getData())){
					CommonUtil.runCmd("ifconfig eth0 up");
				}else if("SET_WLAN_OFF_ALARM".equals(event.getData())){
					CommonUtil.runCmd("ifconfig eth0 down");
				}
				break;
			case AppEvent.SET_POWER_OFF:
				handler.post(runableSetPowerOff);
				DevRing.cacheManager().spCache("PowerStatus").put("status","off");
				break;
			case AppEvent.SET_POWER_ON:
				DevRing.cacheManager().spCache("PowerStatus").put("status","on");
				handler.post(runableSetPowerOn);
				break;
			/*
			case AppEvent.POWER_UPDATE_ALARM_EVENT:
				PowerOnOffData powerOnOffData = (PowerOnOffData)event.getData();
				mHandler.removeMessages(SET_POWER_ALARM_CMD);
				SetPowerAlarm(powerOnOffData.getStartTime(),powerOnOffData.getEndTime());
				break;
			case AppEvent.NETWORK_ONOFF_ALARM_EVENT:
				NetworkOnOffData networkOnOffData = (NetworkOnOffData)event.getData();
				mHandler.removeMessages(SET_NETWORK_ALARM_CMD);
				SetNetworkAlarm(networkOnOffData.getNetworkStartTime(),networkOnOffData.getNetworkEndTime());
				break;
			*/
			default:
				break;
		}
    }

	AdListEventListener mAdListEventListener = new AdListEventListener() {
		@Override
		public void onAdListUpdate() {
			removeImageView();
			initAdView();
			initAdFiles();
			mPlayListManager.setPlayListUpdate("1");
			mHandler.removeMessages(START_CAMERACHECK_CMD);
			mHandler.sendEmptyMessageDelayed(START_CAMERACHECK_CMD, 30 * 1000);
		}
		@Override
		public void onInfoUpdate(AdvertInfoData data){
			showAdvertInfo(data);
		}
		@Override
		public void onWeatherUpdate(AdvertWeatherData data){
			setWeatherInfo(data);
		}
		@Override
		public void onRecoderStart(){
			mHandler.sendEmptyMessageDelayed(START_CAMERACHECK_CMD,  1000);
		}
		@Override
		public void onTemplateUpdate(){
            onPlayerCmd("stop","");
			removeImageView();
			initAdView();
			initAdFiles();
		}
		@Override
		public void onPrintInfo(){
			if(mElevatorDoorManager!=null) mElevatorDoorManager.printDoorManagerInfo();
			if(mElevatorStatusManager!=null) mElevatorStatusManager.printDoorStatusInfo();
			Log.i(TAG,"Screen Status = " + getScreenStatus() + "player status = " + getPlayerState() + "isPowerOff = " + isPowerOff);
		}
	};

	ElevatorEventListener mElevatorEventListener =new  ElevatorEventListener(){
		@Override
		public void onElevatorUp(){
			Log.i(TAG, "onElevatorUp:"  + "screenStatus = " + screenStatus);
			if(!mElevatorDoorManager.isDoorOpened()) {
				if (screenStatus == 0) {
					mHandler.removeMessages(SET_LIFT_STOP_CMD);
					mHandler.sendEmptyMessage(SET_SCREEN_ON_CMD);
				} else if (screenStatus == 1) {
					mHandler.removeMessages(SET_LIFT_STOP_CMD);
				}
			}
		}
		@Override
		public void onElevatorDown(){
			Log.i(TAG, "onElevatorDown:"  + "screenStatus = " + screenStatus);
			if(!mElevatorDoorManager.isDoorOpened()) {
				if (screenStatus == 0) {
					mHandler.removeMessages(SET_LIFT_STOP_CMD);
					mHandler.sendEmptyMessage(SET_SCREEN_ON_CMD);
				} else if (screenStatus == 1) {
					mHandler.removeMessages(SET_LIFT_STOP_CMD);
				}
			}
		}
		@Override
		public void onElevatorStop(){
			Log.i(TAG, "onElevatorStop:"  + "screenStatus = " + screenStatus);
			if(CommonUtil.isForeground(MediaPlayerActivity.this,"com.grandartisans.advert.activity.MediaPlayerActivity")) {
				mHandler.removeMessages(SET_SCREEN_ON_CMD);
				setScreenOff(mContinuePlayWhenScreenOff);
			}
		}
	};
	ElevatorDoorEventListener mElevatorDoorEventListener = new ElevatorDoorEventListener(){
		@Override
		public void onElevatorDoorOpen(){
			Log.i(TAG, "onElevatorDoorOpen:"  + "screenStatus = " + screenStatus);
			if (screenStatus == 1 || screenStatus ==2) {
				mHandler.removeMessages(SET_SCREEN_ON_CMD);
				setScreenOff(mContinuePlayWhenScreenOff);
			}
			mElevatorStatusManager.setStatusDefault();
		}
		@Override
		public void onElevatorDoorClose(){
			Log.i(TAG, "onElevatorDoorClose:"  + "screenStatus = " + screenStatus);
			if( screenStatus == 0 ) {
				screenStatus = 2;
				if(CommonUtil.getGsensorEnabled()!=0) {
					mHandler.sendEmptyMessageDelayed(SET_SCREEN_ON_CMD, 1000);
					mHandler.removeMessages(SET_LIFT_STOP_CMD);
					mHandler.sendEmptyMessageDelayed(SET_LIFT_STOP_CMD, 1000 * 60);
				}else{
					mHandler.sendEmptyMessageDelayed(SET_SCREEN_ON_CMD, 1000);
				}
			}
		}
		@Override
		public void onElevatorError(){
			CommonUtil.reboot(MediaPlayerActivity.this);
		}
	};

	RecorderEventListener mRecorderEventListener = new RecorderEventListener(){
		@Override
		public void onRecordFinished(String path){
			ReportAdListUpdate(path);
		}
		@Override
		public void onRecordStart(){
			if(getScreenStatus()==0){
				if (mCameraService!=null && mCameraService.isRecording() && !CommonUtil.haveUdisk()) {
					RingLog.d(TAG, "onRecordStart Screen is off, so pause record");
					mPlayerHandler.sendEmptyMessageDelayed(RECORDER_PAUSE_CMD,1000);
				}
			}
		}
	};


	private void checkCamera() {
		if(CommonUtil.haveUdisk()||mPlayListManager.isAdListUpdated()) {
			mHandler.sendEmptyMessageDelayed(START_SERVICE_CMD, 3000);
		}

	}

	private void initCameraService() {
		initCamServiceConnection();
		Intent intent = new Intent(MediaPlayerActivity.this, CameraService.class);
		startService(intent);
		bindService(intent, mCamServiceConn, getApplicationContext().BIND_AUTO_CREATE);
	}

	private void initCamServiceConnection() {
		mCamServiceConn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				CameraService.CamBinder binder = (CameraService.CamBinder) service;
				mCameraService = binder.getService();
				mHandler.sendEmptyMessage(START_FIRST_RECORD_CMD);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				mCameraService = null;
			}
		};
	}

	private void startFirstRecord() {
		if (mCameraService != null && !mCameraService.isRecording()) {
			int cameraNumber = mCameraService.getCameraNumber();
			RingLog.d(TAG, "Camera numbers is " + cameraNumber);
			if(cameraNumber>0) {
				mCameraService.registerListener(mRecorderEventListener);
				mCameraService.cameraRecordStart(surfaceHolder_record.getSurface());
			}
		}
		//initCameraView();

	}

    private TerminalAdvertPackageVo getScheduleTimesCache() {
        return ScheduleTimesCache.get();
    }
    private void initAdView() {
        List<TemplateReginVo> regionList = mPlayListManager.getTemplateList();
        boolean onlyMainPos = true;
        if(regionList!=null) {
			if(regionList.size()>1){
				onlyMainPos = false;
			}
			RingLog.i("Use schedule times regionList size = " + regionList.size() + "onlyMainPos  = " + onlyMainPos);
            for(int i=0;i<regionList.size();i++){
				TemplateReginVo region = regionList.get(i);
                String regLocation = region.getTemplateRegion().getLocation();
                String[] regLocations = regLocation.split(",");
                String videoType = region.getTemplateRegion().getVideoType();
                int regWidth = region.getTemplateRegion().getWidth();
                int regHeight = region.getTemplateRegion().getHeight();
                int marginLeft = Integer.valueOf(regLocations[0]);
                int marginTop = Integer.valueOf(regLocations[1]);
                Long adType = Long.valueOf(videoType);
                Long adPosId = region.getTemplateRegion().getId();
				if (onlyMainPos){
					mMainPositionId = adPosId;
					set_view_layout(adType, regWidth, regHeight, marginLeft,
							marginTop, adPosId);
				}else {
					set_view_layout(adType, regWidth, regHeight, marginLeft,
							marginTop, adPosId);
					if(adType==2) {
						mMainPositionId = adPosId;
					}
				}
            }
        }
    }

    private void set_view_layout(long viewType, int width, int height, int left, int top,Long adposid) {
        RingLog.d(TAG, "set_view_layout viewType = " + viewType + "width = " + width + "height = " + height + "left = " + left + "top = " + top );
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.setMargins(left, top, 0, 0);
        if (viewType == 1) {// 图片控件
            RingLog.d(TAG, "set image view");
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setTag(R.id.image_key,adposid);
            relativeLayout.addView(imageView);
        } else if (viewType == 2 || viewType ==3) { // 视频控件
			RingLog.d(TAG, "set surface view");
			surface.setLayoutParams(layoutParams);
			surface.requestLayout();
			relativeLayout.requestLayout();
			/*
			surfaceHolder = surface.getHolder();// SurfaceHolder是SurfaceView的控制接口
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			*/
        }
    }
	private void showImageWithPath(ImageView imageView,String path,long duration,boolean isEncrypt) {
		if (path != null && !path.isEmpty()) {
			//File file = new File(path);
			//Glide.with(getApplicationContext()).load(file).into(imageView);
			if(isEncrypt) {
				RingLog.d("dec file start ");
				InputStream fis = encApi.DecryptFile(path);
				if (fis != null) {
					RingLog.d("dec file sucess ");
					Bitmap bm = BitmapFactory.decodeStream(fis);
					if (bm != null) imageView.setImageBitmap(bm);
				} else {
					RingLog.d("dec file failed ");
				}
			}else{
				Bitmap bm = BitmapFactory.decodeFile(path);
				if(bm!=null) imageView.setImageBitmap(bm);
			}
		}
		mHandler.removeMessages(SHOW_NEXT_ADVERT_CMD);
		mHandler.sendEmptyMessageDelayed(SHOW_NEXT_ADVERT_CMD,duration*1000);
	}
    private void showImageWithIndex(ImageView imageView) {
		Long posid = (Long)imageView.getTag(R.id.image_key);
		PlayingAdvert item  = mPlayListManager.getValidPlayUrl(posid,false);
		if(item!=null) {
			String path = item.getPath();
			boolean isEncrypt = item.isEncrypt();
			if(isEncrypt) {
				RingLog.d("dec file start ");
				InputStream fis = encApi.DecryptFile(path);
				if (fis != null) {
					RingLog.d("dec file sucess ");
					Bitmap bm = BitmapFactory.decodeStream(fis);
					if (bm != null) imageView.setImageBitmap(bm);
				} else {
					RingLog.d("dec file failed ");
				}
			}else{
				Bitmap bm = BitmapFactory.decodeFile(path);
				if(bm!=null) imageView.setImageBitmap(bm);
			}
			Message msg = new Message();
			msg.what = UPDATE_IMAGE_AD_CMD;
			msg.obj = imageView;
			mHandler.sendMessageDelayed(msg,item.getDuration()*1000);
		}
    }

	private void removeImageView() {
		int childCount = relativeLayout.getChildCount();
		if (childCount <= 1)
			return;
		Log.i(TAG,"removeImageView childCount = " + childCount);
		for (int i = childCount-1; i >=0 ; i--) {
			if (relativeLayout.getChildAt(i) instanceof ImageView) {
				Log.i(TAG,"removeImageView i = " + i);
				ImageView imageView = (ImageView) relativeLayout.getChildAt(i);
				relativeLayout.removeView(imageView);
				mHandler.removeMessages(UPDATE_IMAGE_AD_CMD);
			}
		}
		//relativeLayout.requestLayout();
	}

    private void setAdInfoScroll(Boolean visibile ,AdvertInfoData data) {
		if(visibile==true) {
			marqueeTextView.setVisibility(View.VISIBLE);
			String writingText = data.getWriting();
			Long velocity = Long.valueOf(1);
			if (data.getVelocity() != null) velocity = data.getVelocity();
			String backColor = data.getBackColor();
			String fontColor = data.getFontColor();
			// 获取屏幕管理器

			Display display = getWindowManager().getDefaultDisplay();
			DisplayMetrics metrics = new DisplayMetrics();
			display.getMetrics(metrics);
			int win_width = metrics.widthPixels;
			int win_height = metrics.heightPixels;
			/*
			ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(marqueeTextView.getLayoutParams());
			// 设置滚动区域位置
			marginLayoutParams.setMargins(0, win_height - 50, 0, 0);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginLayoutParams);
			// 设置滚动区域高度
			layoutParams.height = 50;
			// 设置滚动区域宽度
			layoutParams.width = win_width;

			// 设置MarqueeTextView参数
			marqueeTextView.setLayoutParams(layoutParams);
			*/
			marqueeTextView.setBackgroundColor(Color.parseColor(backColor));
			marqueeTextView.setTextColor(Color.parseColor(fontColor));
			marqueeTextView.setScrollWidth(win_width);
			marqueeTextView.setCoordinateX(win_width);
			marqueeTextView.setCoordinateY(40);
			marqueeTextView.setSpeed(velocity.intValue());
			marqueeTextView.setText(writingText);
		}else{
			marqueeTextView.setVisibility(View.GONE );
			marqueeTextView.setText("");
		}
	}

	private void showTimeInfo(boolean visiable){
		if(visiable==true) {
            linearLayoutText.setVisibility(View.VISIBLE);
			textViewTime.setVisibility(View.VISIBLE);
			String CurrentTime = CommonUtil.getCurrentTimeString(System.currentTimeMillis());
			textViewTime.setText(CurrentTime);
			mHandler.sendEmptyMessageDelayed(UPDATE_TIME_INFO_CMD,3000);
		}else{
            linearLayoutText.setVisibility(View.GONE);
			textViewTime.setVisibility(View.GONE);
		}
	}
	private void updateTimeInfo(){
		String CurrentTime = CommonUtil.getCurrentTimeString(System.currentTimeMillis());
		textViewTime.setText(CurrentTime);
		mHandler.removeMessages(UPDATE_TIME_INFO_CMD);
		mHandler.sendEmptyMessageDelayed(UPDATE_TIME_INFO_CMD,3000);
	}

	private void showUrgentInfo(AdvertInfoData data,Context context){
			if((mUrgentInfoDialog==null||!mUrgentInfoDialog.isShowing())){
				View view = View.inflate(getApplicationContext(), R.layout.dialog_layout, null);
				mUrgentInfoDialog = new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.bt_ugrentinfo_title_string)).
						setView(view).create();
				//distanceSetDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				mUrgentInfoDialog.show();
				TextView messageTV  = (TextView) mUrgentInfoDialog.findViewById(R.id.distance_dialog);
				String message = data.getUrgentwriting();
				if(messageTV!=null ) messageTV.setText(message);
				else Log.i(TAG,"messageTV view is null");

				//mUrgentInfoDialog.setOnKeyListener(keylistener);
				mHandler.sendEmptyMessageDelayed(HIDE_UGRENT_INFO_CMD,data.getUrgenttime()*1000);
			}
	}
	private void HideUgrentInfo(){
		if(mUrgentInfoDialog!=null && mUrgentInfoDialog.isShowing()){
			mUrgentInfoDialog.dismiss();
		}
	}
	private void showWeatherInfo(boolean visiable){
		if(visiable==true){
			linearLayoutText.setVisibility(View.VISIBLE);
			imageViewWeather.setVisibility(View.VISIBLE);
			textViewTemp.setVisibility(View.VISIBLE);
			textViewTempCode.setVisibility(View.VISIBLE);
		}else{
			imageViewWeather.setVisibility(View.GONE);
			textViewTemp.setVisibility(View.GONE);
			textViewTempCode.setVisibility(View.GONE);
		}
	}
	private void setWeatherInfo(AdvertWeatherData data){
		if(data!=null){
			Long weatherType = data.getType();
			Long celcius = data.getCelcius();
			ViewUtils.setWeatherIcon(getResources(), imageViewWeather, weatherType);
			textViewTemp.setText(String.valueOf(celcius));
		}
	}
	private void ReportAdListUpdate(String path)
	{
		EventParameter parameter = new EventParameter();
		String 	deviceId = SystemInfoManager.getDeviceId(getApplicationContext());
		parameter.setSn(deviceId.toUpperCase());
		parameter.setSessionid(CommonUtil.getRandomString(50));
		parameter.setTimestamp(System.currentTimeMillis());
		parameter.setToken(UpgradeService.mToken);
		parameter.setApp(Utils.getAppPackageName(MediaPlayerActivity.this));
		parameter.setEvent("advertRecord");
		parameter.setEventtype(4000);

		parameter.setMac(CommonUtil.getEthernetMac());

		RecorderUpdateEventData eventData = new RecorderUpdateEventData();
		eventData.setPath(path);
		eventData.setTemplateid(0);
		parameter.setEventData(eventData);
		//parameter.setIp();
		parameter.setTimestamp(System.currentTimeMillis());
		AdvertModel mIModel = new AdvertModel();
		DevRing.httpManager().commonRequest(mIModel.reportEvent(parameter), new CommonObserver<ReportInfoResult>() {
			@Override
			public void onResult(ReportInfoResult result) {
				RingLog.d("report Advert List Updated Event ok status = " + result.getStatus() );
				mPlayListManager.setPlayListUpdate("0");
			}

			@Override
			public void onError(int i, String s) {
				RingLog.d("reportEvent error i = " + i + "msg = " + s );
			}
		},null);
	}
}
