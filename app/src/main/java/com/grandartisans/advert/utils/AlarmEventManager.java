package com.grandartisans.advert.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.ljy.devring.DevRing;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

/*电梯状态管理类*/
public class AlarmEventManager  {
    private final String TAG = "AlarmEventManager";
    private boolean isPowerAlarmHaveSet = false;
    private boolean isNetworkAlarmHaveSet = false;
    private volatile static AlarmEventManager mAlarmEventInstance = null;
    private Context mContext = null;

    private AlarmEventManager (Context context) {mContext = context;}
    public static AlarmEventManager getInstance(Context context) {
        if (mAlarmEventInstance == null) {
            synchronized (AlarmEventManager.class) {
                if (mAlarmEventInstance == null) {
                    mAlarmEventInstance = new AlarmEventManager(context);
                }
            }
        }
        return mAlarmEventInstance;
    }
    public void SetPowerAlarm(long startTimeIn,long endTimeIn){
        long startTime = 0;
        long endTime = 0;
        if(startTimeIn!=0 && endTimeIn!=0){
            startTime = startTimeIn;
            endTime = endTimeIn;
            DevRing.cacheManager().spCache("PowerAlarm").put("endTime",endTimeIn);
            DevRing.cacheManager().spCache("PowerAlarm").put("startTime",startTimeIn);
        }else {
            startTime = DevRing.cacheManager().spCache("PowerAlarm").getLong("startTime", 0);
            endTime = DevRing.cacheManager().spCache("PowerAlarm").getLong("endTime", 0);
        }
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
        initPowerOffAlarm(endHour,endMinute,endSecond,"POWER_OFF_ALARM");// 设置定时关机提醒
        if(startTime > endTime){
            initPowerOffAlarm(startHour,startMinute,startSecond,"POWER_ON_ALARM");// 设置开机时间为同一天
        }else {
            initPowerOnAlarm(startHour, startMinute, startSecond, "POWER_ON_ALARM");//设置开机时间为第二天
        }
        setPowerAlarmStatus(true);
    }

    public void SetNetworkAlarm(long startTimeIn,long endTimeIn){
        long startTime = 0;
        long endTime = 0;
        if(startTimeIn==-1 && endTimeIn==-1){
            startTime = DevRing.cacheManager().spCache("NetworkAlarm").getLong("networkStartTime", 0);
            endTime = DevRing.cacheManager().spCache("NetworkAlarm").getLong("networkEndTime", 0);
        }else{
            startTime = startTimeIn;
            endTime = endTimeIn;
            DevRing.cacheManager().spCache("NetworkAlarm").put("networkEndTime", startTimeIn);
            DevRing.cacheManager().spCache("NetworkAlarm").put("networkStartTime", endTimeIn);
        }
        int startHour = 07;
        int startMinute = 00;
        int startSecond = 00;
        int endHour = 21;
        int endMinute = 30;
        int endSecond = 00;
        if(startTime!=0&& endTime!=0) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            String t1 = format.format(startTime);
            String t2 = format.format(endTime);
            startHour = Integer.valueOf(t1.substring(0, 2));
            startMinute = Integer.valueOf(t1.substring(3, 5));
            startSecond = Integer.valueOf(t1.substring(6, 8));
            endHour = Integer.valueOf(t2.substring(0, 2));
            endMinute = Integer.valueOf(t2.substring(3, 5));
            endSecond = Integer.valueOf(t2.substring(6, 8));
            initPowerOnAlarm(startHour, startMinute, startSecond, "SET_WLAN_ON_ALARM");
            initPowerOnAlarm(endHour, endMinute, endSecond, "SET_WLAN_OFF_ALARM");
            CommonUtil.runCmd("ifconfig eth0 down");//关闭有线网络
            setNetWorkAlarmStatus(true);
        }
    }
    public long getPowerAlarmStartTime(){
        return DevRing.cacheManager().spCache("PowerAlarm").getLong("startTime",0);
    }
    public long getPowerAlarmEndTime(){
        return DevRing.cacheManager().spCache("PowerAlarm").getLong("endTime",0);
    }
    public long getNetworkAlarmStartTime(){
        return DevRing.cacheManager().spCache("NetworkAlarm").getLong("networkStartTime",0);
    }
    public long getNetworkAlarmEndtime(){
        return DevRing.cacheManager().spCache("NetworkAlarm").getLong("networkEndTime", 0);
    }
    public boolean getPowerAlarmStatus(){
        return isPowerAlarmHaveSet;
    }
    public void setPowerAlarmStatus(boolean status){
        isPowerAlarmHaveSet = status;
    }
    public boolean getNetWorkAlarmStatus(){
        return isNetworkAlarmHaveSet;
    }
    public void setNetWorkAlarmStatus(boolean status){
        isNetworkAlarmHaveSet = status;
    }

    private void initPowerOffAlarm(int hour,int minute,int second,String actionName) {
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
        Log.i(TAG,"Alarm action = " + actionName + " time = " + t1);
        Intent intent=new Intent(actionName);
        PendingIntent pi= PendingIntent.getBroadcast(mContext, 0, intent,0);
        //设置一个PendingIntent对象，发送广播
        AlarmManager am=(AlarmManager)mContext.getSystemService(ALARM_SERVICE);
        //获取AlarmManager对象
        //am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }


    private void initPowerOnAlarm(int hour,int minute,int second,String actionName) {
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
        Intent intent=new Intent(actionName);
        PendingIntent pi= PendingIntent.getBroadcast(mContext, 0, intent,0);
        //设置一个PendingIntent对象，发送广播
        AlarmManager am=(AlarmManager)mContext.getSystemService(ALARM_SERVICE);
        //获取AlarmManager对象
        //am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }
}
