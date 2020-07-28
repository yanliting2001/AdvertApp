package com.grandartisans.advert.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.grandartisans.advert.model.PlayerCmd;
import com.grandartisans.advert.model.entity.Player;
import com.grandartisans.advert.model.entity.PlayerStateEnum;
import com.pili.pldroid.player.PLMediaPlayer;

import java.io.IOException;

public class PlayerManager {
    private final String TAG = "PlayerManager";
    private static PlayerManager mPlayerInstance = null;
    private int mPlayerMaxNum = 2;
    private Player[] mPlayer = new Player[mPlayerMaxNum];
    private final int AD_PLAYER_CMD = 100032;
    private PlayerManager (Context context) {
        init();
    }
    public static PlayerManager getInstance(Context context) {
        if (mPlayerInstance == null) {
            synchronized (AdPlayListManager.class) {
                if (mPlayerInstance == null) {
                    mPlayerInstance = new PlayerManager(context);
                }
            }
        }
        return mPlayerInstance;
    }

    private void init(){
        for(int i=0;i<mPlayerMaxNum;i++){
            mPlayer[i] = new Player();
            mPlayer[i].setPlayerState(PlayerStateEnum.PLAYER_STATE_IDEL);
            mPlayer[i].setMediaPlayer(null);
        }
    }
    public void PlayerInit(Context context,  SurfaceHolder surfaceHolder, int index){
        Log.i(TAG, "PlayerInit index = " + index);
        if(index < mPlayerMaxNum) {
            mPlayer[index].setPlayerState(PlayerStateEnum.PLAYER_STATE_INIT);
            PLMediaPlayer mediaplayer = mPlayer[index].getMediaPlayer();
            if(mediaplayer==null) {
                mPlayer[index].setmContext(context);
                mPlayer[index].setSurfaceHolder(surfaceHolder);
            }
        }
    }
    public int GetPlayer(Context context){
        for(int i=0;i<mPlayerMaxNum;i++){
            if(mPlayer[i].getPlayerState() == PlayerStateEnum.PLAYER_STATE_IDEL){
                mPlayer[i].setPlayerState(PlayerStateEnum.PLAYER_STATE_INIT);
                return i;
            }
        }
        return -1;
    }

    private Handler mPlayerHandler = new Handler() {
        public void handleMessage(Message paramMessage) {
            switch (paramMessage.what) {
                case AD_PLAYER_CMD:
                    PlayerCmd playerCmd = (PlayerCmd) paramMessage.obj;
                    processPlayerCmd(playerCmd.getCmd(), playerCmd.getUrl(),playerCmd.getIndex());
                    break;
            }
        }
    };
    public void onPlayerCmd(String cmd,String url,int index) {
        PlayerCmd playerCmd = new PlayerCmd();
        playerCmd.setCmd(cmd);
        playerCmd.setUrl(url);
        playerCmd.setIndex(index);
        Message msg = new Message();
        msg.what = AD_PLAYER_CMD;
        msg.obj = playerCmd;
        mPlayerHandler.sendMessage(msg);
    }
    private void processPlayerCmd(String cmd,String url,int index){
        PlayerStateEnum states = mPlayer[index].getPlayerState();
        Log.d(TAG,"processPlayerCmd cmd:" + cmd + "PlayerState :"  + states + "player index=" + index);
        if(cmd.equals("reset")){
            if(states==PlayerStateEnum.PLAYER_STATE_PLAYING
                    ||states==PlayerStateEnum.PLAYER_STATE_PAUSED){
                PlayerStop(index);
            }
            PlayerRelease(index);
        }else if(cmd.equals("source")){
            if(states==PlayerStateEnum.PLAYER_STATE_INIT||states==PlayerStateEnum.PLAYER_STATE_STOPED) {
                PlayerInit(index);
                setDataSource(url,index);
            }
        }
        else if(cmd.equals("start")){
            if(states==PlayerStateEnum.PLAYER_STATE_PREPARING) {
                PlayerStart(index);
            }
        }
        else if(cmd.equals("pause")){
            if(states==PlayerStateEnum.PLAYER_STATE_PLAYING){
                PlayerPause(index);
            }
        }else if(cmd.equals("resume")) {
            if(states==PlayerStateEnum.PLAYER_STATE_PAUSED){
                PlayerResume(index);
            }
        }else if(cmd.equals("stop")){
            if(states==PlayerStateEnum.PLAYER_STATE_PLAYING
                    || states==PlayerStateEnum.PLAYER_STATE_PAUSED) {
                PlayerStop(index);
            }
            mPlayer[index].setPlayerState(PlayerStateEnum.PLAYER_STATE_STOPED);
        }else if(cmd.equals("release")){
            if(states==PlayerStateEnum.PLAYER_STATE_PLAYING
                    || states==PlayerStateEnum.PLAYER_STATE_PAUSED) {
                PlayerStop(index);
            }
            PlayerRelease(index);
        }

    }

