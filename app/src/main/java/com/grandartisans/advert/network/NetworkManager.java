package com.grandartisans.advert.network;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.grandartisans.advert.interfaces.NetworkEventListener;
import com.grandartisans.advert.utils.CommonUtil;


public class NetworkManager {
	private static String TAG = "NetworkManager";
	private Context mContext;
	//private NetManager netMgr = null;
	private MyReceiver myReceiver = null;
	public static final int KEYBOARD = 0;
	private static final int NET_REFRESH_WIFI_LIST = 1;
	private static final int NET_REFRESH_WIFI_STATE_INFO = 2;
	private final static int WIFI_CONNECTION_FAIL = 3;
	private boolean disconnectByUser = false;
	private boolean isAuthFailed = false;
	ScanResult conecting_ap;
	ScanResult selected_ap;
	ScanResult conected_ap;
	private WifiInfo currentWifiInfo;
	private List<ScanResult> scanResultList; 
	int wifiIndex = 0;
	private CheckWifiConnectionThread  checkWifiThread = null;
	private static final int SHOWWIFILIST = 4;
	private static final int DETECTENTHERNET_SUCCESS = 5;
	private static final int DETECTENTHERNET_FIAL = 6;
	private static final int DETECTWIRELESS = 7;
	private static final int DISCONNECTWIFI = 8;
	private static final int NOCONNECT = 9;
	private static final int SHOWENTERPASSWORD = 10;
	private static final int SHOWWIFICONNECTING = 11;
	private static final int DISCONNECTETHERNET = 12;
	private boolean lastIsEthernet = false;
	private volatile static NetworkManager mNetworkManagerInstance = null;
	private WifiManager mWifiManager;
	private ConnectivityManager mConnectivityManager;
	private NetworkEventListener mNetworkEventListener = null;

	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_WEP = 1;
	public static final int SECURITY_WPA_WPA2 = 2;
	public static final int SECURITY_WPA2 = 3;
	public static final int SECURITY_WPA = 4;
	public static final int SECURITY_EAP = 5;

	public NetworkManager(Context mContext){
		this.mContext = mContext;
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        scanResultList = getScanResults();
        if (scanResultList == null) {
            scanResultList = new ArrayList<ScanResult>();
        }
	}
	public static NetworkManager getInstance(Context context) {
		if (mNetworkManagerInstance == null) {
			synchronized (NetworkManager.class) {
				if (mNetworkManagerInstance == null) {
					mNetworkManagerInstance = new NetworkManager(context);
				}
			}
		}
		return mNetworkManagerInstance;
	}
	public void  registerListener(NetworkEventListener listener){
		mNetworkEventListener = listener;
	}
	
