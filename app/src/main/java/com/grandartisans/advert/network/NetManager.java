package com.grandartisans.advert.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import android.content.Context;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.Inet4Address;

public class NetManager {

	private static String TAG = "NetManager";

	public static int DEV_TYPE_ETH = 1;
	public static int DEV_TYPE_WIFI = 2;

	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_WEP = 1;
	public static final int SECURITY_WPA_WPA2 = 2;
	public static final int SECURITY_WPA2 = 3;
	public static final int SECURITY_WPA = 4;
	public static final int SECURITY_EAP = 5;

	private static final String eth_device_sysfs = "/sys/class/ethernet/linkspeed";
	//public EthernetManager mEthernetManager;
	public final ConnectivityManager mConnectivityManager;
	public WifiManager mWifiManager;
	Context mContext = null;

	public NetManager(Context context) {
		mContext = context;
		//sw = (SystemWriteManager) mContext.getSystemService("system_write");
		//mEthernetManager = (EthernetManager) mContext.getSystemService("ethernet");
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	}

	public WifiManager getWifiMgr() {
		return mWifiManager;
	}




	





	public void enableEthernet(boolean enable) {
		/*if(!WifiUtils.isEthConnected(mContext)){
			EthernetDevInfo info = new EthernetDevInfo();
			info.setIfName("eth0");
			info.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_DHCP);
	        	info.setIpAddress(null);
	        	info.setRouteAddr(null);
	        	info.setDnsAddr(null);
	        	info.setNetMask(null);
			info.setProxy(null, 0, null);
	        	mEthernetManager.updateEthDevInfo(info);
			mEthernetManager.setEthEnabled(enable);
		}*/
	}


	public void startWifiScan() {
		mWifiManager.startScan();
	}



	public String getDeviceIpAddress(int type) {
		boolean isEthConnected = WifiUtils.isEthConnected(mContext);
		boolean isWifiConnected = WifiUtils.isWifiConnected(mContext);
		/*if (isWifiConnected && type == DEV_TYPE_WIFI) {
			WifiInfo mWifiinfo = mWifiManager.getConnectionInfo();
			String ipAddress = null;
			if (mWifiinfo != null) {
				ipAddress = int2ip(mWifiinfo.getIpAddress());
				Log.d(TAG, "==== wifi ipAddress : " + ipAddress);

			}
			return ipAddress;
		} else if (isEthConnected && type == DEV_TYPE_ETH) {
			DhcpInfo mDhcpInfo = mEthernetManager.getDhcpInfo();
			if (mDhcpInfo != null) {
				int ip = mDhcpInfo.ipAddress;
				String ipAddress = int2ip(ip);
				Log.d(TAG, "==== Wired ipAddress : " + ipAddress);
				return ipAddress;

			}
		}*/
		return null;
	}

	public String getDeviceIpAddress() {
		boolean isEthConnected = WifiUtils.isEthConnected(mContext);
		boolean isWifiConnected = WifiUtils.isWifiConnected(mContext);
		/*if (isWifiConnected ) {
			WifiInfo mWifiinfo = mWifiManager.getConnectionInfo();
			String ipAddress = null;
			if (mWifiinfo != null) {
				ipAddress = int2ip(mWifiinfo.getIpAddress());
				Log.d(TAG, "==== wifi ipAddress : " + ipAddress);
			}
			return ipAddress;
		} else if (isEthConnected ) {
			DhcpInfo mDhcpInfo = mEthernetManager.getDhcpInfo();
			if (mDhcpInfo != null) {
				int ip = mDhcpInfo.ipAddress;
				String ipAddress = int2ip(ip);
				Log.d(TAG, "==== Wired ipAddress : " + ipAddress);
				return ipAddress;

			}
		}*/
		return null;
	}
	
	

