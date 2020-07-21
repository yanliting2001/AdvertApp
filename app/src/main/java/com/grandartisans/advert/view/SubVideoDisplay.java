package com.grandartisans.advert.view;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.interfaces.IPlayEventListener;
import com.grandartisans.advert.model.PlayerCmd;
import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.ljy.devring.other.RingLog;
import com.pili.pldroid.player.PLMediaPlayer;
import com.westone.cryptoSdk.Api;

import java.io.IOException;
import java.io.InputStream;

public class SubVideoDisplay extends Presentation implements SurfaceHolder.Callback {
    private final String TAG = "SubDisplay";
    private PLMediaPlayer mMediaPlayer;
    private SurfaceView surface;
    private ImageView mImageMain;
    private RelativeLayout relativeLayout;
    private SurfaceHolder surfaceHolder;
    private Context mContext;
    private View mViewLayout;
    private TextView tv;
    private LinearLayout main_ll;
    private Bitmap defaultBitMap = null;
    private IPlayEventListener mPlayEventListener = null;

    static final int PLAYER_STATE_INIT = 0;
    static final int PLAYER_STATE_PREPARING = 1;
    static final int PLAYER_STATE_PLAYING = 2;
    static final int PLAYER_STATE_PAUSED = 3;
    static final int PLAYER_STATE_STOPED = 4;
    static final int PLAYER_STATE_RELEASED = 5;
    private int mPlayState = PLAYER_STATE_INIT;

    private final int AD_PLAYER_CMD = 100032;
    private final int CHECK_PLAYER_START_CMD = 100033;

        private Handler mPlayerHandler = new Handler() {
        public void handleMessage(Message paramMessage) {
            switch (paramMessage.what) {
                case AD_PLAYER_CMD:
                    PlayerCmd playerCmd = (PlayerCmd) paramMessage.obj;
                    processPlayerCmd(playerCmd.getCmd(), playerCmd.getUrl());
                    break;
                case CHECK_PLAYER_START_CMD:
                    if(getPlayerState()==PLAYER_STATE_PREPARING){
                       // onVideoPlayCompleted(true);
                    }
                    break;
            }
        }
    };

    public SubVideoDisplay(Context outerContext, Display display) {
        super(outerContext, display);
        mContext =outerContext;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_suface_player_test);
        findViews();
        //mViewLayout = getLayoutInflater().inflate(R.layout.activity_suface_player_test, null);
        //setContentView(mViewLayout);
        //We need to wait till the view is created so we can flip it and set the width & height dynamically
       // mViewLayout.getViewTreeObserver().addOnGlobalLayoutListener(new SubVideoDisplay.OnGlobalLayoutListener());
    }
    private class OnGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener{
        @Override
        public void onGlobalLayout() {
            //height is ready
            mViewLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            //int width = 1280;
            //int height = 800;
            int width = mViewLayout.getWidth();
            int height = mViewLayout.getHeight();
            Log.i(TAG,"mViewLayout width = " + width + "height=" + height);
            //mViewLayout.setTranslationX((width - height) / 2);
            //mViewLayout.setTranslationY((height - width) / 2);
            //mViewLayout.setRotation(90.0f);

            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams( height, width);
            // Inflate the layout.
            setContentView(mViewLayout, lp);
            findViews();
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
        if(mPlayEventListener!=null){
            mPlayEventListener.onsurfaceCreated();
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        if(mPlayEventListener!=null) {
            mPlayEventListener.onsurfaceDestroyed();
        }
    }
    private void findViews(){
        //setContentView(R.layout.welcome_activity);
        relativeLayout = (RelativeLayout) findViewById(R.id.rootframeview);
        mImageMain =(ImageView)findViewById(R.id.image_main);
        mImageMain.setVisibility(View.INVISIBLE);
        surface = (SurfaceView) findViewById(R.id.surface);
        surfaceHolder = surface.getHolder();// SurfaceHolder是SurfaceView的控制接口
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.RGBX_8888);
        if(mPlayEventListener!=null) {
            mPlayEventListener.onCompletion();
        }
    }
    public void playAdvert(Api encApi,String imgPath, int type){
        Log.i(TAG,"updateSubDisplay image path = " + imgPath);
        if(imgPath!=null && !imgPath.isEmpty()) {
            Bitmap bm = BitmapFactory.decodeFile(imgPath);
            BitmapDrawable bd = new BitmapDrawable(bm);
            main_ll.setBackground(bd);
        }else{
            BitmapDrawable bd= new BitmapDrawable(defaultBitMap);
            main_ll.setBackground(bd);
        }
    }

    /*
     * 初始化播放首段视频的player
     */
    private void initFirstPlayer() {
        mMediaPlayer = new PLMediaPlayer(mContext);
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
                Log.i(TAG, "video width = " + mMediaPlayer.getVideoWidth() + "video height = " + mMediaPlayer.getVideoHeight());
                //mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                //mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                onPlayerCmd("start","");
                if(mPlayEventListener!=null){
                    mPlayEventListener.onPrepared();
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
                //onPlayerCmd("release","");
                //onVideoPlayCompleted(true);
            }
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }
    public void playAdvert(Api encApi,PlayingAdvert item){
        if(item!=null) {
            long type = item.getvType();
            if(item.getvType()==2) { //视频广告
                mImageMain.setVisibility(View.GONE);
                onPlayerCmd("source",item.getPath());
            }else if(item.getvType()==1) {
                mImageMain.setVisibility(View.VISIBLE);
                showImageWithPath(encApi,mImageMain,item.getPath(),item.getDuration(),item.isEncrypt());
            }
        }

    }
    public void onPlayerCmd(String cmd,String url) {
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

    private void showImageWithPath(Api encApi,ImageView imageView, String path, long duration, boolean isEncrypt) {
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
    }
    public void set_view_layout(long viewType, int width, int height, int left, int top,Long adposid) {
        RingLog.d(TAG, "set_view_layout viewType = " + viewType + "width = " + width + "height = " + height + "left = " + left + "top = " + top );
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.setMargins(left, top, 0, 0);
        if (viewType == 1) {// 图片控件
            RingLog.d(TAG, "set image view");
            ImageView imageView = new ImageView(mContext);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setTag(R.id.image_key,adposid);
            relativeLayout.addView(imageView);
        } else if (viewType == 2 || viewType ==3) { // 视频控件
            RingLog.d(TAG, "set surface view");
            surface.setLayoutParams(layoutParams);
            surface.requestLayout();
            relativeLayout.requestLayout();

        }
    }
    public void removeImageView() {
        int childCount = relativeLayout.getChildCount();
        if (childCount <= 1)
            return;
        Log.i(TAG,"removeImageView childCount = " + childCount);
        for (int i = childCount-1; i >=0 ; i--) {
            if (relativeLayout.getChildAt(i) instanceof ImageView) {
                Log.i(TAG,"removeImageView i = " + i);
                ImageView imageView = (ImageView) relativeLayout.getChildAt(i);
                relativeLayout.removeView(imageView);
            }
        }
        //relativeLayout.requestLayout();
    }
    public int getPlayViewCount(){
        int count = 0;
        count = relativeLayout.getChildCount();
        return count;
    }
    public View getPlayChildView(int index){
        return relativeLayout.getChildAt(index);
    }
    public void  registerPlayListener(IPlayEventListener listener){
        if(listener!=null) mPlayEventListener = listener;
    }
}