    private void PlayerInit(int index) {
        Context context = mPlayer[index].getmContext();
        SurfaceHolder surfaceHolder = mPlayer[index].getSurfaceHolder();
            PLMediaPlayer mediaplayer = new PLMediaPlayer(context);
            mediaplayer.setDisplay(surfaceHolder);
            mediaplayer.setOnCompletionListener(new PLMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(PLMediaPlayer mp) {
                    //onVideoPlayCompleted(true);
                }
            });
            mediaplayer.setOnPreparedListener(new PLMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(PLMediaPlayer mp, int preparedTime) {
                    Log.i(TAG, "video width = " + mp.getVideoWidth() + "video height = " + mp.getVideoHeight());
                    //onPlayerCmd("start", "", 0);
                    mp.start();
                    //mp.start();
                    /*
                    if(mPlayEventListener!=null){
                        mPlayEventListener.onPrepared();
                    }
                    */
                }
            });

            mediaplayer.setOnErrorListener(new PLMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(PLMediaPlayer mp, int errorcode) {
                    Log.d(TAG, "OnError - Error code: " + errorcode);
                    //onVideoPlayCompleted(false);
                    return false;
                }

            });

            mediaplayer.setOnVideoSizeChangedListener(new PLMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(PLMediaPlayer mp, int width, int height, int videoSar, int videoDen) {
                    Log.d(TAG, "setOnVideoSizeChangedListener  width: " + width + "height: " + height);
                    //surfaceHolder.setFixedSize(width,height);
                }
            });
            mPlayer[index].setMediaPlayer(mediaplayer);
    }
    private void setDataSource(String url,int index) {
        if(index < mPlayerMaxNum) {
            PLMediaPlayer mediaPlayer = mPlayer[index].getMediaPlayer();
            try {
                Log.d(TAG, "start play: url = " + url + "index = " + index + "player status= " + mediaPlayer.getPlayerState());
                mediaPlayer.setDataSource(url);
                try {
                    mediaPlayer.prepareAsync();

                } catch (IllegalStateException e) {

                }
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
            mPlayer[index].setPlayerState(PlayerStateEnum.PLAYER_STATE_PLAYING);
        }
    }
    private void PlayerStart(int index){
        if(index < mPlayerMaxNum) {
            PLMediaPlayer mediaPlayer = mPlayer[index].getMediaPlayer();
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mPlayer[index].setPlayerState(PlayerStateEnum.PLAYER_STATE_PLAYING);
            }
        }
    }
    private void PlayerPause(int index) {
        if (index < mPlayerMaxNum) {
            PLMediaPlayer mediaPlayer = mPlayer[index].getMediaPlayer();
        if (mediaPlayer != null) {
            Log.d(TAG, "onPlayer Pause ");
            mediaPlayer.pause();
            mPlayer[index].setPlayerState(PlayerStateEnum.PLAYER_STATE_PAUSED);
        }
        }
    }
    private void PlayerResume(int index){
        if(index < mPlayerMaxNum) {
            PLMediaPlayer mediaPlayer = mPlayer[index].getMediaPlayer();
            if (mediaPlayer != null) {
                mediaPlayer.start();
                Log.d(TAG, "onPlayer Resume isPlaying = " + mediaPlayer.isPlaying() + "position = " + mediaPlayer.getCurrentPosition() + "duration = " + mediaPlayer.getDuration());
                mPlayer[index].setPlayerState(PlayerStateEnum.PLAYER_STATE_PLAYING);
            }
        }
    }
    private void PlayerStop(int index){
        if(index < mPlayerMaxNum) {
            PLMediaPlayer mediaPlayer = mPlayer[index].getMediaPlayer();
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                //mediaPlayer.reset();
                //mediaPlayer.release();

            }
            mPlayer[index].setPlayerState(PlayerStateEnum.PLAYER_STATE_STOPED);
        }
    }
    private void PlayerRelease(int index){
        if(index < mPlayerMaxNum) {
            PLMediaPlayer mediaPlayer = mPlayer[index].getMediaPlayer();
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mPlayer[index].setPlayerState(PlayerStateEnum.PLAYER_STATE_INIT);
            mPlayer[index].setMediaPlayer(null);
        }
    }
}
