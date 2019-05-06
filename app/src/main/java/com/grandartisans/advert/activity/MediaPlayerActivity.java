package com.grandartisans.advert.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.grandartisans.advert.common.ScheduleTimesCache;
import com.grandartisans.advert.dbutils.PlayRecord;
import com.grandartisans.advert.dbutils.dbutils;
import com.grandartisans.advert.interfaces.AdListEventListener;
import com.grandartisans.advert.interfaces.ElevatorDoorEventListener;
import com.grandartisans.advert.interfaces.ElevatorEventListener;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.grandartisans.advert.model.entity.event.AppEvent;
import com.grandartisans.advert.model.entity.post.EventParameter;
import com.grandartisans.advert.model.entity.post.PlayerStatusParameter;
import com.grandartisans.advert.model.entity.post.ReportEventData;
import com.grandartisans.advert.model.entity.res.AdvertFile;
import com.grandartisans.advert.model.entity.res.AdvertPositionVo;
import com.grandartisans.advert.model.entity.res.AdvertVo;
import com.grandartisans.advert.model.entity.res.DateScheduleVo;
import com.grandartisans.advert.model.entity.res.PowerOnOffData;
import com.grandartisans.advert.model.entity.res.ReportInfoResult;
import com.grandartisans.advert.model.entity.res.TemplateRegion;
import com.grandartisans.advert.model.entity.res.TerminalAdvertPackageVo;
import com.grandartisans.advert.model.entity.res.TimeScheduleVo;
import com.grandartisans.advert.service.AdvertInfoService;
import com.grandartisans.advert.service.CameraService;
import com.grandartisans.advert.service.NetworkService;
import com.grandartisans.advert.service.UpgradeService;
import com.grandartisans.advert.utils.AdPlayListManager;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.ElevatorDoorManager;
import com.grandartisans.advert.utils.ElevatorStatusManager;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.SystemInfoManager;
import com.grandartisans.advert.utils.Utils;
import com.grandartisans.advert.utils.ViewUtils;
import com.grandartisans.advert.view.MarqueeTextView;
import com.grandartisans.advert.view.MySurfaceView;
import com.ljy.devring.DevRing;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.other.RingLog;
import com.prj.utils.PrjSettingsManager;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import gartisans.hardware.pico.PicoClient;

public class MediaPlayerActivity extends Activity implements SurfaceHolder.Callback,SrsEncodeHandler.SrsEncodeListener {
	private final String TAG = "MediaPlayerActivity";
	private MediaPlayer mMediaPlayer;
	private MySurfaceView surface;
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
	private final int INIT_INFO_SERVICE_CMD = 100024;
	private final int SET_ADVERT_INFO_CMD = 100025;
	private final int UPDATE_ADVERT_WEATHER_CMD = 100026;
	private final int REFRESH_ADVERT_INFO_CMD = 100027;

    private final int UPDATE_IMAGE_AD_CMD = 100028;
    private final int SHOW_NEXT_ADVERT_CMD = 100029;

	private String mMode ="";

	static final int PLAYER_STATE_INIT = 0;
	static final int PLAYER_STATE_PLAYING = 1;
	static final int PLAYER_STATE_PAUSED = 2;
	static final int PLAYER_STATE_STOPED = 3;
	static final int PLAYER_STATE_RELEASED = 4;
	private int mPlayState = PLAYER_STATE_INIT;

	public static SrsCameraView mCameraView;
	public static SrsPublisher mPublisher;
	private CameraService mCameraService;
	private NetworkService mNetworkService;
	private AdvertInfoService mInfoService;
	private ServiceConnection mCamServiceConn;
	private ServiceConnection mNetServiceConn;
	private ServiceConnection mInfoServiceConn;
	private float mInitZ = 0;
	private ElevatorStatusManager mElevatorStatusManager;
	private ElevatorDoorManager mElevatorDoorManager;


	private int mReportEventTimeInterval=5*60*1000;
	private int mIntentId = 0;

	private boolean  activate_started = false;
	private boolean IsCameraServiceOn = false;
	private boolean IsNetworkServiceOn = false;
	public static boolean firstStartRecord = false;


	AdPlayListManager mPlayListManager = null;

