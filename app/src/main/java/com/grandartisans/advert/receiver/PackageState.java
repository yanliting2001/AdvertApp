package com.grandartisans.advert.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class PackageState{
	private BroadcastReceiver mPackageStateReceiver = null;
	private PackageStateChangeListener mChangeListener=null;
	private Context mcontext;
	

	public void  init(Context pcontext){
		this.mcontext=pcontext;
		registerPackageListener();
	}
	
	public void registerPackageListener() {
		if(mPackageStateReceiver==null){
			mPackageStateReceiver = new BroadcastReceiver(){
				@Override
				public void onReceive(Context context, Intent intent) {
					String pack = intent.getDataString().substring(8);
					if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
						if(mChangeListener!=null) mChangeListener.onRemove(pack);
					}else if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
						if(mChangeListener!=null) mChangeListener.onInstall(pack);
					}else if(intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){
						if(mChangeListener!=null) mChangeListener.onInstall(pack);
					}
				}
			};
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			iFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
			iFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
			iFilter.addDataScheme("package");
			mcontext.registerReceiver(mPackageStateReceiver, iFilter);
		}
	}
	
	public void unregisterPackageListener(Context pcontext) {
		
		if (mPackageStateReceiver != null) {
			mcontext.unregisterReceiver(mPackageStateReceiver);
		}		
		
	}
	
	public void setPackageStateChangeListener(PackageStateChangeListener l) {
		mChangeListener = l;
	}
	
	public interface PackageStateChangeListener {
		 public void onInstall(String packageName);
		 public void onRemove(String packageName);
	}
}
