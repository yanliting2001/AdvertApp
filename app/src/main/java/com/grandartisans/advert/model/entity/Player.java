package com.grandartisans.advert.model.entity;

import android.content.Context;
import android.view.SurfaceHolder;

import com.pili.pldroid.player.PLMediaPlayer;

public class Player {
    private PLMediaPlayer mediaPlayer;
    private PlayerStateEnum playerState;
    private Context mContext;
    private SurfaceHolder surfaceHolder;

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public SurfaceHolder getSurfaceHolder() {
        return surfaceHolder;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    public PLMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(PLMediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public PlayerStateEnum getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerStateEnum mPlayState) {
        this.playerState = mPlayState;
    }
}
