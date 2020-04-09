package com.grandartisans.advert.interfaces;

import android.net.wifi.ScanResult;

import java.util.List;

public interface NetworkEventListener {
    public void onShowWifiList(List<ScanResult> list);
    public void onEthernetConnected();
    public void onEthernetDisconnect();
    public void onWifiConnected();
    public void onWifiConnecteFailed();
    public void onWifiConnecting();
    public void onWifiListRefresh();
    public void onNoNetwork();
    public void showPassword(ScanResult result);

}