	/*
   	public String getEthernetIpAddress() {
        	LinkProperties linkProperties = mConnectivityManager.getLinkProperties(ConnectivityManager.TYPE_ETHERNET);

        	if (linkProperties == null) {
            		return "";
        	}
        	for (LinkAddress linkAddress: linkProperties.getAllLinkAddresses()) {
            		InetAddress address = linkAddress.getAddress();
            		if (address instanceof Inet4Address) {
            		    return address.getHostAddress();
            		}
        	}

        	// IPv6 address will not be shown like WifiInfo internally does.
        	return "";
    	}
	*/



	
	
	public int getEthSpeed() {
		/*String str = Utils.readSysFile(sw, eth_device_sysfs);
		if (str == null)
			return 0;
		int start = str.indexOf("speed :");
		String value = null;
		if (str.length() > start + 7) {
			value = str.substring(start + 7).trim();
		}
		if (value != null)
			return Integer.parseInt(value);
		else*/
			return 0;
	}

	public boolean isEthEnable() {
		return  true;
		/*
		if (mConnectivityManager.isNetworkSupported(ConnectivityManager.TYPE_ETHERNET)) {
            		return mEthernetManager.isAvailable();
        	}
		return false;
		*/
	}
	
	protected InetAddress getLocalInetAddress() {  
	    InetAddress ip = null;  
	    try {  
		        Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();  
		        while (en_netInterface.hasMoreElements()) {  
		            NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();  
		            Enumeration<InetAddress> en_ip = ni.getInetAddresses();  
		            while (en_ip.hasMoreElements()) {  
		                ip = en_ip.nextElement();  
		                if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)  
		                    break;  
		                else  
	                    ip = null;  
		            }  
		  
		            if (ip != null) {  
		                break;  
		            }  
		        }  
		    } catch (SocketException e) {  
		        // TODO Auto-generated catch block  
		        e.printStackTrace();  
		    }  
		    return ip;  
	}  
	  

	
	public String getLocalEthMacAddress() {
		String mac_s = "";
		String ipAddress = null;
		/*DhcpInfo mDhcpInfo = mEthernetManager.getDhcpInfo();
		if (mDhcpInfo != null) {
			int ip = mDhcpInfo.ipAddress;
			ipAddress = int2ip(ip);
			Log.d(TAG, "==== Wired ipAddress : " + ipAddress);
		} else {
			return null;
		}
		try {
			byte[] mac;
			InetAddress inet =getLocalInetAddress();	//InetAddress.getByName(ipAddress);
			NetworkInterface ne = NetworkInterface.getByInetAddress(inet);
			mac = ne.getHardwareAddress();
			mac_s = byte2hex(mac);
		} catch (Exception e) {
			e.printStackTrace();
		}*/

		return mac_s;
	}

	public String byte2hex(byte[] b) {
		StringBuffer hs = new StringBuffer(b.length);
		String stmp = "";
		int len = b.length;
		for (int n = 0; n < len; n++) {
			
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (n == 0) {
				if (stmp.length() == 1)
					hs = hs.append("0").append(stmp);
				else {
					hs = hs.append(stmp);
				}
			} else {
				if (stmp.length() == 1)
					hs = hs.append(":").append("0" + stmp);
				else {
					hs = hs.append(":" + stmp);
				}
			}
		}
		return String.valueOf(hs);
	}
	
	public class NetworkDetailInfo
	{
		 public  String ip = null;
		 public String mac = null;
		 public String gateway = null;
		 public String netmask = null;
		 public  String dns1 = null;
		 public  String dns2 = null;
		 public  int speed = 0;
		 public String ssid =null;
	}
	

	
	/**
     * @return 
     */  
    public WifiConfiguration getHistoryWifiConfig(){  
    	WifiConfiguration wifiConfig = null;
    	WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
    	List<WifiConfiguration> configuredNetworks = mWifiManager.getConfiguredNetworks();
    	for (WifiConfiguration conf : configuredNetworks) {
    		if (conf.networkId == connectionInfo.getNetworkId()) {
    			wifiConfig = conf;
    			break;
    		}
    	}
    	return wifiConfig;
    }
    
    /**
     * @param ip
     * @param gateway
     * @param prefixLength
     * @param dns
     * @throws Exception
     */
    public void setStaticIp(String ip,String gateway,
    		int prefixLength,String dns) throws Exception{          
        WifiConfiguration historyWifiConfig = getHistoryWifiConfig();
        setIpAssignment("STATIC", historyWifiConfig); //"STATIC" or "DHCP" for dynamic setting  
        setIpAddress(InetAddress.getByName(ip),(prefixLength == 0) ? 24 : prefixLength, historyWifiConfig);  
        setGateway(InetAddress.getByName(gateway), historyWifiConfig);  
        setDNS(InetAddress.getByName(dns), historyWifiConfig);  
          
        mWifiManager.updateNetwork(historyWifiConfig); //apply the setting
        
    }
        
    public static void setIpAssignment(String assign, WifiConfiguration wifiConf)throws SecurityException, 
    	IllegalArgumentException,NoSuchFieldException, IllegalAccessException {  
        setEnumField(wifiConf, assign, "ipAssignment");  
    }
    
    @SuppressWarnings("unchecked")
	public static void setIpAddress(InetAddress addr, int prefixLength,WifiConfiguration wifiConf) throws SecurityException,
		IllegalArgumentException,NoSuchFieldException,IllegalAccessException, NoSuchMethodException,ClassNotFoundException,
		InstantiationException,InvocationTargetException {  
        Object linkProperties = getField(wifiConf, "linkProperties");  
        if (linkProperties == null)  
            return;  
        Class<?> laClass = Class.forName("android.net.LinkAddress");  
        Constructor<?> laConstructor = laClass.getConstructor(new Class[] {InetAddress.class, int.class });  
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);  
		ArrayList<Object> mLinkAddresses = (ArrayList<Object>) getDeclaredField(linkProperties,"mLinkAddresses");
        mLinkAddresses.clear();  
        mLinkAddresses.add(linkAddress);  
    }
    
    @SuppressWarnings("unchecked")
	public static void setGateway(InetAddress gateway,WifiConfiguration wifiConf) throws SecurityException,
		IllegalArgumentException,NoSuchFieldException,IllegalAccessException, ClassNotFoundException,
		NoSuchMethodException, InstantiationException,InvocationTargetException {  
        Object linkProperties = getField(wifiConf, "linkProperties");  
        if (linkProperties == null)  
            return;  
        Class<?> routeInfoClass = Class.forName("android.net.RouteInfo");  
        Constructor<?> routeInfoConstructor = routeInfoClass.getConstructor(new Class[] { InetAddress.class });  
        Object routeInfo = routeInfoConstructor.newInstance(gateway);  
        ArrayList<Object> mRoutes = (ArrayList<Object>) getDeclaredField(linkProperties,"mRoutes");  
        mRoutes.clear();  
        mRoutes.add(routeInfo);  
    }
    
    @SuppressWarnings("unchecked")
	public static void setDNS(InetAddress dns, WifiConfiguration wifiConf) throws SecurityException,
		IllegalArgumentException,NoSuchFieldException, IllegalAccessException {  
        Object linkProperties = getField(wifiConf, "linkProperties");  
        if (linkProperties == null)  
            return;
		ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mDnses");  
        mDnses.clear(); // or add a new dns address , here I just want to replace DNS1  
        mDnses.add(dns);  
    }
    
    @SuppressWarnings({"unchecked", "rawtypes" })
	public static void setEnumField(Object obj, String value, String name) throws SecurityException,
		NoSuchFieldException,IllegalArgumentException, IllegalAccessException {  
    	Field f = obj.getClass().getField(name);  
    	f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));  
    }

    public static Object getField(Object obj, String name) throws SecurityException,
    	NoSuchFieldException,IllegalArgumentException, IllegalAccessException {  
        Field f = obj.getClass().getField(name);  
        Object out = f.get(obj);  
        return out;  
    }
    public static Object getDeclaredField(Object obj, String name) throws SecurityException,
    	NoSuchFieldException,IllegalArgumentException, IllegalAccessException {  
        Field f = obj.getClass().getDeclaredField(name);  
        f.setAccessible(true);  
        Object out = f.get(obj);  
        return out;  
    }
    

}
