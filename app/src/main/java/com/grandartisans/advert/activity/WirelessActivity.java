package com.grandartisans.advert.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.grandartisans.advert.R;
import com.grandartisans.advert.interfaces.NetworkEventListener;
import com.grandartisans.advert.network.NetworkDetailInfo;
import com.grandartisans.advert.network.NetworkManager;
import com.grandartisans.advert.utils.DecodeImgUtil;
import com.grandartisans.advert.utils.DensityUtil;
import com.grandartisans.advert.utils.ValidUtils;

public class WirelessActivity extends Activity implements OnClickListener{
	
	private ListView wifiListView;
	private Context mContext;
	private NetworkWifiListAdapter mAdapter;
	private static final int SHOWWIFILIST = 4;
	private static final int NET_REFRESH_WIFI_LIST = 1;
	private NetworkManager networkManager;
	private List<ScanResult> scanResultList;
	private WifiInfo currentWifiInfo;
	private LinearLayout enterPassword,network_detial_ll;
	private final static int WIFI_CONNECTION_FAIL = 3;
	private LinearLayout add_wireless_ssid,add_wireless_encryption,add_wireless_password;
	private LinearLayout add_wireless_edit_ll,add_wireless_ll;
	private EditText EditPsd;
	private TextView wifiTitle;
	private static final int SHOWENTERPASSWORD = 10;
	private static final int SHOWWIFICONNECTING = 11;
	private int curPosition = 0;
	private EditText edit_ip;
	private EditText edit_yanma;
	private EditText edit_wangguan;
	private EditText edit_dns;
	private Button submit;
	private Button ignore;
	private Button update;
	private LinearLayout network_detial,network_detial_buttons;
	private NetworkDetailInfo netInfor;
	private LinearLayout ip_ll;
	private LinearLayout yanma_ll;
	private LinearLayout wangguan_ll;
	private LinearLayout dns_ll;
	private RelativeLayout network_detial_openbtn;
	private TextView network_detial_wifiTitle;

