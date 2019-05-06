package com.grandartisans.advert.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.grandartisans.advert.R;
import com.grandartisans.advert.activity.MediaPlayerActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * author：   zp
 * date：     2015/8/19 0019 17:45
 * <p/>       公共类,主要用于一些常用的方法
 * modify by  ljy
 */
public class CommonUtil {

    /**
     * 根据输入法状态打开或隐藏输入法
     *
     * @param context 上下文
     */
    public static void toggleSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 隐藏输入键盘
     *
     * @param context
     * @param view
     */
    public static void hideSoftInput(Context context, View view) {
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 移除焦点
     *
     * @param view view
     */
    public static void removeFocus(View view) {
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
    }


    /**
     * 创建桌面快捷方式
     *
     * @param context Context
     */
    public static void creatShortcut(Context context) {
        //创建快捷方式的Intent
        Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //不允许重复创建
        shortcutIntent.putExtra("duplicate", false);
        //需要显示的名称
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));
        //快捷图片
        Parcelable icon = Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), R.mipmap.ic_launcher);

        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

        Intent intent = new Intent(context.getApplicationContext(),MediaPlayerActivity.class);
        //下面两个属性是为了当应用程序卸载时桌面 上的快捷方式会删除
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        //点击快捷图片，运行的程序主入口
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        //发送广播。OK
        context.sendBroadcast(shortcutIntent);
    }

    /**
     * 删除程序的快捷方式
     *
     * @param context Context
     */
    public static void delShortcut(Context context, ComponentName componentName) {
        Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        //快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));
        Intent intent = new Intent(context.getApplicationContext(), MediaPlayerActivity.class);
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setComponent(componentName);

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

        context.sendBroadcast(shortcut);

    }

    /**
     * 获取随机字符串
     *
     * @param len
     * @return
     */
    public static String getRandomString(int len) {
        String returnStr;
        char[] ch = new char[len];
        Random rd = new Random();
        for (int i = 0; i < len; i++) {
            ch[i] = (char) (rd.nextInt(9) + 97);
        }
        returnStr = new String(ch);
        return returnStr;
    }



    /**
     * 对当前屏幕进行截屏操作
     */
    public static Bitmap captureScreen(Activity activity) {

        activity.getWindow().getDecorView().setDrawingCacheEnabled(true);

        Bitmap bmp = activity.getWindow().getDecorView().getDrawingCache();

        return bmp;

    }

    /**
     * 判断当前日期是否在给定的两个日期之间
     * @param beginDate 开始日期
     * @param endDate 结束日期
     */
    public static boolean compareDateState(String beginDate, String endDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c.setTimeInMillis(System.currentTimeMillis());
            c1.setTime(df.parse(beginDate));
            c2.setTime(df.parse(endDate));
        } catch (java.text.ParseException e) {
            return false;
        }
        int resultBegin = c.compareTo(c1);
        int resultEnd = c.compareTo(c2);
        return resultBegin > 0 && resultEnd < 0;
    }

    public static Long getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        Log.d("getPastDate", result);
        return calendar.getTimeInMillis();
    }


    //获取设备唯一ID号
    //需加入<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    //和<uses-permission android:name="android.permission.BLUETOOTH"/>权限
    public static String getDeviceUniqueId(Context context) {
        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String m_szImei = TelephonyMgr.getDeviceId();

        String m_szDevIDShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 +
                Build.HOST.length() % 10 +
                Build.ID.length() % 10 +
                Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 +
                Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 +
                Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 digits

        String m_szAndroidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();

        BluetoothAdapter m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String m_szBTMAC = m_BluetoothAdapter == null ? "" : m_BluetoothAdapter.getAddress();

        /**
         * 综上所述，我们一共有五种方式取得设备的唯一标识。它们中的一些可能会返回null，或者由于硬件缺失、权限问题等获取失败。
         但你总能获得至少一个能用。所以，最好的方法就是通过拼接，或者拼接后的计算出的MD5值来产生一个结果。
         */
        String uniqueId = m_szImei + m_szDevIDShort + m_szAndroidID + m_szWLANMAC + m_szBTMAC;
        return EncryptUtil.md5Crypt(uniqueId.getBytes());
    }

    /**
     * description 增加view的触摸范围
     */
    public static void expandViewTouchDelegate(final View view, final int top, final int bottom, final int left, final int right) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -= top;
                bounds.bottom += bottom;
                bounds.left -= left;
                bounds.right += right;

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }
    /**
     * description :获取有线网络MAC 地址
     */
    public static String getEthernetMac() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/eth0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str;) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    private static String get(String prop, String defaultvalue) {
        Method get = null;
        String value = defaultvalue;
        try {
            if (null == get) {
                    if (null == get) {
                        Class<?> cls = Class.forName("android.os.SystemProperties");
                        get = cls.getDeclaredMethod("get", new Class<?>[]{String.class, String.class});
                    }
            }
            value = (String) (get.invoke(null, new Object[]{prop, defaultvalue}));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return value;
    }

    private static boolean set(String prop, String value) {
        Method set = null;
        try {
            if (null == set) {
                if (null == set) {
                    Class<?> cls = Class.forName("android.os.SystemProperties");
                    set = cls.getDeclaredMethod("set", new Class<?>[]{String.class, String.class});
                }
            }
            set.invoke(null, new Object[]{prop, value});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean setScreenVideoMode(String mode){
        return set("tv.default.screen.mode",mode);
    }

    public static String getVersionInfo(){
        String version="";
        version = get("ro.build.version.versioncode","");
        if(version!=null && version.length()>0){
            int start = version.indexOf("_V");
            int index = version.lastIndexOf("_");
            if(start >=0 && index >=0){
                version = version.substring(start+2, index);
            }
        }
        return version;
    }
    public static String getModel() {
        String mode = get("ro.product.model","");
        return mode;
    }
    public static String getUsid() {
        String mode = get("ro.product.model","");
        return mode;
    }
    public static String getTFMiniDevice(){
        String device = get("ro.tfmini.device","ttyS3");
        return device.trim();
    }
    public static int getTFMiniEnabled() {
        String value = get("persist.sys.tfmini.enable","1");
        int enable = Integer.valueOf(value);
        return enable;
    }
    public static int getGsensorEnabled() {
        String value = get("persist.sys.gsensor.enable","1");
        int enable = Integer.valueOf(value);
        return enable;
    }

    public static void reboot (Context context){
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
    }

    public static boolean isForeground(Context context, String className) {
        Log.i("isForeground","className = " + className);
        if (context == null || className.isEmpty()) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        Log.i("isForeground","RunningTask list size  = " + list.size());
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            Log.i("isForeground","TopActivity = " + cpn.getClassName() );
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * SDCard 总容量大小
     * @return MB
     */
    public static long getTotalSize()
    {
        String sdcard= Environment.getExternalStorageState();
        String state=Environment.MEDIA_MOUNTED;
        File file=Environment.getExternalStorageDirectory();
        StatFs statFs=new StatFs(file.getPath());
        if(sdcard.equals(state))
        {
            //获得sdcard上 block的总数
            long blockCount=statFs.getBlockCount();
            //获得sdcard上每个block 的大小
            long blockSize=statFs.getBlockSize();
            //计算标准大小使用：1024，当然使用1000也可以
            long bookTotalSize=blockCount*blockSize/1000/1000;
            return bookTotalSize;

        }else
        {
            return -1;
        }

    }

    /**
     * 计算Sdcard的剩余大小
     * @return MB
     */
    public static long getAvailableSize()
    {
        String sdcard= Environment.getExternalStorageState();
        String state=Environment.MEDIA_MOUNTED;
        File file=Environment.getExternalStorageDirectory();
        StatFs statFs=new StatFs(file.getPath());
        if(sdcard.equals(state))
        {
            //获得Sdcard上每个block的size
            long blockSize=statFs.getBlockSize();
            //获取可供程序使用的Block数量
            long blockavailable=statFs.getAvailableBlocks();
            //计算标准大小使用：1024，当然使用1000也可以
            long blockavailableTotal=blockSize*blockavailable/1000/1000;
            return blockavailableTotal;
        }else
        {
            return -1;
        }
    }

    /**
     * 获取开机的时间
     * @return 年-月-日 时:分:秒
     */
    public static String getSystemBootTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SystemBootTime", Context.MODE_PRIVATE);
        long seconds = sharedPreferences.getLong("SystemBootTime", new Date().getTime());
        String res = stampToDate(seconds);
        return res;
    }

    /**
     * 将时间戳转换为时间
     */
    public static String stampToDate(long timestamp) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timestamp);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 上传 Crash Log 到服务器
     * @param logPath
     * @return 是否需要上传
     */
    public static boolean isUploadCrashLog(String logPath) {
        // 判断文件夹是否存在
        if (FileUtils.isFileExit(logPath)) {
            File file = new File(logPath);
            File[] listFiles = file.listFiles();
            // 判断该文件夹是否为空
            if (listFiles.length > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除文件 如果是文件夹则清空
     * @param file
     */
    public static void deleteFile(File file) {
        // 是否为文件夹
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
        } else if (file.exists()) {
            file.delete();
        }
    }
}
