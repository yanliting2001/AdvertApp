package com.grandartisans.advert.utils;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.util.Log;

public class TelephonyUtils {
	private static final String TAG="TelephonyUtils";
	//SIM卡状态常量  
    private static final String SIM_ABSENT = "Absent"; //手机内无SIM卡  
    private static final String SIM_READY = "Ready"; //SIM卡已准备好  
    private static final String SIM_PIN_REQUIRED = "PIN required"; //需要SIM卡的PIN解锁  
    private static final String SIM_PUK_REQUIRED = "PUK required"; //需要SIM卡的PUK解锁   
    private static final String SIM_NETWORK_LOCKED = "Network locked"; //需要Network PIN解锁  
    private static final String SIM_UNKNOWN = "Unknown"; //状态未知  
      
    //网络类型常量  
    private static final String NETWORK_CDMA = "CDMA: Either IS95A or IS95B (2G)";  
    private static final String NETWORK_EDGE = "EDGE (2.75G)";  
    private static final String NETWORK_GPRS = "GPRS (2.5G)";  
    private static final String NETWORK_UMTS = "UMTS (3G)";  
    private static final String NETWORK_EVDO_0 = "EVDO revision 0 (3G)";  
    private static final String NETWORK_EVDO_A = "EVDO revision A (3G - Transitional)";  
    private static final String NETWORK_EVDO_B = "EVDO revision B (3G - Transitional)";  
    private static final String NETWORK_1X_RTT = "1xRTT  (2G - Transitional)";  
    private static final String NETWORK_HSDPA = "HSDPA (3G - Transitional)";  
    private static final String NETWORK_HSUPA = "HSUPA (3G - Transitional)";  
    private static final String NETWORK_HSPA = "HSPA (3G - Transitional)";  
    private static final String NETWORK_IDEN = "iDen (2G)"; 
    private static final String NETWORK_LTE = "Lte";
   
    private static final String NETWORK_UNKOWN = "Unknown";  
      
    //手机制式类型常量  
    private static final String PHONE_CDMA = "CDMA";  
    private static final String PHONE_GSM = "GSM";  
    private static final String PHONE_NONE = "No radio";  
	/*获取运营商*/
	public static int getOperators(Context mActivity) {
        TelephonyManager telManager = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telManager.getSubscriberId();
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007")) {
                // 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
                return 1;// 中国移动
            } else if (imsi.startsWith("46001") || imsi.startsWith("46006")) {
                return 2;// 中国联通
            } else if (imsi.startsWith("46003") || imsi.startsWith("46005")) {
                return 4;// 中国电信
            }
        }
        
        return -1;
    }
	  /**   
	   * 电话方位：   
	   * 返回当前移动终端的位置  
	   */    
	public static CellLocation getCellLocation(Context mcontext) {
		TelephonyManager telManager = (TelephonyManager) mcontext.getSystemService(Context.TELEPHONY_SERVICE);
		CellLocation Location = telManager.getCellLocation();//CellLocation 
		return Location;
	}
	/*获取移动网络类型*/
	public static String getNetworkType(Context mcontext) {
		TelephonyManager telManager = (TelephonyManager) mcontext.getSystemService(Context.TELEPHONY_SERVICE);
		return mapNetworkTypeToName(telManager.getNetworkType());
	}
	
	public static String getNetworkName(Context mcontext) {
		TelephonyManager telManager = (TelephonyManager) mcontext.getSystemService(Context.TELEPHONY_SERVICE);
		return telManager.getNetworkOperatorName();
	}
	
	public static String getSimState(Context mcontext) {
		TelephonyManager telManager = (TelephonyManager) mcontext.getSystemService(Context.TELEPHONY_SERVICE);
		return mapSimStateToName(telManager.getSimState());
	}
	
	public static String getPhoneType(Context mcontext) {
		TelephonyManager telManager = (TelephonyManager) mcontext.getSystemService(Context.TELEPHONY_SERVICE);
		return mapDeviceTypeToName(telManager.getPhoneType());
	}
	
	 /** 
     * 将手机制式值以字符串形式返回 
     * @param phoneType 
     * @return 
     */  
    private static String mapDeviceTypeToName(int phoneType) {
    	Log.i(TAG,"phoneType = " + phoneType);
        switch (phoneType) {  
        case TelephonyManager.PHONE_TYPE_CDMA:  
            return PHONE_CDMA;  
        case TelephonyManager.PHONE_TYPE_GSM:  
            return PHONE_GSM;  
        case TelephonyManager.PHONE_TYPE_NONE:  
            return PHONE_NONE;  
        default:  
            //不应该走到这个分支  
            return null;  
        }  
    }  
  
    /** 
     * 将网络类型值以字符串形式返回 
     * @param networkType 
     * @return 
     */  
    private static String mapNetworkTypeToName(int networkType) {  
    	Log.i(TAG,"networkType = " + networkType);
        switch (networkType) {  
        case TelephonyManager.NETWORK_TYPE_CDMA:  
            return NETWORK_CDMA;  
        case TelephonyManager.NETWORK_TYPE_EDGE:  
            return NETWORK_EDGE;  
        case TelephonyManager.NETWORK_TYPE_GPRS:  
            return NETWORK_GPRS;  
        case TelephonyManager.NETWORK_TYPE_UMTS:  
            return NETWORK_UMTS;  
        case TelephonyManager.NETWORK_TYPE_EVDO_0:  
            return NETWORK_EVDO_0;  
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
            return NETWORK_EVDO_A;
        case TelephonyManager.NETWORK_TYPE_LTE:
        	return NETWORK_LTE;
        case TelephonyManager.NETWORK_TYPE_EVDO_B:  
            return NETWORK_EVDO_B;  
        case TelephonyManager.NETWORK_TYPE_1xRTT:  
            return NETWORK_1X_RTT;  
        case TelephonyManager.NETWORK_TYPE_HSDPA:  
            return NETWORK_HSDPA;  
        case TelephonyManager.NETWORK_TYPE_HSPA:  
            return NETWORK_HSPA;  
        case TelephonyManager.NETWORK_TYPE_HSUPA:  
            return NETWORK_HSUPA;  
        case TelephonyManager.NETWORK_TYPE_IDEN:  
            return NETWORK_IDEN;  
        case TelephonyManager.NETWORK_TYPE_UNKNOWN:  
        default:  
            return NETWORK_UNKOWN;  
        }  
    }  
  
    /** 
     * 将SIM卡状态值以字符串形式返回 
     * @param simState 
     * @return 
     */  
    private static String mapSimStateToName(int simState) { 
    	Log.i(TAG,"simState = " + simState);
        switch (simState) {  
        case TelephonyManager.SIM_STATE_ABSENT:  
            return SIM_ABSENT;  
        case TelephonyManager.SIM_STATE_READY:  
            return SIM_READY;  
        case TelephonyManager.SIM_STATE_PIN_REQUIRED:  
            return SIM_PIN_REQUIRED;  
        case TelephonyManager.SIM_STATE_PUK_REQUIRED:  
            return SIM_PUK_REQUIRED;  
        case TelephonyManager.SIM_STATE_NETWORK_LOCKED:  
            return SIM_NETWORK_LOCKED;  
        case TelephonyManager.SIM_STATE_UNKNOWN:  
            return SIM_UNKNOWN;  
        default:  
            //不应该走到这个分支  
            return null;  
        }  
    }  

}
