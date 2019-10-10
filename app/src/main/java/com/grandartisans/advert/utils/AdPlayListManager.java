package com.grandartisans.advert.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.grandartisans.advert.app.AdvertApp;
import com.grandartisans.advert.interfaces.AdListEventListener;
import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.grandartisans.advert.model.entity.res.AdvertInfoData;
import com.grandartisans.advert.model.entity.res.AdvertPosition;
import com.grandartisans.advert.model.entity.res.AdvertWeatherData;
import com.ljy.devring.DevRing;
import com.ljy.devring.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class AdPlayListManager {
    private final String TAG = "AdPlayListManager";
    private volatile static AdPlayListManager mPlayListInstance = null;
    private Map<Long, List<PlayingAdvert>> mAdvertMap = new HashMap<>();
    private Map<Long, Integer> mAdvertIndex = new HashMap<>();
    private List<PlayingAdvert> adurls = new ArrayList<PlayingAdvert>();
    private List<PlayingAdvert> adurls_local = new ArrayList<PlayingAdvert>();
    private AdListEventListener mAdListEventListener = null;
    private AdvertInfoData mInfoData = null;
    private AdvertWeatherData mWeatherData = null;
    ReentrantLock lock = new ReentrantLock();
    private AdPlayListManager (Context context) {}
    public static AdPlayListManager getInstance(Context context) {
        if (mPlayListInstance == null) {
            synchronized (AdPlayListManager.class) {
                if (mPlayListInstance == null) {
                    mPlayListInstance = new AdPlayListManager(context);
                }
            }
        }
        return mPlayListInstance;
    }
    public boolean init(Context context){
        boolean res = false;
        String jsondata = DevRing.cacheManager().diskCache("advertMap").getString("playMap");
        Gson gson = new Gson();
        if (jsondata!=null) {
            mAdvertMap = gson.fromJson(jsondata, new TypeToken<Map<Long,List<PlayingAdvert>>>() {}.getType());
            for (Map.Entry<Long, List<PlayingAdvert>> entry : mAdvertMap.entrySet()) {
                Long id = entry.getKey();
                mAdvertIndex.put(id,0);
            }
        }
        String destPath = FileUtil.getExternalCacheDir(context);
        File file = new File(destPath + "/default");
        if(!file.exists()){
            FileOperator.copyFileFromAsserts(context,"default.zip","default.zip",destPath);
            File zipfile = new File(destPath+"/default.zip");
            try {
                ZipUtils.upZipFile(zipfile,destPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        PlayingAdvert item = new PlayingAdvert();
        String url = "http://127.0.0.1:8769/"+destPath + "/default/playlist.m3u8";
        item.setPath(url);
        adurls_local.add(item);
        return false;
    }
    public void  registerListener(AdListEventListener listener){
        if(listener!=null) mAdListEventListener = listener;
    }
    public boolean updatePlayList( Map<Long, List<PlayingAdvert>> adMap){
        boolean res = true;
        lock.lock();
        mAdvertMap = adMap;
        /*
        adurls.clear();
        for(int i=0;i<urls.size();i++){
            adurls.add(urls.get(i));
        }
        */
        lock.unlock();
        return res;
    }

    public  PlayingAdvert  getValidPlayUrl(Long positionId) {
        String url=null;
        lock.lock();
        int playindex = 0;

        PlayingAdvert playAdvertItem = null;
        for (Map.Entry<Long, List<PlayingAdvert>> entry : mAdvertMap.entrySet()) {
            Long id = entry.getKey();
            Log.i(TAG,"getValidPlayUrl mAdvertMap id = " + id);
        }
        adurls = mAdvertMap.get(positionId);
        if(mAdvertIndex.containsKey(positionId))
            playindex = mAdvertIndex.get(positionId);
        Log.i(TAG,"getValidPlayUrl  positionId = " + positionId + "playindex = " + playindex);
        boolean urlvalid = false;
        if(adurls!=null && adurls.size()>0) {
            //Log.i(TAG,"adurls size  = " + adurls.size() + "playindex = " + playindex);
            //Log.i(TAG,"adurls_local size  = " + adurls_local.size() + "playindex = " + playindex);
            playAdvertItem  = findPlayUrl(positionId,playindex);
            if(playAdvertItem!=null) {
                urlvalid = true;
                int index = playindex % adurls.size();
                //url = adurls.get(index).getPath();
                AdvertApp.setPlayingAdvert(playAdvertItem);
                //PlayRecord record = new PlayRecord();
            }else{
                urlvalid = false;
            }

        }
        if(urlvalid == false && adurls_local.size()>0) {
            int index = playindex % adurls_local.size();
            url = adurls_local.get(index).getPath();
            playAdvertItem = new PlayingAdvert();
            Long id = Long.valueOf(0);
            playAdvertItem.setAdPositionID(id);
            playAdvertItem.setAdvertid(id);
            playAdvertItem.setPath(url);
            playAdvertItem.setvType(2);
            playAdvertItem.setDuration(15);
            AdvertApp.setPlayingAdvert(playAdvertItem);
            mAdvertIndex.put(positionId,++playindex);
        }
        lock.unlock();
        return playAdvertItem;
    }
    private PlayingAdvert findPlayUrl(Long positionId,int playindex){
        String url="";
        PlayingAdvert playAdvertItem=null;
        int size = adurls.size();
        for(int i=0;i<size;i++) {
            int index = playindex % adurls.size();

            playAdvertItem  = adurls.get(index);
            Log.i(TAG,"play advertitem "+ playAdvertItem.getPath() + "playindex = " +  playindex + "index = " + index + "path = " + playAdvertItem.getPath());
            Log.i(TAG,"play advertitem   = " +  playAdvertItem.getStartDate() + " " + playAdvertItem.getStartTime()+playAdvertItem.getEndDate() + " " + playAdvertItem.getEndTime());
            if(playAdvertItem.getPath()!=null && !playAdvertItem.getPath().isEmpty()) {
                if(playAdvertItem.getStartDate()!=null && !playAdvertItem.getStartDate().isEmpty()) {
                    if (CommonUtil.compareTimeState(playAdvertItem.getStartDate(),playAdvertItem.getStartTime(), playAdvertItem.getEndDate(),playAdvertItem.getEndTime())) {
                        url = playAdvertItem.getPath();
                        break;
                    } else if (CommonUtil.compareTimeState("2015-01-01", "00:00:00", "2016-12-30","23:59:59")) {
                        url = playAdvertItem.getPath();
                        break;
                    } else {
                        playindex++;
                    }
                }else{
                    url = playAdvertItem.getPath();
                    //return playAdvertItem;
                    break;
                }
            }else{
                playindex++;
            }
        }
        if(url!=null && !url.isEmpty()) {
            mAdvertIndex.put(positionId,++playindex);
            return playAdvertItem;
        }else return null;
    }

    public void saveAdvertVersion(List<AdvertPosition> advertPositions) {
        Gson gson = new Gson();
        String str = gson.toJson(mAdvertMap);
        //Log.i(TAG, "save advertMap = " + str);
        DevRing.cacheManager().diskCache("advertMap").put("playMap", str);
        for(int i=0;i<advertPositions.size();i++) {
            AdvertPosition advertPosition = advertPositions.get(i);
            AdvertVersion.setAdVersion(advertPosition.getId().intValue(), advertPosition.getVersion());
        }
        if(mAdListEventListener!=null) {
            mAdListEventListener.onAdListUpdate();
        }
    }
    public PlayingAdvert getPlayingAd(Long posid){
        int playindex = 0;
        List<PlayingAdvert> adList  = mAdvertMap.get(posid);
        if(mAdvertIndex.containsKey(posid))
            playindex = mAdvertIndex.get(posid);
        if(playindex>0) playindex--;
        if(adList!=null && adList.size()>0) {
            int index = playindex % adList.size();
            PlayingAdvert item = adList.get(index);
            return item;
        }
        return null;
    }
    public void setPlayListUpdate(String isUpdated){
        DevRing.cacheManager().diskCache("advertMap").put("isUpdated",isUpdated);
    }
    public boolean isAdListUpdated(){
        boolean res = false;
        String isUpdated = DevRing.cacheManager().diskCache("advertMap").getString("isUpdated","0");
        if(isUpdated!=null && Integer.valueOf(isUpdated)==1) res = true;
        return res;
    }
    public void updateInfo(AdvertInfoData data){
        mInfoData = data;
        if(mAdListEventListener!=null) mAdListEventListener.onInfoUpdate(data);
    }
    public AdvertInfoData getAdvertInfo(){
        return mInfoData;
    }
    public void updateWeatherInfo(AdvertWeatherData data){
        mWeatherData = data;
        if(mAdListEventListener!=null) mAdListEventListener.onWeatherUpdate(data);
    }
    public AdvertWeatherData getWeatherInfo(){
        return mWeatherData;
    }
    public void setOnRecorderStart(){
        setPlayListUpdate("1");
        if(mAdListEventListener!=null) mAdListEventListener.onRecoderStart();
    }
    public void printInfo(){
        if(mAdListEventListener!=null) mAdListEventListener.onPrintInfo();
    }
    public boolean isPlayerActivit(){
        if(mAdListEventListener!=null) return true;
        else return false;
    }
}
