package com.grandartisans.advert.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.grandartisans.advert.interfaces.IPlayEventListener;
import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.grandartisans.advert.view.SubDisplay;
import com.grandartisans.advert.view.SubVideoDisplay;
import com.westone.cryptoSdk.Api;

import static android.support.constraint.Constraints.TAG;

public class SubDisplayEngine {
    //获取涉笔上的屏幕
    private static final  String TAG = "SubDisplayEngine";
    private DisplayManager mDisPlayManager;
    private Display[] displays;//屏幕数组
    private SubVideoDisplay mSubDisplay;
    private static SubDisplayEngine instance;
    public static SubDisplayEngine getInstance(Context context){
        if (instance == null) {
            synchronized (SubDisplayEngine.class) {
                if (instance == null) {
                    instance = new SubDisplayEngine(context);
                }
            }
        }
        return instance;
    }
    public static void close(){
        instance = null;
    }
    private SubDisplayEngine(Context context){
        mDisPlayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
        displays = mDisPlayManager.getDisplays();
        if(null == mSubDisplay && displays.length>1){
            mSubDisplay = new SubVideoDisplay(context,displays[1]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
                mSubDisplay.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            }else {
                mSubDisplay.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            mSubDisplay.show();
        }
    }
    public void SubDisplayHide(){
        if(mSubDisplay!=null){
            mSubDisplay.dismiss();
            mSubDisplay = null;
            instance = null;
        }
    }
    public void updateSubDisplay(String imgPath){
        if(mSubDisplay!=null) {
            //mSubDisplay.updateSubDisplay(imgPath);
        }
    }
    public void onPlayerCmd(String cmd,String url){
        if(mSubDisplay!=null)
            mSubDisplay.onPlayerCmd(cmd,url);
    }
    public void playerAdvert(Api encApi,PlayingAdvert item){
        if(mSubDisplay!=null){
            mSubDisplay.playAdvert(encApi,item);
        }
    }
    public void set_view_layout(long viewType, int width, int height, int left, int top,Long adposid){
        if(mSubDisplay!=null) {
            //mSubDisplay.set_view_layout(viewType,width,height,left,top,adposid);
        }
    }
    public void removeImageView() {
        if(mSubDisplay!=null) {
            mSubDisplay.removeImageView();
        }
    }
    public int getPlayViewCount()
    {
        if(mSubDisplay!=null)
            return mSubDisplay.getPlayViewCount();
        return 0;
    }
    public View getPlayChildView(int index){
        if(mSubDisplay!=null)
            return mSubDisplay.getPlayChildView(index);
        return null;
    }
    public void  registerPlayListener(IPlayEventListener listener){
        if(mSubDisplay!=null)
            mSubDisplay.registerPlayListener(listener);
    }
}
