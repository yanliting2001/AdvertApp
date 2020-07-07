package com.grandartisans.advert.utils;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.grandartisans.advert.view.SubDisplay;

public class SubDisplayEngine {
    //获取涉笔上的屏幕
    private DisplayManager mDisPlayManager;
    private Display[] displays;//屏幕数组
    private SubDisplay mSubDisplay;
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
            mSubDisplay = new SubDisplay(context,displays[1]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
                mSubDisplay.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            }else {
                mSubDisplay.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            mSubDisplay.show();
        }
    }
    public void updateSubDisplay(String imgPath){
        if(mSubDisplay!=null) {
            mSubDisplay.updateSubDisplay(imgPath);
        }
    }

}
