package com.grandartisans.advert.interfaces;

public interface RecorderEventListener {
    public void onRecordFinished(String path,int cameraId);
    public void onRecordStart();
}