	public void setWifiEnable(boolean value) {
		if(!isWirePluggedIn()) {
			if(value == true) {
				enableWifi();
			}
		}
	}
	public void regiestbroadcast(){
		scanResultList = getScanResults();
		if (scanResultList == null) {
			scanResultList = new ArrayList<ScanResult>();
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		//filter.addAction(WindowManagerPolicy.ACTION_HDMI_HW_PLUGGED);
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		//filter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
		

		myReceiver = new MyReceiver();
		mContext.registerReceiver(myReceiver, filter);
		
	}
	
	private void displayWifiList(int t){
		if(mNetworkEventListener!=null) {
			mNetworkEventListener.onShowWifiList(scanResultList);
		}
	}
	
	private  List<ScanResult> getSortlist(){
		currentWifiInfo = getConnectionInfo();
		if(currentWifiInfo!=null&&currentWifiInfo.getSSID()!=null){
			for (int i = 0; i <scanResultList.size(); i++) {
				ScanResult result = scanResultList.get(i);
				if(("\"" + result.SSID + "\"").equals(currentWifiInfo.getSSID())){
					scanResultList.remove(i);
					scanResultList.add(0,result);
				}
			}
		}
		
		return scanResultList;
	}
	
	class MyReceiver extends BroadcastReceiver {
		String TAG = "MyReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "=======action========" + action);
			if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
				if (isWirePluggedIn()) {
					Log.d(TAG, "=======isEthEnable========" + action);
					 if(isWifiConnected()){ // wifi is connected  show wifi list
						    Log.d(TAG, "=======WIFI is connected  show wifi list ========" + action);
						    if(mNetworkEventListener!=null) mNetworkEventListener.onEthernetDisconnect();
						    if(scanResultList.size()>0){
						    	displayWifiList(SHOWWIFILIST);//huang
						    }
						    new ReFreshWifiDetailThread().start();
						    lastIsEthernet = false;
                         if(mNetworkEventListener!=null) mNetworkEventListener.onNoNetwork();
                         if(mNetworkEventListener!=null) mNetworkEventListener.onWifiConnected();
					}else if(isEthConnected()){//  Ethernet is connect
						Log.d(TAG, "=======Ethernet is connect ========" + action);
						displayCurEthInfo();
						disableWifi();
						disconnectByUser = false;
						lastIsEthernet = true ;
					}else {
						 if (!isWirePluggedIn() && disconnectByUser == false) {// wire is out
							 //Log.d(TAG, "=======Wired is plugout =start WIFI=======" + action);
								NetworkInfo info = getActiveNetwork();
								if(info != null){
									int type = info.getType();
									if(type == ConnectivityManager.TYPE_ETHERNET){
										Log.d(TAG, "===++++++++====Activi is Ethernet  ========" + action + "Connect state  ========" + getEtherConnected());
									}else if(type == ConnectivityManager.TYPE_WIFI){
										Log.d(TAG, "===++++++++====Activi is WIFI  ========" + action + "Connect state  ========" + getWifiConnectState());
									}
									     
								}
	                         
	                         if(lastIsEthernet){  //huang
	                              enableWifi();
	                         }
	                         lastIsEthernet = false;//huang
//	                         
						}else {
							Log.d(TAG, "======= show wifi list ========" + action);
						
						}
						
					}
				
  
				} else if (isWIFIEnable()) {
					Log.d(TAG, "============isWifiConnected:==" + isWifiConnected());
					
				}
			} else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				         procesWIFIScanResult();
			} else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
				         processWifiState(intent);
			}else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
			             processWifiConnectChange(intent);
			}else if(!isWirePluggedIn()&&!isWifiConnected()){
				if(mNetworkEventListener!=null) mNetworkEventListener.onNoNetwork();
				if(mNetworkEventListener!=null) mNetworkEventListener.onEthernetDisconnect();
			}
		}
	}
	
	private void displayCurEthInfo(){
		if (isWirePluggedIn()) {
			if(mNetworkEventListener!=null) mNetworkEventListener.onWifiConnecteFailed();
			if(mNetworkEventListener!=null) mNetworkEventListener.onEthernetConnected();
		}else{
			if(mNetworkEventListener!=null) mNetworkEventListener.onEthernetDisconnect();
		}
	}
	private void disableWifi()
	{
		enableWIFI(false);
		if (scanResultList != null) {
			 scanResultList.clear();
		}
	}
	
	private void enableWifi() {	
		if (!isWIFIEnable())
			enableWIFI(true);
	}
	public void startScan() {
		if(!isWirePluggedIn())
			enableWifi();
		mWifiManager.startScan();
	}
	
	

	
	class ReFreshWifiDetailThread extends Thread
	{
		@Override
		public void run() {
					int strength = 0;
					String ipaddress ="";
					for (int i = 0; i < 10; i++) {
						currentWifiInfo = getConnectionInfo();// �õ�������Ϣ
						int ip = currentWifiInfo.getIpAddress();
						ipaddress = (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
						if (ip != 0 && ( ipaddress.length() > 4)) {
								break;
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if(mNetworkEventListener!=null) mNetworkEventListener.onWifiListRefresh();
			}
	}
	
	private void procesWIFIScanResult() {
//		displayWifiScanView(false);
		ArrayList<ScanResult> accessPoints = new ArrayList<ScanResult>();
		final List<ScanResult> results = getScanResults();
		if (results != null) {
			for (ScanResult result : results) {
				if (TextUtils.isEmpty(result.SSID) || result.capabilities.contains("[IBSS]")) {
					continue;
				}
				Log.d("procesWIFIScanResult", "SSID" + result.SSID);
				
				boolean found = false;
	            for (ScanResult ap : accessPoints) {
	                  if (result.SSID.equals(ap.SSID)){
	                	  if(getSecurityType(result)==getSecurityType(ap)){
	                        found = true;
	                	  }
	                  }
	            }
	            if (!found) {
	            	accessPoints.add(result);
	            }
			}
			
			 
		}

		if (accessPoints.size() > 0 && scanResultList.size() != accessPoints.size()) {
			scanResultList.clear();
			scanResultList.addAll(accessPoints);
			if(scanResultList.size()>0){
				displayWifiList(NET_REFRESH_WIFI_LIST);//huang
			}
		}
	}
	
	public void processWifiListClicked(int position) {
		if(!mWifiManager.isWifiEnabled())
		{
				Log.w("processWifiListClicked", "Clicked AP ,when Wifi is off!");
				enableWIFI(true);
		}
		if (position >= scanResultList.size()) {
			 Log.e("processWifiListClicked", " Select AP out of WIFI List:"+position +"/"+ scanResultList.size() );
		} else {
			ScanResult result = scanResultList.get(position);
			wifiIndex = position;
//			pushTofront(wifiIndex);
			
			final int secureType = getSecurityType(result);
			
			 Log.e("processWifiListClicked", " Select AP :"+result.SSID );
			
			currentWifiInfo = getConnectionInfo();// �õ�������Ϣ
			if (isWifiConnected() && currentWifiInfo.getBSSID() != null && !"".equals(currentWifiInfo.getBSSID()) && currentWifiInfo.getBSSID().equals(scanResultList.get(position).BSSID)) {

			} else {
				String ssid = currentWifiInfo.getSSID();
				ssid =ssid.substring(1,ssid.length() -1);
				//conecting_ap =  netMgr.getScanResultBySSID(ssid);
				     
				if((conecting_ap!= null && conecting_ap.SSID.equals(result.SSID))  || (result.SSID.equals(ssid) && getWifiConnectState() == State.CONNECTING)){ // current try connecting
					 System.out.println("+++++++++++++++++++++++++Selected wifi is connecting++++++++++");
		         	if(isAuthFailed)
					 return ;
				}
				
				if (isWifiSaved(result) /*&& !netMgr.isWifiConfDisable(result)*/) {
					  Log.d("processWifiListClicked", "AP: " + result.SSID + " Has Conf");
					  selected_ap = result;
					  
					   disconnectByUser = true;
				   	   conecting_ap = selected_ap;
				   	   Log.d("processWifiListClicked", "Select AP "  + " To Connect" );
				   	   doWifiConnect(selected_ap,null,null);
					 					  					 
					  return;
				}else {
					
				} 

				if (secureType == NetManager.SECURITY_NONE) {
					 selected_ap = result;
//					 hidNetDetail(true);//huang
					 disconnectByUser = true;
					 doWifiConnect(selected_ap,null,null);
				} else {
					selected_ap = result; //���������?
					if(mNetworkEventListener!=null) mNetworkEventListener.showPassword(selected_ap);
				}
			}
		}
	}
	
	
//	private void pushTofront(int position){
//		if(position!=0){
//		ScanResult result = scanResultList.get(position);
//		scanResultList.remove(position);
//		scanResultList.add(0,result);
//		}
//	}
	public void ignore(int position){ //���Դ�����
		if(scanResultList!=null&&scanResultList.size()>0){
			ScanResult result = scanResultList.get(position);
			selected_ap = result;
			 WifiConfiguration conf =  getSavedWifiConfig(selected_ap);
			   if(conf != null){
				   int netId = conf.networkId;
				   mWifiManager.disableNetwork(netId);
				   mWifiManager.removeNetwork(netId);   // ignore the network
				   mWifiManager.saveConfiguration();
			   }
	 			selected_ap = null;	
	 			conecting_ap = null;
	 			 if(scanResultList.size()>0){
	 				 displayWifiList(NET_REFRESH_WIFI_LIST);
	 			 }
	 			 if(mNetworkEventListener!=null) mNetworkEventListener.onNoNetwork();
		}
	}
	
	public void connectingWifi(String passwd){ //����������ɴ˴�����ȥ��������?
		conecting_ap = selected_ap;
		doWifiConnect(conecting_ap,passwd,"");
	}
	private void processWifiState(Intent intent) {
		WifiInfo info = getConnectionInfo();
		SupplicantState state = info.getSupplicantState();

   		String str = null;
		if (state == SupplicantState.ASSOCIATED) {
			str = "关联AP完成";
		} else if (state.toString().equals("AUTHENTICATING")) {
			str = "正在验证";
			String ssid = info.getSSID();
			ssid =ssid.substring(1,ssid.length() -1);
			conecting_ap =  getScanResultBySSID(ssid);
			if(conecting_ap != null)
				   showWifiConnecting(conecting_ap.SSID);
		} else if (state == SupplicantState.ASSOCIATING) {
			str = "正在关联AP...";
		} else if (state == SupplicantState.COMPLETED) {
			str = "已连接";
		} else if (state == SupplicantState.DISCONNECTED) {
			str = "已断开";
		} else if (state == SupplicantState.DORMANT) {
			str = "暂停活动";
		} else if (state == SupplicantState.FOUR_WAY_HANDSHAKE) {
			str = "四路握手";
			String ssid = info.getSSID();
			ssid =ssid.substring(1,ssid.length() -1);
			conecting_ap =  getScanResultBySSID(ssid);
			if(conecting_ap != null)
			   showWifiConnecting(conecting_ap.SSID);
		} else if (state == SupplicantState.GROUP_HANDSHAKE) {
			str = "GROUP_HANDSHAKE";
		} else if (state == SupplicantState.INACTIVE) {
			str = "休眠";
			if(isAuthFailed ){
				Log.d("ProcessWifiState", "++++++++++++++" +info.getSSID()+" ++++++++=" + str);
				if(mNetworkEventListener!=null) mNetworkEventListener.onWifiConnecteFailed();
				conecting_ap = null; //clear the data when wifi connect fail
				if(checkWifiThread != null) checkWifiThread.needStop = true;
				isAuthFailed = false;
				return ;
			}
		} else if (state == SupplicantState.INVALID) {
			str = "无效";
		} else if (state == SupplicantState.SCANNING) {
			str = "扫描中";
		} else if (state == SupplicantState.UNINITIALIZED) {
			str = "未初始化";
		}
		Log.d("ProcessWifiState", "++++++++++++++" +info.getSSID()+" ++++++++=" + str);
		final int errorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
		if (errorCode == WifiManager.ERROR_AUTHENTICATING) { // AUTHENTICATING
																// FAILED
			  Log.d("ProcessWifiState", "++++++++++?????++++++" +info.getSSID()+" ++++++=WIFI验证失败");
			  isAuthFailed = true;
			  
			 WifiInfo info2 =   getConnectionInfo();
			 WifiConfiguration config=  getSavedWifiConfig(conecting_ap);
			 
			
			 if(config != null){
				 Log.d("ProcessWifiState", "++++++++++Remove config" + info2.getSSID());
				  int id = config.networkId;
				 mWifiManager.disableNetwork(id);
				 mWifiManager.removeNetwork(id);
				 mWifiManager.saveConfiguration();
			 }
		}
	}
	
	private void showWifiConnecting(String ssid){
//		if(ssid != null) //huang
//			settingsPageThreeStatusContent.setText( "\"" + ssid +" \"" +getResources().getString(R.string.string_wifi_connecting));
//		 settingsPageThreeStatusContent.setVisibility(View.VISIBLE);//huang
		if(mNetworkEventListener!=null) mNetworkEventListener.onWifiConnecting();
	}
	
	private void processWifiConnectChange(Intent intent)
	{
		  //WifiInfo info = netMgr.getWifiMgr().getConnectionInfo();
		  Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);    
          if (null != parcelableExtra) {    
              NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;    
		      Log.d("WifiConnectChange", "++++++++++WIFI :"+ networkInfo.getState());
		      showWifiConnectionState(networkInfo);
          }
	}
	
	private void showWifiConnectionState(NetworkInfo networkInfo)
	{
		 WifiInfo info = getConnectionInfo();
		 if(info != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI ){
			 // info.getSSID();
			 // Log.d("processWifiConnectChange", "++++++++++WIFI :"+ info.getState());
			 if(networkInfo.getState() == State.CONNECTED || networkInfo.getState() == State.CONNECTING){
//				 settingsPageThreeStatusContent.setText(   info.getSSID() +" " +getResources().getString(R.string.string_wifi_connecting));
//				 settingsPageThreeStatusContent.setVisibility(View.VISIBLE);//huang
//				 myHandler.sendEmptyMessage(SHOWWIFICONNECTING);//��ʾ��������text
			 }
		 }
	}
	
	class CheckWifiConnectionThread extends Thread {
        boolean needStop = false;
		@Override
		public void run() {

			int i = 0;
			WifiInfo	curInfo = null;
			for ( i = 0; i < 60 && needStop == false; i++) {
					curInfo = getConnectionInfo();
					if(curInfo != null){
						  SupplicantState state  = curInfo.getSupplicantState();
						  if(SupplicantState.COMPLETED == state  ){
							      break;
						  } else if (SupplicantState.INACTIVE == state){
							      
						  }
					}
					try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
			}
			
			if(needStop) return ;
			if(i>=60){// send connection time out msg
				 Log.d("CheckWifiConnection", "   ++++++++++++ wifi connection time out  :"+ curInfo.getSSID());
				 if(mNetworkEventListener!=null) mNetworkEventListener.onWifiConnecteFailed();
				 WifiInfo info2 =   getConnectionInfo();
				 WifiConfiguration config=  getSavedWifiConfig(conecting_ap);
				 if(config != null){
					 Log.d("CheckWifiConnection", "++++++++++Remove config" + info2.getSSID());
					  int id = config.networkId;
					  mWifiManager.disableNetwork(id);
					  mWifiManager.removeNetwork(id);
					  mWifiManager.saveConfiguration();
				 }
				 
			}
			
			super.run();
		}
	}
	
	public void doWifiConnect(ScanResult ap,String password, String username){
		conecting_ap = ap;
		 connect2AccessPoint(ap, password, "");
		 showWifiConnecting(ap.SSID);
		 
		 if(checkWifiThread != null) checkWifiThread.needStop = true;
		  checkWifiThread = new CheckWifiConnectionThread();
		  checkWifiThread.start();
		  
	}

	public void unreigest(){
		if(myReceiver!=null) {
			mContext.unregisterReceiver(myReceiver);
			myReceiver = null;
		}
	}

	public boolean isEthConnected() {
		NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return false;
		}
		Log.e(TAG,"active networkInfo type is :" + networkInfo.getType() + "state :" + networkInfo.getState());
        if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
            if (State.CONNECTED == networkInfo.getState()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
	}

	public boolean isDataConnected()
	{
		ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo [] networkInfos=connMgr.getAllNetworkInfo();
		for (int i = 0; i < networkInfos.length; i++) {
			Log.d(TAG,"network info type " + networkInfos[i].getType() + "state = " + networkInfos[i].getState() + "typename = " + networkInfos[i].getTypeName());
		}
		NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isMobileConn = networkInfo.isConnected();
		Log.d(TAG, "isMobileConn = " + isMobileConn);
		return isMobileConn;
	}

	public String getEthernetMacAddress() {
		NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (networkInfo == null ||networkInfo.getType() != ConnectivityManager.TYPE_ETHERNET) {
			return "";
		} else {
			return networkInfo.getExtraInfo();
		}
	}
	public boolean isWIFIEnable() {
		int state = mWifiManager.getWifiState();
		if (state == WifiManager.WIFI_STATE_ENABLED) {
			return true;
		} else {
			return false;
		}
	}
	public void enableWIFI(boolean enable) {
		mWifiManager.setWifiEnabled(enable);
	}
	public boolean isWifiConnected() {
		return WifiUtils.isWifiConnected(mContext) && this.isWIFIEnable();
	}

	public State getWifiConnectState() {
		return WifiUtils.getWifiConnectState(mContext);
	}

	public State getEtherConnected() {
		return WifiUtils.getEthConnectState(mContext);
	}
	public NetworkDetailInfo getNetworkDetailInfo(){
		NetworkDetailInfo info = new NetworkDetailInfo();
		DhcpInfo mDhcpInfo = null;
		int speed = 0;
		String mac = null;
		String ssid = null;
		if(isWifiConnected()){
			mDhcpInfo = mWifiManager.getDhcpInfo();
			speed =mWifiManager.getConnectionInfo().getLinkSpeed();
			mac = mWifiManager.getConnectionInfo().getMacAddress();
			ssid = mWifiManager.getConnectionInfo().getSSID();
			if(mDhcpInfo != null){
				info.setIp(int2ip(mDhcpInfo.ipAddress));
				info.setGateway(int2ip(mDhcpInfo.gateway));
				info.setNetmask(int2ip( mDhcpInfo.netmask));
				info.setDns1(int2ip(mDhcpInfo.dns1));
				info.setDns2(int2ip(mDhcpInfo.dns2));
				info.setMac(mac);
				info.setSsid(ssid);
			}
		}else if(isEthConnected()){
			/*
			info.ip = SystemProperties.get("dhcp.eth0.ipaddress","0.0.0.0");
			info.gateway = SystemProperties.get("dhcp.eth0.gateway","0.0.0.0");
			info.netmask = SystemProperties.get("dhcp.eth0.mask","0.0.0.0");
			info.dns1 = SystemProperties.get("dhcp.eth0.dns1","0.0.0.0");
			info.dns2 = SystemProperties.get("dhcp.eth0.dns2","0.0.0.0");
			info.mac = getLocalEthMacAddress();
			*/
		}
		return info;
	}
	public ScanResult getScanResultBySSID(String ssid){
		List<ScanResult> results = mWifiManager.getScanResults();
		if (results != null) {
			for (ScanResult result : results) {
				System.out.println("ssid" + ssid + "  ssid2:" +result.SSID);
				if(ssid.equals(result.SSID)){
					return result;
				}
			}
		}
		return null;
	}
	public int calSignalLevel(int rssi,int numLevels){
		return mWifiManager.calculateSignalLevel(rssi, 4);
	}
	public WifiInfo getConnectionInfo(){
		return mWifiManager.getConnectionInfo();
	}
	public List<ScanResult> getScanResults(){
		return mWifiManager.getScanResults();
	}
	public  int getSecurityType(ScanResult result) { // CISCO MODIFY, from
		// private to public
		//System.out.println("capabilities = " + result.capabilities);
		if (result.capabilities.contains("WEP")) {
			return SECURITY_WEP;
		} else if (result.capabilities.contains("WPA2-PSK") && result.capabilities.contains("WPA-PSK")) {
			return SECURITY_WPA_WPA2;
		} else if (result.capabilities.contains("WPA2-PSK")) {
			return SECURITY_WPA2;
		} else if (result.capabilities.contains("WPA-PSK")) {
			return SECURITY_WPA;
		} else if (result.capabilities.contains("EAP")) {
			return SECURITY_EAP;
		}
		return SECURITY_NONE;
	}

	public  NetworkInfo getActiveNetwork() {
		NetworkInfo info  = mConnectivityManager.getActiveNetworkInfo();
		return info;
	}

	public String int2ip(long ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}
	//判断网线拔插状态
	//通过命令cat /sys/class/net/eth0/carrier，如果插有网线的话，读取到的值是1，否则为0
	public boolean isWirePluggedIn(){
		String state= CommonUtil.execCommand("cat /sys/class/net/eth0/carrier");
		if(state.trim().equals("1")){  //有网线插入时返回1，拔出时返回0
			return true;
		}
		return false;
	}
	public void connect2AccessPoint(ScanResult scanResult, String password, String usrname) {

		int securityType = getSecurityType(scanResult);

		WifiConfiguration config = getSavedWifiConfig(scanResult);

		mWifiManager.disconnect();
		if (config == null /*|| isWifiConfDisable(scanResult)*/) {
			Log.d(TAG, "===== It's a new AccessPoint!!! ");
			config = createWifiConfig(scanResult, password, usrname);
			// config.priority = 1;
			config.status = WifiConfiguration.Status.ENABLED;
			int netId = mWifiManager.addNetwork(config);
			mWifiManager.enableNetwork(netId, true);
			mWifiManager.saveConfiguration();
		} else {
			Log.d(TAG, "===== It's a saved AccessPoint!!! ");
			int netId  = config.networkId;
			if (password != null && password.length() > 0) {
				Log.d(TAG, "===== It's a saved AccessPoint, Need Update password ! ");
				mWifiManager.removeNetwork(config.networkId);
				config = createWifiConfig(scanResult, password, usrname);
				netId = mWifiManager.addNetwork(config);
			}else {
				Log.d(TAG, "===== It's a saved AccessPoint,  Enable the config ! ");
			}

			config.status = WifiConfiguration.Status.ENABLED;
			mWifiManager.enableNetwork(netId, true);
			mWifiManager.saveConfiguration();
			// mWifiManager.updateNetwork(config);
		}

	}
	public WifiConfiguration getSavedWifiConfig(ScanResult scanResult) {
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		if(existingConfigs!=null && existingConfigs.size()>0 && scanResult!=null){
			for (WifiConfiguration existingConfig : existingConfigs) {

				if (existingConfig != null &&  existingConfig.SSID != null && existingConfig.SSID.equals("\"" + scanResult.SSID + "\"")) {
					return existingConfig;
				}
			}
		}
		return null;
	}
	public WifiConfiguration createWifiConfig(ScanResult scanResult, String Password, String username) {

		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + scanResult.SSID + "\"";

		if (isWifiSaved(scanResult)) {
			removWifiConf(scanResult);
		}
		int Type = getSecurityType(scanResult);

		if (Type == SECURITY_NONE) // WIFICIPHER_NOPASS
		{
			//config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			//config.wepTxKeyIndex = 0;
		}
		if (Type == SECURITY_WEP) // WIFICIPHER_WEP
		{
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == SECURITY_WPA_WPA2 || Type == SECURITY_WPA2 || Type == SECURITY_WPA) // WIFICIPHER_WPA
		{
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		if (Type == SECURITY_EAP) {
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
			/*
			 * config.eap.setValue((String) mEapMethod.getSelectedItem());
			 *
			 * config.phase2.setValue((mPhase2.getSelectedItemPosition() == 0) ?
			 * "" : "auth=" + mPhase2.getSelectedItem());
			 * config.ca_cert.setValue((mEapCaCert.getSelectedItemPosition() ==
			 * 0) ? "" : KEYSTORE_SPACE + Credentials.CA_CERTIFICATE + (String)
			 * mEapCaCert.getSelectedItem());
			 * config.client_cert.setValue((mEapUserCert
			 * .getSelectedItemPosition() == 0) ? "" : KEYSTORE_SPACE +
			 * Credentials.USER_CERTIFICATE + (String)
			 * mEapUserCert.getSelectedItem());
			 * config.private_key.setValue((mEapUserCert
			 * .getSelectedItemPosition() == 0) ? "" : KEYSTORE_SPACE +
			 * Credentials.USER_PRIVATE_KEY + (String)
			 * mEapUserCert.getSelectedItem());
			 * config.identity.setValue((mEapIdentity.length() == 0) ? "" :
			 * mEapIdentity.getText().toString());
			 */
			/* 设置用户名，密码 */
			/*
			 * config.anonymous_identity.setValue(username); if
			 * (Password.length() != 0) { config.password.setValue(Password); }
			 */
		}
		return config;
	}
	public boolean isWifiSaved(ScanResult scanResult) {
		WifiConfiguration config = getSavedWifiConfig(scanResult);

		if (config == null) {
			System.out.println("WifiConfiguration:    NO!!!!!!!!");
			return false;
		}
		System.out.println("WifiConfiguration:    "+ config.SSID + "  has config");
		//if (config.status == WifiConfiguration.Status.DISABLED )
		//	 return false;
		return true;
	}
	public void removWifiConf(ScanResult scanResult) {
		WifiConfiguration config = getSavedWifiConfig(scanResult);
		if (config != null) {
			mWifiManager.removeNetwork(config.networkId);
		}
	}
	public void enableEthernet(boolean enable) {
		if(enable){
			CommonUtil.runCmd("ifconfig eth0 up");
		}else {
			CommonUtil.runCmd("ifconfig eth0 down");
		}
	}

}