	private static final int DISCONNECTWIFI = 8;
	private static final String WHITE = "#FFFFFF";
	private boolean isFirst = true;
	private static final int NORMAL_FRONT_SIZE = 24;
	private static final int BIGGER_FRONT_SIZE = 26;
	private static final String LBULE = "#97FFFF";
	private RelativeLayout textTip_rl;
	private TextView textTip;
	private ImageView iconTip;
	private static final int WIFI_CONNECTION_SUCCESS = 13;
	private static final int HIDE_WIFI_CONNECTION_TIP = 14;
	private wifiOnKey mWifiOnkey;
	String Tip = "";
	private ImageView wifisignal;
	private String[] Encryptions = {"PSK","WEP","EAP"};
	private int rpIndex = 0;
	private String type = "";
	private ArrayList<Map<String, String>> networkInfo = new ArrayList<Map<String,String>>();
	private Map<String, String> map = null;
	private static final int NOCONNECT = 9;
	private List<ScanResult> ManuallyScanResultList = new ArrayList<ScanResult>(); 
	private InputMethodManager imm;
	private Bitmap b;
	private RelativeLayout main_rl;
	//private Typeface face;
	private boolean isUpdate = false;
	Handler myhandler = new Handler(){
	public void handleMessage(android.os.Message msg) {
			
			switch (msg.what) {  
			case NET_REFRESH_WIFI_LIST:
			case SHOWWIFILIST:
				scanResultList = (List<ScanResult>) msg.obj;
				displayWifiList();
				break;
			case SHOWENTERPASSWORD:
				hideView();
				enterPassword.setVisibility(View.VISIBLE);
				ScanResult sr = scanResultList.get(curPosition);
				wifiTitle.setText(sr.SSID);
				int wifi_de = networkManager.calSignalLevel(sr.level, 4);
				if (wifi_de == 1) {
					wifisignal.setImageResource(R.drawable.icon_wifi_1);
				} else if (wifi_de == 2) {
					wifisignal.setImageResource(R.drawable.icon_wifi_2);
				} else if (wifi_de == 3) {
					wifisignal.setImageResource(R.drawable.icon_wifi_3);
				} else {
					wifisignal.setImageResource(R.drawable.icon_wifi);
				}
				
				EditPsd.setText("");
				EditPsd.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(EditPsd, 0);
				break;
			case WIFI_CONNECTION_FAIL:
				textTip_rl.setVisibility(View.VISIBLE);
				textTip.setText(mContext.getString(R.string.wirelessTip_connectedFial));
				iconTip.setImageResource(R.drawable.red_wrong_icon);
				myhandler.sendEmptyMessageDelayed(HIDE_WIFI_CONNECTION_TIP, 5000);
				break;
			case SHOWWIFICONNECTING: 
				mAdapter.isConnected(false);
				textTip_rl.setVisibility(View.VISIBLE);
				textTip.setText(mContext.getString(R.string.wirelessTip_connecting));
				iconTip.setImageBitmap(null);
				break;
			case DISCONNECTWIFI: //
				if(isFirst){
					if(type.equals("boot")){	
						startDetectNetwork();
					}else{
						WirelessActivity.this.finish();
					}
				isFirst = false;
				}
				break;
			case WIFI_CONNECTION_SUCCESS:
				textTip_rl.setVisibility(View.GONE);
				textTip.setText(mContext.getString(R.string.wirelessTip_connectedSuccess));
				iconTip.setImageResource(R.drawable.blue_icon);
				myhandler.sendEmptyMessageDelayed(HIDE_WIFI_CONNECTION_TIP, 3000);
				saveNetwork();
				break;
			case HIDE_WIFI_CONNECTION_TIP:
				textTip_rl.setVisibility(View.GONE);
				textTip.setText("");
				iconTip.setImageBitmap(null);
				break;
			
			case NOCONNECT:
				initData();
				//app.setNetworkInfo(networkInfo);
				break;
			default:
				break; 
			}
			
		};
	};
	NetworkEventListener listener = new NetworkEventListener(){
		@Override
		public void onShowWifiList(List<ScanResult> list){
			scanResultList = list;
			displayWifiList();
		}
		@Override
		public void onEthernetDisconnect(){

		}
		@Override
		public void onNoNetwork(){
			initData();
		}
		@Override
		public void onEthernetConnected(){
		}
		@Override
		public void onWifiConnected(){
			textTip_rl.setVisibility(View.GONE);
			textTip.setText(mContext.getString(R.string.wirelessTip_connectedSuccess));
			iconTip.setImageResource(R.drawable.blue_icon);
			myhandler.sendEmptyMessageDelayed(HIDE_WIFI_CONNECTION_TIP, 3000);
			saveNetwork();
		}
		@Override
		public void onWifiConnecteFailed(){
			textTip_rl.setVisibility(View.VISIBLE);
			textTip.setText(mContext.getString(R.string.wirelessTip_connectedFial));
			iconTip.setImageResource(R.drawable.red_wrong_icon);
			myhandler.sendEmptyMessageDelayed(HIDE_WIFI_CONNECTION_TIP, 5000);
		}
		@Override
		public void onWifiConnecting(){
			mAdapter.isConnected(false);
			textTip_rl.setVisibility(View.VISIBLE);
			textTip.setText(mContext.getString(R.string.wirelessTip_connecting));
			iconTip.setImageBitmap(null);
		}
		@Override
		public void onWifiListRefresh()
		{

		}
		@Override
		public void showPassword(ScanResult result){
			hideView();
			enterPassword.setVisibility(View.VISIBLE);
			ScanResult sr = scanResultList.get(curPosition);
			wifiTitle.setText(sr.SSID);
			int wifi_de = networkManager.calSignalLevel(sr.level, 4);
			if (wifi_de == 1) {
				wifisignal.setImageResource(R.drawable.icon_wifi_1);
			} else if (wifi_de == 2) {
				wifisignal.setImageResource(R.drawable.icon_wifi_2);
			} else if (wifi_de == 3) {
				wifisignal.setImageResource(R.drawable.icon_wifi_3);
			} else {
				wifisignal.setImageResource(R.drawable.icon_wifi);
			}

			EditPsd.setText("");
			EditPsd.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(EditPsd, 0);
		}
	};
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.wireless_activity);
		//AgentApplication.getInstance().addActivity(this);
		mContext = this;
		main_rl = (RelativeLayout) findViewById(R.id.main_rl);
		b = new DecodeImgUtil(this, R.raw.settings_bg).getBitmap();
		BitmapDrawable bd= new BitmapDrawable(b);
		main_rl.setBackground(bd);
		//face = Typeface.createFromAsset (mContext.getAssets() , "LTXHGBK.TTF" );
		//initDataParams();
		type = getIntent().getStringExtra("type");
		networkManager = NetworkManager.getInstance(this);
		networkManager.registerListener(listener);
		networkManager.startScan();
		initView();
		initOnClick();
		initOnSelect();
		initOnKeyBoard();
		//initOnkey();
		initFocus();
		mWifiOnkey = new wifiOnKey(true);
		wifiListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if(scanResultList.size()-1==position){//锟街讹拷锟斤拷锟�?
					hideView();
				}else{
//					pushTofront(position);
					
				int	ClickItem = position;
				boolean isgo = true;
					netInfor = networkManager.getNetworkDetailInfo();
					currentWifiInfo = networkManager.getConnectionInfo();
					if(netInfor!=null&&netInfor.getSsid()!=null&&!netInfor.getSsid().equals("")
							&&currentWifiInfo!=null&&currentWifiInfo.getSSID()!=null
							&&!currentWifiInfo.getSSID().equals("")){
						String wifiName = scanResultList.get(ClickItem).SSID;
						String bssid = scanResultList.get(ClickItem).BSSID;
						if(netInfor.getSsid().equals("\""+wifiName+"\"")&&currentWifiInfo.getBSSID().equals(bssid)){
							hideView();
							network_detial_wifiTitle.setText(wifiName);
							initDetialData();
							network_detial_ll.setVisibility(View.VISIBLE);
							submit.requestFocus();
							isgo = false;
						}
					}
					if(isgo)
					networkManager.processWifiListClicked(ClickItem);
					wifiListView.setSelection(ClickItem);
				}
			}
		});
		wifiListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				curPosition  = arg2;
				mAdapter.setCurItem(arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		wifiListView.setOnKeyListener(mWifiOnkey);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	class wifiOnKey implements OnKeyListener{
		
		private boolean b;
		public wifiOnKey(boolean b){
			this.b = b;
		}
		@Override
		public boolean onKey(View arg0, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if(b){
				return false;
			}else{
				return true;
			}
		}
		
	}

	private void initDetialData(){
		edit_ip.setText(netInfor.getIp());
		edit_yanma.setText(netInfor.getNetmask());
		edit_wangguan.setText(netInfor.getGateway());
		edit_dns.setText(netInfor.getDns1());
	}
	private void initOnClick(){
		edit_ip.setOnClickListener(this);
		edit_yanma.setOnClickListener(this);
		edit_wangguan.setOnClickListener(this);
		edit_dns.setOnClickListener(this);
		update.setOnClickListener(this);
		submit.setOnClickListener(this);
		network_detial_openbtn.setOnClickListener(this);
		ignore.setOnClickListener(this);
		EditPsd.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(networkManager!=null)
			networkManager.unreigest();
		if(b!=null) b.recycle();	
	}

	private void hideView(){
		wifiListView.setVisibility(View.GONE);
		enterPassword.setVisibility(View.GONE);
		network_detial_ll.setVisibility(View.GONE);
	}
	
	private void initView(){
		wifiListView = (ListView) findViewById(R.id.wifiList);
		enterPassword = (LinearLayout) findViewById(R.id.enterPassword);
		//addNetwork = (LinearLayout) findViewById(R.id.addNetwork);
		network_detial_ll = (LinearLayout) findViewById(R.id.network_detial_ll);
		/*
		add_wireless_ssid = (LinearLayout) findViewById(R.id.add_wireless_ssid);
		add_wireless_encryption = (LinearLayout) findViewById(R.id.add_wireless_encryption);
		add_wireless_password = (LinearLayout) findViewById(R.id.add_wireless_password);
		add_wireless_edit_ll = (LinearLayout) findViewById(R.id.add_wireless_edit_ll);
		add_wireless_submit = (Button) findViewById(R.id.add_wireless_submit);
		add_wireless_ll = (LinearLayout) findViewById(R.id.add_wireless_ll);
		add_wireless_edit_edit = (EditText) findViewById(R.id.add_wireless_edit_edit);
		add_wireless_edit_text = (TextView) findViewById(R.id.add_wireless_edit_text);
		add_wireless_password_text = (TextView) findViewById(R.id.add_wireless_password_text);
		add_wireless_ssid_text = (TextView) findViewById(R.id.add_wireless_ssid_text);
		*/
		wifiTitle = (TextView) findViewById(R.id.wifiTitle);
		EditPsd = (EditText) findViewById(R.id.EditPsd);
		network_detial = (LinearLayout) findViewById(R.id.network_detial);
		edit_ip = (EditText) findViewById(R.id.edit_ip);
		edit_yanma = (EditText) findViewById(R.id.edit_yanma);
		edit_wangguan = (EditText) findViewById(R.id.edit_wangguan);
		edit_dns = (EditText) findViewById(R.id.edit_dns);
		submit = (Button) findViewById(R.id.network_detial_submit);
		ignore = (Button) findViewById(R.id.network_detial_ignore);
		update = (Button) findViewById(R.id.network_detial_update);
		ip_ll = (LinearLayout) findViewById(R.id.network_detial_ip_ll);
		yanma_ll = (LinearLayout) findViewById(R.id.network_detial_yanma_ll);
		wangguan_ll = (LinearLayout) findViewById(R.id.network_detial_wangguan_ll);
		dns_ll = (LinearLayout) findViewById(R.id.network_detial_dns_ll);
		network_detial_buttons = (LinearLayout) findViewById(R.id.network_detial_buttons);
		network_detial_openbtn = (RelativeLayout) findViewById(R.id.network_detial_openbtn);
		network_detial_wifiTitle = (TextView) findViewById(R.id.network_detial_wifiTitle);
		/*
		add_wireless_password_edit = (EditText) findViewById(R.id.add_wireless_password_edit);
		add_wireless_ssid_edit = (EditText) findViewById(R.id.add_wireless_ssid_edit);
		*/
		textTip_rl = (RelativeLayout) findViewById(R.id.textTip_rl);
		textTip = (TextView) findViewById(R.id.textTip);
		iconTip = (ImageView) findViewById(R.id.iconTip);
		wifisignal = (ImageView) findViewById(R.id.wifisignal);
		/*
		add_wireless_encryption_text = (TextView) findViewById(R.id.add_wireless_encryption_text);
		*/
		textTip_rl.setVisibility(View.GONE);
		setFont(EditPsd);
		//scanResultList = app.getScanResult();
		scanResultList = networkManager.getScanResults();
		if (scanResultList == null) {
			scanResultList = new ArrayList<ScanResult>();
		}
		if(scanResultList!=null){
			displayWifiList();
		}
	}
	private void displayWifiList() {
		if (wifiListView == null || scanResultList == null)
			return;		
		int selected = wifiListView.getSelectedItemPosition();
		mAdapter = new NetworkWifiListAdapter(scanResultList);
		mAdapter.setCurItem(selected);
		updateWifiState();
		wifiListView.setAdapter(mAdapter);
		wifiListView.setDividerHeight(0);
		if (selected > (scanResultList.size() - 1)) {
			wifiListView.setSelection(0);
		} else {
			wifiListView.setSelection(selected);
		}
	}
	
	private void updateWifiState() {
		if(networkManager!=null){
			WifiInfo info = networkManager.getConnectionInfo();
			if (info != null && info.getNetworkId() != -1) {
				Log.i("WIFI", "info.getSSID, " + info.getSSID());
				if (mAdapter != null) {
					mAdapter.UpdateConnectAccesspoint(info.getSSID(), networkManager.isWifiConnected());
				}
			}
		}
	}
		
	class NetworkWifiListAdapter extends BaseAdapter {
		private List<ScanResult> mWifiList;
		private boolean mConnected = false;
		private ViewHolder holder = null;
		private int selectItem = 0;
		
		
		
		ImageView rightIconComm;
		
		public NetworkWifiListAdapter(List<ScanResult> list) {

			this.mWifiList = list;
//			FilterNamesakeWifi();
			if(mWifiList!=null&&(mWifiList.size()==0||!mWifiList.get(mWifiList.size()-1).BSSID.equals("23e"))){
				this.mWifiList.addAll(ManuallyScanResultList);
			} 
			
		}
		public void addManuallyWifi(){
			if(mWifiList!=null&&(mWifiList.size()==0||mWifiList.get(mWifiList.size()-1).BSSID.equals("23e"))){
				if(mWifiList.size()>0){
					mWifiList.remove(mWifiList.size()-1);
				}
				this.mWifiList.addAll(ManuallyScanResultList);
			}
			notifyDataSetChanged();
		}
		
		public void delManuallyWifi(String wifiName,String bssid){
			int m = -1;
			if(mWifiList!=null&&mWifiList.get(mWifiList.size()-1).BSSID.equals("23e")){
				for (int i = 0; i < mWifiList.size(); i++) {
					if(mWifiList.get(i).SSID.equals(wifiName)&&bssid!=null&&mWifiList.get(i).BSSID.equals(bssid)){
						m = i;
					}
				}
				if(m>-1){
					mWifiList.remove(m);
				}
			}
			notifyDataSetChanged();
		}
		private void FilterNamesakeWifi(){
			ScanResult sr1;
			ScanResult sr2;
			for (int i = 0; i < mWifiList.size(); i++) {
				sr1 = mWifiList.get(i);
				for (int j = i+1; j < mWifiList.size(); j++) {
					sr2 = mWifiList.get(j);
					if(sr1.SSID.equals(sr2.SSID)){
						int wifi_de1 = networkManager.calSignalLevel(sr1.level, 4);
						int wifi_de2= networkManager.calSignalLevel(sr2.level, 4);
						if(wifi_de1 >= wifi_de2){
							mWifiList.remove(j);
						}else{
							mWifiList.remove(i);
							break;
						}
					}
				}
			}
		}
		
		private void topConnectedWifi(){//缃《宸茶繛鎺ョ殑wifi
			if(networkManager!=null){
				currentWifiInfo = networkManager.getConnectionInfo();
				if(currentWifiInfo!=null&&currentWifiInfo.getSSID()!=null){
					String ssid =  currentWifiInfo.getSSID();
					for (int i = 1; i < mWifiList.size(); i++) {
						ScanResult item = mWifiList.get(i);
						if(item!=null&&ssid!=null&&("\"" + item.SSID + "\"").equals(ssid)
								&&item.BSSID!=null&&currentWifiInfo!=null&&currentWifiInfo.getBSSID()!=null&&item.BSSID.equals(currentWifiInfo.getBSSID())){
							mWifiList.remove(i);
							mWifiList.add(0, item);
							notifyDataSetChanged();
							break;
						}
					}
				}
			}
		}
		
		
		public void isConnected(boolean b){
			mConnected = b;
			notifyDataSetChanged();
		}
		public void UpdateConnectAccesspoint(String ssid, boolean connected) {
			mConnected = connected;
			if(mConnected){
				myhandler.removeMessages(WIFI_CONNECTION_SUCCESS);
				myhandler.sendEmptyMessage(WIFI_CONNECTION_SUCCESS);
			}
			notifyDataSetChanged();
		}
		public void setCurItem(int position){
			this.selectItem = position;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return mWifiList.size();
		}
		
		public void setConnectingState(int position,String tip){
			Tip = tip;
			notifyDataSetChanged();
		}
		@Override
		public Object getItem(int position) {
			return mWifiList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		class ViewHolder
		{
			RelativeLayout rl;
			TextView wifiTitle;
			ImageView rightIcon;
			ImageView lockIcon ;
			ImageView wifiConnectedIcon0;
			TextView wifiConnectedText0;
			ImageView wifiSignal;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view  = convertView;
//			if(view == null){
				 holder = new ViewHolder();
				 view = LayoutInflater.from(mContext).inflate(R.layout.wifi_list_item, null);
				 holder. rl =  (RelativeLayout) view.findViewById(R.id.rl);
				 holder. wifiTitle = (TextView) view.findViewById(R.id.wifiTitle);
				 holder. rightIcon = (ImageView) view.findViewById(R.id.rightIcon);
				 holder. lockIcon = (ImageView) view.findViewById(R.id.lockIcon);
				 holder. wifiSignal = (ImageView) view.findViewById(R.id.wifiSignal);
				 holder. wifiConnectedIcon0 = (ImageView) view.findViewById(R.id.wifiConnectedIcon0);
				 holder. wifiConnectedText0 = (TextView) view.findViewById(R.id.wifiConnectedText0);
//				 view.setTag(holder);
//			}else {
//				 holder = (ViewHolder) view.getTag();
//				 
//			}
				 
			
			if(position==selectItem){
				view.setLayoutParams(new AbsListView.LayoutParams(DensityUtil.dip2px(mContext, 820),
				DensityUtil.dip2px(mContext, 60)));
				Animation ta = AnimationUtils.loadAnimation(mContext, R.anim.scale_wifi_item);
				ta.setFillAfter(true);
				holder. wifiTitle.startAnimation(ta);
				
				Animation ta1 = AnimationUtils.loadAnimation(mContext, R.anim.scale_wifi_item_center);
				ta1.setFillAfter(true);
				holder. lockIcon.startAnimation(ta1);
				holder. wifiSignal.startAnimation(ta1);
		
				holder. wifiTitle.setTextSize(33);  
				holder. wifiTitle.setAlpha(1.0f);
				holder. lockIcon.setAlpha(1.0f);
				holder. wifiSignal.setAlpha(1.0f);
				holder.wifiConnectedText0.setAlpha(1.0f);
				holder.wifiConnectedText0.setTextSize(21);
//				rightIconComm.setImageResource(R.drawable.icon_arrow_right);
			}else{
				view.setLayoutParams(new AbsListView.LayoutParams(DensityUtil.dip2px(mContext, 820),
						DensityUtil.dip2px(mContext, 60)));
				holder. wifiTitle.clearAnimation();
				holder. lockIcon.clearAnimation();
				holder. wifiSignal.clearAnimation();
				
				holder. wifiTitle.setTextSize(26);
				holder. wifiTitle.setAlpha(0.5f);
				holder. lockIcon.setAlpha(0.5f);
				holder. wifiSignal.setAlpha(0.5f);
				holder.wifiConnectedText0.setAlpha(0.5f);
				holder.wifiConnectedText0.setTextSize(20);
			}
			
			String tempStr = null;
			ScanResult result = mWifiList.get(position);
			if(result!=null){
			if(position==mWifiList.size()-1){
//				tempStr = result.SSID;
				holder. lockIcon.setImageDrawable(null);
				holder. wifiTitle.setText(getResources().getString(R.string.wirelessList_addNet_text));
				holder. wifiSignal.setImageDrawable(null);
//				rightIconComm.setImageDrawable(null);
//				holder. wifiConnectedIcon0.setVisibility(View.INVISIBLE);
				holder.wifiConnectedText0.setVisibility(View.GONE);
				
			}else if(result.SSID!=null&&!result.SSID.equals("")){
//System.out.println("-----------result.level----------"+result.level);
			int wifi_de = networkManager.calSignalLevel(result.level, 4);
			int secureType = networkManager.getSecurityType(result);
			if (secureType == networkManager.SECURITY_NONE) {
				holder. lockIcon.setImageDrawable(null);
				if (wifi_de == 1) {
					holder. wifiSignal.setImageResource(R.drawable.icon_wifi_1);
				} else if (wifi_de == 2) {
					holder. wifiSignal.setImageResource(R.drawable.icon_wifi_2);
				} else if (wifi_de == 3) {
					holder. wifiSignal.setImageResource(R.drawable.icon_wifi_3);
				} else {
					holder. wifiSignal.setImageResource(R.drawable.icon_wifi);
				}
			} else {
				holder. lockIcon.setImageResource(R.drawable.settings_page_three_lock);
				if (wifi_de == 1) {
					holder. wifiSignal.setImageResource(R.drawable.icon_wifi_1);
				} else if (wifi_de == 2) {
					holder. wifiSignal.setImageResource(R.drawable.icon_wifi_2);
				} else if (wifi_de == 3) {
					holder. wifiSignal.setImageResource(R.drawable.icon_wifi_3);
				} else {
					holder. wifiSignal.setImageResource(R.drawable.icon_wifi);
				}
			}
			currentWifiInfo = networkManager.getConnectionInfo();
			tempStr = result.SSID;
				holder. wifiTitle.setText(tempStr);
		
			
			if (result.BSSID!=null&&currentWifiInfo != null &&currentWifiInfo.getSSID()!=null
					&&currentWifiInfo.getBSSID()!=null&& ("\"" + result.SSID + "\"").equals(currentWifiInfo.getSSID())
					&&result.BSSID.equals(currentWifiInfo.getBSSID())&& ValidUtils.checkWifi(mContext)) {
					
				if(mConnected){
//					wifiConnectedIconComm.setVisibility(View.VISIBLE);
					holder.wifiConnectedText0.setVisibility(View.VISIBLE);
					holder. lockIcon.setImageResource(R.drawable.lock_unlock);
				}else {
//					wifiConnectedIconComm.setVisibility(View.INVISIBLE);
					holder.wifiConnectedText0.setVisibility(View.GONE);
					holder. lockIcon.setImageResource(R.drawable.settings_page_three_lock);
				}
			}else {
//				wifiConnectedIconComm.setVisibility(View.INVISIBLE);
				holder.wifiConnectedText0.setVisibility(View.GONE);
				holder. lockIcon.setImageResource(R.drawable.settings_page_three_lock);
			}
				
			}
			topConnectedWifi();
			}
			return view;
		}
	}

	@Override
	public boolean onKeyDown(int arg0, KeyEvent arg1) {
		// TODO Auto-generated method stub
		
		if(arg0==KeyEvent.KEYCODE_BACK&&arg1.getAction()==KeyEvent.ACTION_DOWN){
			if(enterPassword.getVisibility()==View.VISIBLE){
				hideView();
				wifiListView.setVisibility(View.VISIBLE);
				myhandler.sendEmptyMessage(111);
				return true;
			}else if(network_detial_ll.getVisibility()==View.VISIBLE){
				hideView();
				wifiListView.setVisibility(View.VISIBLE);
				return true;
			}else if(wifiListView.getVisibility()==View.VISIBLE){
				if(type.equals("boot")){
				startDetectNetwork();
				}
			}
			isUpdate = false;
		}
		return super.onKeyDown(arg0, arg1);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.network_detial_update:
			if(!isUpdate){
				edit_ip.setFocusable(true);
				edit_yanma.setFocusable(true);
				edit_wangguan.setFocusable(true);
				edit_dns.setFocusable(true);
				edit_ip.requestFocus();
				isUpdate = true;
			}
			break;
		case R.id.edit_ip:
			if(network_detial_ll.getVisibility()==View.VISIBLE){
				imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(edit_ip, 0);
				}
			break;
		case R.id.edit_yanma:
			if(network_detial_ll.getVisibility()==View.VISIBLE){
				imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(edit_yanma, 0);
				}
			break;
		case R.id.edit_wangguan:
			if(network_detial_ll.getVisibility()==View.VISIBLE){
				imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(edit_wangguan, 0);
				}
			break;
		case R.id.edit_dns:
			if(network_detial_ll.getVisibility()==View.VISIBLE){
				imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(edit_dns, 0);
				}
			break;
		case R.id.network_detial_submit:
			// TODO:network detecting
			 if(network_detial_ll.getVisibility()==View.VISIBLE){
				 startNetworkDetecting();
				}
			
			break;
		case R.id.network_detial_ignore:			
			 if(network_detial_ll.getVisibility()==View.VISIBLE){
				hideView();
				currentWifiInfo = networkManager.getConnectionInfo();
				for (int i = 0; i < ManuallyScanResultList.size(); i++) {
					ScanResult item = ManuallyScanResultList.get(i);
					if (currentWifiInfo != null && currentWifiInfo.getSSID()!=null&&("\""+item.SSID+"\"").equals(currentWifiInfo.getSSID())&&ValidUtils.checkWifi(mContext)) {
						ManuallyScanResultList.remove(i);
						mAdapter.delManuallyWifi(item.SSID,item.BSSID);
					}
				}
				networkManager.ignore(curPosition);
				wifiListView.setVisibility(View.VISIBLE);
				initData();
				//app.setNetworkInfo(networkInfo);
			}
			break;
			
		case R.id.EditPsd:
			if(EditPsd.getText().length()<8) {
				imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(EditPsd, 0);
			}else {
				networkManager.connectingWifi(EditPsd.getText().toString());
				enterPasswordFinish();	
			}
		  break;
		default:
			break;
		}
	}

	private void enterPasswordFinish(){ //锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷敕拷潜叩锟斤拷锟�?
		hideView();
		wifiListView.setVisibility(View.VISIBLE);
	}
	private void startDetectNetwork(){
		/*
		Intent i = new Intent();
		i.setClass(mContext, DetectNetworkActivity.class);
		startActivity(i);
		
		WirelessActivity.this.finish();
		*/
	}
	
	private void initOnSelect(){
		ip_ll.setOnFocusChangeListener(new FocusChange(edit_ip));
		yanma_ll.setOnFocusChangeListener(new FocusChange(edit_yanma));
		wangguan_ll.setOnFocusChangeListener(new FocusChange(edit_wangguan));
		dns_ll.setOnFocusChangeListener(new FocusChange(edit_dns));
	}
	
	class FocusChange implements OnFocusChangeListener{
		private EditText et;
		
		public FocusChange(EditText et){
			this.et = et;
		}
		@Override
		public void onFocusChange(View arg0, boolean b) {
			// TODO Auto-generated method stub
			if(b){
				et.setTextColor(Color.parseColor(LBULE));
				et.setTextSize(BIGGER_FRONT_SIZE);
			}else{
				et.setTextColor(Color.parseColor(WHITE));
				et.setTextSize(NORMAL_FRONT_SIZE);
			}
		}
		
	}
	/**
	 * 
	 * @锟斤拷锟斤拷:锟斤拷锟教回筹拷锟斤拷锟斤拷锟斤拷应
	 * @锟斤拷锟斤拷锟斤�? initOnKeyBoard
	 * @锟斤拷锟斤拷锟斤拷锟斤拷 void
	 * @锟斤拷锟斤拷锟斤�?huang
	 * @锟斤拷锟斤拷时锟斤拷 Aug 20, 20143:49:40 PM	
	 * @锟睫革拷锟斤�?huang
	 * @锟睫革拷时锟斤拷 Aug 20, 20143:49:40 PM	
	 * @锟睫改憋拷注 
	 * @since
	 * @throws
	 */
	private void initOnKeyBoard(){
		edit_ip.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if(actionId==EditorInfo.IME_ACTION_GO){
					edit_ip.setFocusable(false);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(
							edit_ip.getWindowToken(), 0);
				}
				return false;
			}
			
		});
		 edit_yanma.setOnEditorActionListener(new OnEditorActionListener(){
	
				@Override
				public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
					// TODO Auto-generated method stub
					if(actionId==EditorInfo.IME_ACTION_GO){
						//edit_ip.setFocusable(false);
						//handle_saveconf();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(
								edit_yanma.getWindowToken(), 0);
					}
					return false;
				}
				
			});
		 edit_wangguan.setOnEditorActionListener(new OnEditorActionListener(){
	
				@Override
				public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
					// TODO Auto-generated method stub
					if(actionId==EditorInfo.IME_ACTION_GO){
						//edit_ip.setFocusable(false);
						//handle_saveconf();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(
								edit_wangguan.getWindowToken(), 0);
					}
					return false;
				}
				
			});
		 edit_dns.setOnEditorActionListener(new OnEditorActionListener(){
	
				@Override
				public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
					// TODO Auto-generated method stub
					if(actionId==EditorInfo.IME_ACTION_DONE){
						//edit_ip.setFocusable(false);
						//handle_saveconf();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(
								edit_dns.getWindowToken(), 0);
					}
					return false;
				}
				
			});
		
		//锟斤拷锟斤拷锟斤拷锟斤拷嗉拷募锟斤拷锟斤拷录锟�?
		EditPsd.setOnEditorActionListener(new OnEditorActionListener() {//锟斤拷锟斤拷锟斤拷锟斤拷
			
			@Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if(actionId==EditorInfo.IME_ACTION_DONE){
					//锟斤拷锟斤拷wifi
					
					if(EditPsd.getText().length()>=8&&EditPsd.getText().length()<=63){
						networkManager.connectingWifi(EditPsd.getText().toString());
						enterPasswordFinish();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(
								EditPsd.getWindowToken(), 0);
					}else{
						Toast.makeText(mContext, mContext.getString(R.string.password_tips), Toast.LENGTH_LONG).show();
					}
				}
				
				return false;
			}
		});
	}
	private void startNetworkDetecting(){
		Intent i = new Intent();
		i.setClass(mContext, NetworkSetting_Detecting.class);
		startActivity(i);
	}
	private void saveNetwork(){
		networkInfo.clear();
		map = new HashMap<String, String>();
		map.put("netName", getResources().getString(R.string.detect_network_detectedNetworkTip_wireless));
		if(networkManager!=null){
			netInfor = networkManager.getNetworkDetailInfo();
			map.put("netState", netInfor.getSsid());
			map.put("hasConnected", "true");
		}
		networkInfo.add(map);
		//app.setNetworkInfo(networkInfo);
	}
	
	private void initData(){
		networkInfo.clear();
		map = new HashMap<String, String>();
		map.put("netName", getResources().getString(R.string.detect_network_detectedNetworkTip_wireless));
		map.put("netState", getResources().getString(R.string.detect_network_wirelessTip_disconnected));
		map.put("hasConnected", "false");
		networkInfo.add(map);
	}
	private void initFocus(){
		update.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					update.setBackgroundResource(R.drawable.btn_focus_bg);
					update.setTextColor(Color.parseColor("#FFFFFF"));
				}else{
					update.setBackgroundResource(0);
					update.setTextColor(Color.parseColor("#4c5c6a"));
				}
			}
			
		});
    	submit.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					submit.setBackgroundResource(R.drawable.btn_focus_bg);
					submit.setTextColor(Color.parseColor("#FFFFFF"));
				}else{
					submit.setBackgroundResource(0);
					submit.setTextColor(Color.parseColor("#4c5c6a"));
				}
			}
		});
    	ignore.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					ignore.setBackgroundResource(R.drawable.btn_focus_bg);
					ignore.setTextColor(Color.parseColor("#FFFFFF"));
				}else{
					ignore.setBackgroundResource(0);
					ignore.setTextColor(Color.parseColor("#4c5c6a"));
				}
			}
		});
    }
	private void setFont(EditText v){
		//v.setTypeface(face);
		v.getPaint().setFakeBoldText(true);
	}
	
	private boolean isIpAddress(String value){
		 int start = 0;
		 int end = value.indexOf('.');
		 int numBlocks = 0;
		 while(start < value.length()){
			 if(end == -1){
				 end = value.length();
			 }
			 try {
	                int block = Integer.parseInt(value.substring(start, end));
	                if ((block > 255) || (block < 0)) {
	                        return false;
	                }
			 } catch (NumberFormatException e) {
	                    return false;
	         }
			 numBlocks++;
			 start = end + 1;
	         end = value.indexOf('.', start);
		 }
		 return numBlocks == 4;
	 }
}
