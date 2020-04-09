package com.grandartisans.advert.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.grandartisans.advert.R;
import com.grandartisans.advert.model.entity.DownloadInfo;
import com.grandartisans.advert.utils.ALFu700ProjectManager;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.Contacts;

public class MainActivity extends Activity {
    private final String TAG = "MainActivity";
    private final int PLAYER_START_CMD = 100001;
    private ALFu700ProjectManager mProjectManager = null;

    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message paramMessage)
        {
            switch (paramMessage.what)
            {
                case PLAYER_START_CMD:

                    break;
            }
            super.handleMessage(paramMessage);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate");
        mProjectManager = ALFu700ProjectManager.getInstance(getApplicationContext());
        mProjectManager.openProject();
        Button openBtn = (Button)findViewById(R.id.btnOpen);
        openBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //mProjectManager.WakeUpProject();
                //CommonUtil.runCmd("ifconfig eth0 down");


                Intent intent = new Intent("com.android.56iq.otaupgrade");
                intent.putExtra("path", Contacts.DOWNLOAD_FILE_PATH_CACHE + "update11.zip");//otaPath为ota包所在的本地路径
                sendBroadcast(intent);

                /*
                Intent intent = new Intent("android.action.adtv.sleep");
                intent.putExtra("is_audio_mute", true);
                sendBroadcast(intent);
                */

                /*
                Intent intent = new Intent("android.action.adtv.wakeup");
                intent.putExtra("is_audio_mute", true);
                sendBroadcast(intent);
                */

            }
        });
        Button closeBtn = (Button)findViewById(R.id.btnClose);
        closeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mProjectManager.SleepProject();
            }
        });
        Button PositionBtn = (Button)findViewById(R.id.btnPosition);
        PositionBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //mProjectManager.setPosition();
                CommonUtil.runCmd("ifconfig eth0 up");
            }
        });
        Button scaleBtn = (Button)findViewById(R.id.btnScale);
        scaleBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mProjectManager.setScale();
            }
        });
        Button focusBtn = (Button)findViewById(R.id.btnFocus);
        focusBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mProjectManager.setFocus();
            }
        });
        Button playerBtn = (Button)findViewById(R.id.btnPlayer);
        playerBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startAdPlayer();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.i(TAG,"onKeyDown keyCode = " + keyCode);
        /*
        if(keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            mProjectManager.sendUp();
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
            mProjectManager.sendDown();
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
            mProjectManager.sendLeft();
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
            mProjectManager.sendRight();
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BACKSLASH){
            mProjectManager.sWitchHdmi();
            return true;
        }*/
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        mHandler.removeMessages(PLAYER_START_CMD);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void startAdPlayer(){
        Intent intent = new Intent(MainActivity.this, MediaPlayerActivity.class);
        startActivity(intent);
    }
}