    private TerminalAdvertPackageVo mTerminalAdvertPackageVo;
    private long mMainPositionId = 0;

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
					initFirstPlayer();
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
					SetPowerAlarm();
					break;
				case START_PUSH_CMD:
					startPush();
					break;
				case STOP_PUSH_CMD:
                    break;
				case START_REPORT_PLAYSTATUS_CMD:
					ReportPlayStatus();
					break;
				case START_SERVICE_CMD:
					initService();
					break;
				case SET_LIFT_STOP_CMD:
					//setLiftState(LIFT_STATE_STOP);
					setScreenOff();
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
					onVideoPlayCompleted();
					break;
				case INIT_INFO_SERVICE_CMD:
					initAdInfoService();
					break;
				case SET_ADVERT_INFO_CMD:
					setAdInfo();
					break;
				case UPDATE_ADVERT_WEATHER_CMD:
					updateAdWeather();
					break;
				case REFRESH_ADVERT_INFO_CMD:
					refreshAdInfo();
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
	private void SetPowerAlarm(){
			//initPowerOffAlarm(22,00,00);// 设置定时关机提醒
		long startTime = DevRing.cacheManager().spCache("PowerAlarm").getLong("startTime",0);
		long endTime = DevRing.cacheManager().spCache("PowerAlarm").getLong("endTime",0);
		int startHour = 07;
		int startMinute = 00;
		int startSecond = 00;
		int endHour = 21;
		int endMinute = 30;
		int endSecond = 00;
		if(startTime!=0) {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String t1 = format.format(startTime);
			startHour = Integer.valueOf(t1.substring(0, 2));
			startMinute = Integer.valueOf(t1.substring(3, 5));
			startSecond = Integer.valueOf(t1.substring(6, 8));
		}
		if(endTime!=0){
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			String t1 = format.format(endTime);
			endHour = Integer.valueOf(t1.substring(0, 2));
			endMinute = Integer.valueOf(t1.substring(3, 5));
			endSecond = Integer.valueOf(t1.substring(6, 8));
		}

		initPowerOffAlarm(endHour,endMinute,endSecond);// 设置定时关机提醒

		initPowerOnAlarm(startHour,startMinute,startSecond);//设置定时开机提醒

	}

	Runnable runableSetPowerOff = new Runnable() {
		@Override
		public void run() {
			lock.lock();
            if(isPowerOff==false) {
				isPowerOff = true;
				setScreenOff();
				mHandler.sendEmptyMessageDelayed(START_REPORT_EVENT_CMD,mReportEventTimeInterval);
			}
            lock.unlock();

			//initPowerOnAlarm(13,10,00);
		}
	};

	Runnable runableSetPowerOn = new Runnable() {
		@Override
		public void run() {
			lock.lock();
			if(isPowerOff==true) {
				isPowerOff = false;
				setScreen(1);
				onVideoPlayCompleted();
			}
			lock.unlock();
			//initPowerOnAlarm(13,10,00);
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_surface_player);
		Log.i(TAG,"onCreate");
		setCurrentTime();
		keepScreenWake();

		mPlayListManager = AdPlayListManager.getInstance(getApplicationContext());
		mPlayListManager.registerListene(mAdListEventListener);

		handler = new Handler();
		mHandler.sendEmptyMessageDelayed(SET_POWER_ALARM_CMD,1000*60*10);
		prjmanager = PrjSettingsManager.getInstance(this);

		mMode = CommonUtil.getModel();
		if(mMode.equals("GAPEDS4A3") || mMode.equals("GAPEDS4A6")){
			prjmanager.setBrightness(10);
		}

		mElevatorStatusManager = new ElevatorStatusManager(this,mMode,Float.valueOf(prjmanager.getGsensorDefault()));
		mElevatorStatusManager.registerListener(mElevatorEventListener);
		initEventBus();//注册事件接收

		initTFMini();//初始化激光测距模块

		mPlayListManager.init();

		/*
		Intent intentService = new Intent(MediaPlayerActivity.this,UpgradeService.class);
		startService(intentService);
		*/


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

		//mHandler.sendEmptyMessage(START_REPORT_PLAYSTATUS_CMD);
	}

