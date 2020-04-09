package com.grandartisans.advert.utils;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

import com.grandartisans.advert.interfaces.IMultKeyTrigger;


public class MyMultKeyTrigger implements IMultKeyTrigger {
	private static String TAG =MyMultKeyTrigger.class.getSimpleName();

    //组合键序列
    private final static int[] MULT_KEY = new int[]{KeyEvent.KEYCODE_DPAD_UP,KeyEvent.KEYCODE_DPAD_RIGHT,KeyEvent.KEYCODE_DPAD_DOWN,KeyEvent.KEYCODE_DPAD_LEFT};


    //是否限定要在时间间隔里再次输入按键
    private static boolean ALLAW_SETTING_DELAYED_FLAG = true;

    //允许用户在多少时间间隔里输入按键
    private static int CHECK_NUM_ALLAW_MAX_DELAYED = 10000;

    //记录用户连续输入了多少个有效的键
    private static int check_num = 0;

    private static long lastEventTime = 0;//最后一次用户输入按键的时间

    public Context context;

    public MyMultKeyTrigger(Context context){
        this.context = context;
    }
    @Override
    public boolean allowTrigger() {
        // TODO Auto-generated method stub
        return true;
    }
    @Override
    public boolean checkKey(int keycode, long eventTime) {
        // TODO Auto-generated method stub
        boolean check;
        int delayed;
        //转换为实际数值
        int num = keycode;
        Log.i(TAG, "checkKey lastEventTime="+lastEventTime);
        Log.i(TAG, "checkKey num= "+num+" , eventTime = "+eventTime);
        //首次按键
        if(lastEventTime==0){
            delayed = 0;
        }else{
            //非首次按键
            delayed = (int)(eventTime-lastEventTime);
        }
        check = checkKeyValid(num, delayed);

        lastEventTime = check?eventTime:0L;

        Log.i(TAG, "checkKey check key valid = "+check);
        return check;
    }
    /**
     * 传入用户输入的按键
     * @param num
     * @param delayed 两次按键之间的时间间隔
     * @return
     */
    private  boolean checkKeyValid(int num,int delayed){
        Log.i(TAG, "checkKey num= "+num+" , delayed = "+delayed);
        //如果超过最大时间间隔，则重置
        if(ALLAW_SETTING_DELAYED_FLAG && delayed>CHECK_NUM_ALLAW_MAX_DELAYED){
            check_num = 0;
            return false;
        }
        //如果输入的数刚好等于校验位置的数，则有效输入+1
        if(check_num<MULT_KEY.length&&MULT_KEY[check_num]==num){
            check_num++;
            return true;
        }else{
            check_num = 0;//如果输入错误的话，则重置掉原先输入的
        }
//      return check_num==MULT_KEY_RESTART.length;
        return false;
    }
    @Override
    public void clearKeys() {
        // TODO Auto-generated method stub
        lastEventTime = 0;
        check_num = 0;
    }

    @Override
    public boolean checkMultKey() {
        // TODO Auto-generated method stub
        return check_num == MULT_KEY.length;
    }

    @Override
    public void onTrigger() {
        // TODO Auto-generated method stub
        if(checkMultKey()){
        	Log.i(TAG, "onTrigger MultKey num= ");
        }else{
            //throw new RuntimeException("you must be sure checkMultKey() is return true.");
        }
    }
   
}