	private void initView(){
        // 判断有无排期模板缓存 有则进行排板 无则使用默认模板
        TerminalAdvertPackageVo tapvo = getScheduleTimesCache();
        relativeLayout = (RelativeLayout) findViewById(R.id.rootframeview);
		surface = (MySurfaceView) findViewById(R.id.surface);
		mImageMain =(ImageView)findViewById(R.id.image_main);
		mImageMain.setVisibility(View.INVISIBLE);

		surfaceHolder = surface.getHolder();// SurfaceHolder是SurfaceView的控制接口
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mCameraView = (SrsCameraView) findViewById(R.id.glsurfaceview_camera);
		mCameraView.setVisibility(View.GONE);

        if (tapvo != null) {
            RingLog.d(TAG, "the schedule times is not none, let init it");
            mTerminalAdvertPackageVo = tapvo;
            initAdView();
        }else {
            RingLog.d(TAG, "the schedule times is none, let use default view");
        }
		marqueeTextView = (MarqueeTextView) findViewById(R.id.tv_scroll);
		marqueeTextView.setVisibility(View.GONE);
		linearLayoutText = (LinearLayout) findViewById(R.id.ll_info);
		linearLayoutText.setVisibility(View.GONE);
		imageViewWeather = (ImageView) findViewById(R.id.icon_weather);
		textViewTime = (TextView) findViewById(R.id.tv_time);
		textViewTemp = (TextView) findViewById(R.id.tv_temperature);
		textViewTempCode = (TextView) findViewById(R.id.tv_temperature_code);
	}
	/*
	 * 初始化播放首段视频的player
	 */
	private void initFirstPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setDisplay(surfaceHolder);
		mMediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						//onVideoPlayCompleted();
					}
				});
		mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
			@Override public void onPrepared(MediaPlayer mp) {

                Log.i(TAG, "video width = " + mMediaPlayer.getVideoWidth() + "video height = " + mMediaPlayer.getVideoHeight() + "screenStatus = " + getScreenStatus());
                //mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                mMediaPlayer.start();
                if (IsCameraServiceOn && mCameraService != null && !mCameraService.getFinishStatus() && !mCameraService.getRecordStatus()) {
                    RingLog.d(TAG, "Player is resumed, now resume record");
                    mPublisher.resumeRecord();
                }
                if (getScreenStatus() == 0){
                    mMediaPlayer.pause();
					mPlayState = PLAYER_STATE_PAUSED;
			    }
			    if(mMode.equals("AOSP on p313")) {
					if (player_first_time == true) {
						player_first_time = false;
						mMediaPlayer.seekTo(mMediaPlayer.getDuration());
					}
				}
			}
		});

		mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener(){
		    @Override public boolean onError(MediaPlayer mp,int what, int extra)
            {
                Log.d(TAG, "OnError - Error code: " + what + " Extra code: " + extra);
				onVideoPlayCompleted();
                return false;
            }

        });

		mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener(){
			@Override public void onVideoSizeChanged(MediaPlayer mp,int width,int height) {
				Log.d(TAG, "setOnVideoSizeChangedListener  width: " + width + "height: " + height);
			}
		});

	}

	private void startPlay(String url) {
		try {
			//mMediaPlayer.setDataSource(url);

			Log.d(TAG, "start play: url = " + url );
			mMediaPlayer.setDataSource(MediaPlayerActivity.this,Uri.parse(url));
			mMediaPlayer.prepareAsync();
			//mMediaPlayer.prepare();
			//mMediaPlayer.start();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

	private void initAdFiles(){
        int childCount = relativeLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (relativeLayout.getChildAt(i) instanceof ImageView) {
                ImageView imageView = (ImageView) relativeLayout.getChildAt(i);
                showImageWithIndex(imageView);
            } else if(relativeLayout.getChildAt(i) instanceof MySurfaceView){
                MySurfaceView surfaceView = (MySurfaceView) relativeLayout.getChildAt(i);
                initPlayer(mMainPositionId);
            }

        }
    }

	private void initPlayer(Long posid){
        PlayingAdvert item  = mPlayListManager.getValidPlayUrl(posid);
        if(item!=null) {
			String url = item.getPath();
			Log.i(TAG,"player advertFile vType = " + item.getvType() + "url = " + item.getPath());
        	if(item.getvType()==2) { //视频广告
				//surface.setVisibility(View.VISIBLE);
				mImageMain.setVisibility(View.GONE);
				mMediaPlayer.reset();
				if (url != null && url.length() > 0) {
					startPlay(url);
					int duration = item.getDuration();
					mHandler.sendEmptyMessageDelayed(SHOW_NEXT_ADVERT_CMD,duration*1000);
				}
			}else if(item.getvType()==1) {
				//surface.setVisibility(View.GONE);
        		mImageMain.setVisibility(View.VISIBLE);
				showImageWithPath(mImageMain,item.getPath(),item.getDuration());
			}
		}
    }
	private void onVideoPlayCompleted() {
		//get next player
		Log.i(TAG,"onVideoPlayCompleted format  isPowerOff =  " + isPowerOff);
		Long posid = mMainPositionId;
		savePlayRecord(posid);
		if(!isPowerOff) {
		    if(mMediaPlayer!=null) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
				mPlayState = PLAYER_STATE_STOPED;
            }
			initPlayer(posid);
		}else{
			setScreenOff();
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
		CommonUtil.setScreenVideoMode("1");
		String status = DevRing.cacheManager().spCache("PowerStatus").getString("status","on");
		if(status.equals("off")){
			handler.post(runableSetPowerOff);
		}
        onResumeEvent();
		if(activate_started == false) {
			mHandler.sendEmptyMessageDelayed(START_PLAYER_CMD, 3 * 1000);
			if(mMode.equals("GAPEDS4A4") || mMode.equals("GAPEDS4A6")||
					mMode.equals("GAPADS4A1") || mMode.equals("GAPADS4A2") || mMode.equals("GAPEDS4A3")) {
				mHandler.sendEmptyMessageDelayed(START_OPEN_SERIALPORT, 5 * 1000);
			}else{
				if(mElevatorDoorManager!=null)
					mElevatorDoorManager.openSerialPort();

			}
			activate_started =true;
		}
		else{
			mHandler.sendEmptyMessage(START_PLAYER_CMD);
			if(mElevatorDoorManager!=null)
				mElevatorDoorManager.openSerialPort();
		}
		Log.i(TAG,"surfaceCreated");
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

		//mHandler.sendEmptyMessageDelayed(ON_PAUSE_EVENT_CMD,1000);
	}
	private void onPauseEvent(){
		Log.i(TAG,"onPauseEvent");
		mHandler.removeMessages(START_PLAYER_CMD);

		if(mMode.equals("GAPEDS4A4") || mMode.equals("GAPEDS4A6")||
				mMode.equals("GAPADS4A1") || mMode.equals("GAPADS4A2") || mMode.equals("GAPEDS4A3")) {
			mHandler.removeMessages(START_OPEN_SERIALPORT);
		}
		mElevatorDoorManager.closeSeriaPort();
		if(mMediaPlayer!=null) {
			Log.i(TAG,"Stop mMediaPlayer");
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		surface = null;
		surfaceHolder = null;
		if (IsNetworkServiceOn) {
			unbindService(mNetServiceConn);
			IsNetworkServiceOn = false;
		}
		if (IsCameraServiceOn && mPublisher != null) {
			CameraService.cameraNeedStop = true;
			stopRecord();
		}
		setScreen(1);
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG,"onResume");
		initView();
	}

	private void onResumeEvent(){
        if(!mMode.equals("AOSP on p313")) {
            threshold_distance = Integer.valueOf(prjmanager.getDistance());
			mElevatorDoorManager.setDefaultDistance(threshold_distance);
        }
        mInitZ = Float.valueOf(prjmanager.getGsensorDefault());
        mElevatorStatusManager.setAccSensorDefaultValue(mInitZ);

        if (IsCameraServiceOn && mPublisher != null) {
        	mCameraService.restartCameraRecord();
        }else {
        	checkCamera();
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG,"onKeyDown keyCode = " + keyCode);
		if(keyCode == KeyEvent.KEYCODE_MENU) {
			menuKeyPressedCount +=1;
			startSysSetting(MediaPlayerActivity.this);
		}else if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BACKSLASH){
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
		if(mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		EventBus.getDefault().unregister(this);
		mElevatorDoorManager.closeSeriaPort();
		if (IsNetworkServiceOn) {
			unbindService(mNetServiceConn);
			IsNetworkServiceOn = false;
		}
		CameraService.cameraNeedStop = true;
		stopRecord();
		if (IsCameraServiceOn) {
			unbindService(mCamServiceConn);
			IsCameraServiceOn = false;
		}
		unbindService(mInfoServiceConn);
	}

	private void initTFMini() {
		if(mMode.equals("AOSP on p313")) {
			threshold_distance = DevRing.cacheManager().spCache("TFMini").getInt("threshold_distance",0);
		}else {
			threshold_distance = Integer.valueOf(prjmanager.getDistance());
		}
		mElevatorDoorManager = new ElevatorDoorManager(threshold_distance);
		mElevatorDoorManager.registerListener(mElevatorDoorEventListener);
	}
	private void startSysSetting(Context context) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		ComponentName cn = new ComponentName("com.projector.settings", "com.txbox.settings.launcher.systemsettings.SystemSettingsMain");
		intent.setComponent(cn);
		context.startActivity(intent);
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
		parameter.setSn(SystemInfoManager.readFromNandkey("usid").toUpperCase());
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
		parameter.setSn(SystemInfoManager.readFromNandkey("usid").toUpperCase());
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
		prjmanager.setScreen(enable);
	}

	private void setScreenOff(){
		if (getScreenStatus() != 0) {
			setScreen(0);
			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mPlayState = PLAYER_STATE_PAUSED;
				if (IsCameraServiceOn && mCameraService.getRecordStatus()) {
					RingLog.d(TAG, "Player is paused, so pause record");
					mPublisher.pauseRecord();
				}
			}
		}
	}
	private void setScreenOn() {
		if(getScreenStatus()!=1 ) {
			setScreen(1);
			if (mMediaPlayer != null && mPlayState == PLAYER_STATE_PAUSED) {
				mMediaPlayer.start();
				if (IsCameraServiceOn && mCameraService != null && !mCameraService.getFinishStatus() && !mCameraService.getRecordStatus()) {
					RingLog.d(TAG, "Player is resumed, now resume record");
					mPublisher.resumeRecord();
				}
			}
		}
	}
	private void initPowerOffAlarm(int hour,int minute,int second) {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		//cal.setTimeZone(TimeZone.getTimeZone("GTM+8:00"));
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.MILLISECOND, 00);

		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d1=new Date(cal.getTimeInMillis());
		String t1=format.format(d1);
		Log.i(TAG,"Alarm POWER OFF time = " + t1);
		Intent intent=new Intent("POWER_OFF_ALARM");
		PendingIntent pi= PendingIntent.getBroadcast(this, 0, intent,0);
		//设置一个PendingIntent对象，发送广播
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
		am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
	}


	private void initPowerOnAlarm(int hour,int minute,int second) {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		//cal.setTimeZone(TimeZone.getTimeZone("GTM+8:00"));
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.MILLISECOND, 00);

		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d1=new Date(cal.getTimeInMillis());
		String t1=format.format(d1);
		Log.i(TAG,"Alarm POWER ON time = " + t1);
		Intent intent=new Intent("POWER_ON_ALARM");
		PendingIntent pi= PendingIntent.getBroadcast(this, 0, intent,0);
		//设置一个PendingIntent对象，发送广播
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
	}

	private void ResetPowerOnAlarm(long time){
		/*
		Intent intent=new Intent("POWER_ON_ALARM");
		PendingIntent pi= PendingIntent.getBroadcast(this, 0, intent,0);
		//设置一个PendingIntent对象，发送广播
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
		am.cancel(pi);
		*/

		SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss");
		String t1=format.format(time);

		int hour = Integer.valueOf(t1.substring(0,2));
		int minute = Integer.valueOf(t1.substring(3,5));
		int second = Integer.valueOf(t1.substring(6,8));

		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		//cal.setTimeZone(TimeZone.getTimeZone("GTM+8:00"));
		Log.i(TAG,"Alarm POWER ON time = " + t1 + "hour = " + hour + "minute = " + minute + "second = " + second  + "date = " + date.toString());
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.MILLISECOND, 00);

		Intent intent1=new Intent("POWER_ON_ALARM");
		PendingIntent pi1= PendingIntent.getBroadcast(this, 0, intent1,0);
		//设置一个PendingIntent对象，发送广播
		AlarmManager am1=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
		am1.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi1);
	}
	private void ResetPowerOffAlarm(long time){

		/*
		Intent intent=new Intent("POWER_OFF_ALARM");
		PendingIntent pi= PendingIntent.getBroadcast(this, 0, intent,0);
		//设置一个PendingIntent对象，发送广播
		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
		am.cancel(pi);
		*/


		SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss");
		String t1=format.format(time);

		int hour = Integer.valueOf(t1.substring(0,2));
		int minute = Integer.valueOf(t1.substring(3,5));
		int second = Integer.valueOf(t1.substring(6,8));
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		//cal.setTimeZone(TimeZone.getTimeZone("GTM+8:00"));
		Log.i(TAG,"Alarm POWER OFF time = " + t1 + "hour = " + hour + "minute = " + minute + "second = " + second  + "date = " + date.toString());
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.MILLISECOND, 00);

		Intent intent1=new Intent("POWER_OFF_ALARM");
		PendingIntent pi1= PendingIntent.getBroadcast(this, 0, intent1,0);


		//设置一个PendingIntent对象，发送广播
		AlarmManager am1=(AlarmManager)getSystemService(ALARM_SERVICE);
		//获取AlarmManager对象
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
		am1.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi1);

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

				}else if("POWER_ON_ALARM".equals(event.getData())){
					saveCurrentTime();
					CommonUtil.reboot(MediaPlayerActivity.this);
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
			case AppEvent.POWER_UPDATE_ALARM_EVENT:
				PowerOnOffData powerOnOffData = (PowerOnOffData)event.getData();
				mHandler.removeMessages(SET_POWER_ALARM_CMD);
				ResetPowerOffAlarm(powerOnOffData.getEndTime());
				ResetPowerOnAlarm(powerOnOffData.getStartTime());
			case AppEvent.SHOW_ADVERT_INFO:
				if (mInfoService == null) {
					mHandler.sendEmptyMessage(INIT_INFO_SERVICE_CMD);
				} else {
					mHandler.sendEmptyMessage(REFRESH_ADVERT_INFO_CMD);
				}
				break;
			case AppEvent.SET_ADVERT_INFO:
				mHandler.sendEmptyMessage(SET_ADVERT_INFO_CMD);
				break;
			case AppEvent.UPDATE_ADVERT_WEATHER:
				mHandler.sendEmptyMessage(UPDATE_ADVERT_WEATHER_CMD);
				break;
			case AppEvent.CLOSE_ADVERT_INFO:
				marqueeTextView.setVisibility(View.GONE);
				linearLayoutText.setVisibility(View.GONE);
				mInfoService.closeTimer();
				break;
			default:
				break;
		}
    }

	AdListEventListener mAdListEventListener = new AdListEventListener() {
		@Override
		public void onAdListUpdate() {
			removeImageView();
			TerminalAdvertPackageVo tapvo = getScheduleTimesCache();
			if (tapvo != null) {
				RingLog.d(TAG, "the schedule times is not none, let init it");
				mTerminalAdvertPackageVo = tapvo;
				initAdView();
				initAdFiles();
			}
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
				setScreenOff();
			}
		}
	};
	ElevatorDoorEventListener mElevatorDoorEventListener = new ElevatorDoorEventListener(){
		@Override
		public void onElevatorDoorOpen(){
			Log.i(TAG, "onElevatorDoorOpen:"  + "screenStatus = " + screenStatus);
			if (screenStatus == 1 || screenStatus ==2) {
				mHandler.removeMessages(SET_SCREEN_ON_CMD);
				setScreenOff();
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
					mHandler.sendEmptyMessageDelayed(SET_LIFT_STOP_CMD, 1000 * 60*2);
				}else{
					mHandler.sendEmptyMessageDelayed(SET_SCREEN_ON_CMD, 1000);
				}
			}
		}
	};
	@Override
	public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
		handleException(e);
	}

	private void handleException(Exception e) {
		try {
			RingLog.d(TAG, e.getMessage());
		} catch (Exception e1) {
			//
		}
	}

	@Override
	public void onNetworkWeak() {
		RingLog.d(TAG, "The network is weak");
	}

	@Override
	public void onNetworkResume() {}

	private void checkCamera() {
		// 初始化CameraView 并设置为不可见
        int num = Camera.getNumberOfCameras();
        RingLog.d(TAG, "Camera numbers is " + num);
        if (num != 0) {
            RingLog.d(TAG, "Camera is on");
            firstStartRecord = true;
            mHandler.sendEmptyMessageDelayed(START_SERVICE_CMD, 3000);
        } else {
            RingLog.d(TAG, "None camera");
            mHandler.removeMessages(START_CAMERACHECK_CMD);
            mHandler.sendEmptyMessageDelayed(START_CAMERACHECK_CMD,10*1000);
        }
	}

	private void initCameraView() {
		// 初始化相机并打开
		mCameraView.setVisibility(View.VISIBLE);
		mPublisher = new SrsPublisher(mCameraView);
		// 编码状态回调
		mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
		// 预览分辨率
		mPublisher.setPreviewResolution(1280, 720);
		// 推流分辨率
		mPublisher.setOutputResolution(1280, 720);
		// 传输率
		mPublisher.setVideoHDMode();
		// 将摄像头预览最小化
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(1, 1);
		layoutParams.setMargins(0, 0, 0, 0);
		mCameraView.setLayoutParams(layoutParams);
		// 调整摄像头角度
		if(mMode.equals("GAPADS4A1")) {
			mCameraView.setPreviewOrientation(0);
		}else{
			mCameraView.setPreviewOrientation(0);
		}
		mPublisher.startCamera();
	}

	private void startPush() {
		if (firstStartRecord) {
			// 等待第一次录像完成
			return;
		}
		if (mCameraService.getFinishStatus() && !mCameraService.getUploadStatus() && !mCameraService.recordUploadSuccess()) {
			// 开始上传录像
			mCameraService.uploadRecord();
		}
	}

	private void stopRecord() {
		if (mPublisher != null) {
			mPublisher.stopRecord();
		}
	}


	private void initService() {
		initCameraService();
		initNetworkService();
	}

	private void initCameraService() {
		initCamServiceConnection();
		Intent intent = new Intent(MediaPlayerActivity.this, CameraService.class);
		startService(intent);
		bindService(intent, mCamServiceConn, getApplicationContext().BIND_AUTO_CREATE);
		IsCameraServiceOn = true;
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

    private void initNetworkService() {
		initNetServiceConnection();
		Intent intent = new Intent(MediaPlayerActivity.this, NetworkService.class);
		startService(intent);
		bindService(intent, mNetServiceConn, getApplicationContext().BIND_AUTO_CREATE);
		IsNetworkServiceOn = true;
	}

    private void initNetServiceConnection() {
		mNetServiceConn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mNetworkService = ((NetworkService.MyBinder) service).getService();
				mNetworkService.setOnGetConnectState(new NetworkService.GetConnectState() {
					@Override
					public void GetState(int isConnected) {
						if (mIntentId != 0 && isConnected != 0) {
							// 当前还是处于有网状态，不做任何动作
							return;
						}
						if (mIntentId != isConnected) {
							// 如果当前连接状态与广播服务返回的状态不同才进行通知显示
							mIntentId = isConnected;
							RingLog.d(TAG, "Current network is " + mIntentId);

							if (mIntentId == 0) {
								// 未连接
								RingLog.d(TAG, "The network has disconnected");
								CameraService.cameraNeedStop = true;
								mHandler.sendEmptyMessage(STOP_PUSH_CMD);
							} else if (mIntentId != 0) {
								// 已连接
								RingLog.d(TAG, "The network has connected");
								mHandler.sendEmptyMessage(START_PUSH_CMD);
							}
						}
					}
				});
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {}
		};
	}

	private void startFirstRecord() {
		initCameraView();
		if (mCameraService != null) {
			mCameraService.startCameraRecord();
		}
	}

    private TerminalAdvertPackageVo getScheduleTimesCache() {
        return ScheduleTimesCache.get();
    }
    private void initAdView() {

        List<TemplateRegion> regionList = mTerminalAdvertPackageVo.getTemplate().getRegionList();
        Map<String, Long> relationMap = mTerminalAdvertPackageVo.getRelationMap();
        int imageIndexOffset = 0;
        boolean onlyMainPos = true;
        if(regionList.size()>1){
        	onlyMainPos = false;
		}
		RingLog.i("Use schedule times regionList size = " + regionList.size() + "onlyMainPos  = " + onlyMainPos);
        if(regionList!=null) {
            for(int i=0;i<regionList.size();i++){
                TemplateRegion region = regionList.get(i);
                String regLocation = region.getLocation();
                String[] regLocations = regLocation.split(",");
                String videoType = region.getVideoType();
                int regWidth = region.getWidth();
                int regHeight = region.getHeight();
                int marginLeft = Integer.valueOf(regLocations[0]);
                int marginTop = Integer.valueOf(regLocations[1]);
                Long adType = Long.valueOf(videoType);
                Long adPosId = relationMap.get(region.getIdent());
				if (onlyMainPos){
					mMainPositionId = adPosId;
				}else {
					set_view_layout(adType, regWidth, regHeight, marginLeft,
							marginTop, adPosId);
				}
            }
        }
    }

    private void set_view_layout(long viewType, int width, int height, int left, int top,Long adposid) {
		/*
		float scale = (float) 1080 / (float) 768;
		height = (int) (height * scale);
		if (top != 0)
			top = (int) (top * scale);
		*/
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
        } else if (viewType == 2) { // 视频控件
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
	private void showImageWithPath(ImageView imageView,String path,long duration) {
		if (path != null && !path.isEmpty()) {
			File file = new File(path);
			Glide.with(getApplicationContext()).load(file).into(imageView);
		}
		mHandler.sendEmptyMessageDelayed(SHOW_NEXT_ADVERT_CMD,duration*1000);
	}

    private void showImageWithIndex(ImageView imageView) {
		Long posid = (Long)imageView.getTag(R.id.image_key);
		PlayingAdvert item  = mPlayListManager.getValidPlayUrl(posid);
		if(item!=null) {
			String path = item.getPath();
			if (path != null && !path.isEmpty()) {
				File file = new File(path);
				Glide.with(getApplicationContext()).load(file).into(imageView);
			}
		}
		Message msg = new Message();
		msg.what = UPDATE_IMAGE_AD_CMD;
		msg.obj = imageView;
		mHandler.sendMessageDelayed(msg,IMAGE_AD_SHOW_DURATION);
    }
	private void removeImageView() {
		int childCount = relativeLayout.getChildCount();
		if (childCount <= 1)
			return;
		for (int i = 0; i < childCount; i++) {
			if (relativeLayout.getChildAt(i) instanceof ImageView) {
				ImageView imageView = (ImageView) relativeLayout.getChildAt(i);
				relativeLayout.removeView(imageView);
			}
		}
	}
	private void initAdInfoService(){
		initAdInfoServiceConn();
		Intent intent = new Intent(MediaPlayerActivity.this, AdvertInfoService.class);
		startService(intent);
		bindService(intent, mInfoServiceConn, getApplicationContext().BIND_AUTO_CREATE);
	}

	private void initAdInfoServiceConn() {
		mInfoServiceConn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "Advert info service connected");
				AdvertInfoService.InfoBinder binder = (AdvertInfoService.InfoBinder) service;
				mInfoService = binder.getService();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "Advert info service disconnected");
				mInfoService = null;
			}
		};
	}

	private void setAdInfo() {
		// 获取预设广告信息
		String backColor = DevRing.cacheManager().spCache("AdvertInfoResult").getString("backColor", "#000000");
		String fontColor = DevRing.cacheManager().spCache("AdvertInfoResult").getString("fontColor", "#ffffff");

		// 设置滚动信息
		setAdInfoScroll(backColor, fontColor);

		// 设置时间天气
		setAdInfoText(backColor, fontColor);
    }

    private void updateAdWeather() {
		Long celcius = DevRing.cacheManager().spCache("AdvertWeatherResult").getLong("celcius", 15);
		Long weatherType = DevRing.cacheManager().spCache("AdvertWeatherResult").getLong("type", 0);
		textViewTemp.setText(String.valueOf(celcius));
		ViewUtils.setWeatherIcon(getResources(), imageViewWeather, weatherType);
	}

	private void refreshAdInfo() {
		if (mInfoService != null) {
			mInfoService.refreshAdvertInfo();
		}
	}

    private void setAdInfoScroll(String backColor, String fontColor) {
		String writingText = DevRing.cacheManager().spCache("AdvertInfoResult").getString("writing", "");
		Long velocity = DevRing.cacheManager().spCache("AdvertInfoResult").getLong("velocity", 1);

		// 获取屏幕管理器
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		int win_width = metrics.widthPixels;
		int win_height = metrics.heightPixels;
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
		marqueeTextView.setBackgroundColor(Color.parseColor(backColor));
		marqueeTextView.setTextColor(Color.parseColor(fontColor));
		marqueeTextView.setScrollWidth(win_width);
		marqueeTextView.setCoordinateX(win_width);
		marqueeTextView.setCoordinateY(40);
		marqueeTextView.setSpeed(velocity.intValue());
		marqueeTextView.setText(writingText);
		marqueeTextView.setVisibility(View.VISIBLE);
	}

	private void setAdInfoText(String backColor, String fontColor) {
		Long isShowTime = DevRing.cacheManager().spCache("AdvertInfoResult").getLong("vTime", 1);
		Long isShowWeather = DevRing.cacheManager().spCache("AdvertInfoResult").getLong("weather", 1);
		Long weatherType = DevRing.cacheManager().spCache("AdvertWeatherResult").getLong("type", 0);
		Long celcius = DevRing.cacheManager().spCache("AdvertWeatherResult").getLong("celcius", 15);
		if (isShowTime != 1) {
			textViewTime.setVisibility(View.GONE);
		} else {
			textViewTime.setVisibility(View.VISIBLE);
		}
		if (isShowWeather != 1) {
			imageViewWeather.setVisibility(View.GONE);
		} else {
			imageViewWeather.setVisibility(View.VISIBLE);
		}
		ViewUtils.setWeatherIcon(getResources(), imageViewWeather, weatherType);
		textViewTime.setTextColor(Color.parseColor(fontColor));
		textViewTemp.setText(String.valueOf(celcius));
		textViewTemp.setTextColor(Color.parseColor(fontColor));
		textViewTempCode.setTextColor(Color.parseColor(fontColor));
		linearLayoutText.setVisibility(View.VISIBLE);
		linearLayoutText.setBackgroundColor(Color.parseColor(backColor));
	}
}
