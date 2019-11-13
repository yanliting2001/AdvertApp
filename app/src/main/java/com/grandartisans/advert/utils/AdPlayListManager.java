package com.grandartisans.advert.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.grandartisans.advert.app.AdvertApp;
import com.grandartisans.advert.interfaces.AdListEventListener;
import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.grandartisans.advert.model.entity.res.AdvertData;
import com.grandartisans.advert.model.entity.res.AdvertInfoData;
import com.grandartisans.advert.model.entity.res.AdvertPosition;
import com.grandartisans.advert.model.entity.res.AdvertSchedule;
import com.grandartisans.advert.model.entity.res.AdvertWeatherData;
import com.grandartisans.advert.model.entity.res.PositionVer;
import com.grandartisans.advert.model.entity.res.TemplateReginVo;
import com.grandartisans.advert.model.entity.res.TemplateVo;
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
    private List<AdvertData> mAdvertList = null;
    private Map<Long, Integer> mAdvertIndex = new HashMap<>();
    private List<PlayingAdvert> adurls = new ArrayList<PlayingAdvert>();
    private List<PlayingAdvert> adurls_local = new ArrayList<PlayingAdvert>();
    private AdListEventListener mAdListEventListener = null;
    private AdvertInfoData mInfoData = null;
    private AdvertWeatherData mWeatherData = null;
    ReentrantLock lock = new ReentrantLock();
    private int mScheduleIndex = -1;
    private int mTemplateIndex = 0;
    private AdPlayListManager (Context context) {}
    private void initPlayerIndex(){
        mAdvertIndex.put(0L, 0);
        if(mAdvertList!=null) {
            for(int i=0;i<mAdvertList.size();i++) {
                AdvertData advertData = mAdvertList.get(i);
                Map<Long, List<PlayingAdvert>> advertMap = advertData.getAdvertMap();
                for (Map.Entry<Long, List<PlayingAdvert>> entry : advertMap.entrySet()) {
                    Long id = entry.getKey();
                    mAdvertIndex.put(id, 0);
                }
            }
        }
    }
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

        String jsondata = DevRing.cacheManager().diskCache("advertList").getString("playList");
        Gson gson = new Gson();
        if (jsondata!=null) {
            mAdvertList = new ArrayList<>();
            mAdvertList = gson.fromJson(jsondata, new TypeToken<List<AdvertData>>() {}.getType());
        }else {
            mAdvertList = null;
        }
        initPlayerIndex();
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
    public boolean updatePlayList( List<AdvertData> adList){
        boolean res = true;
        lock.lock();
        mAdvertList = adList;
        lock.unlock();
        return res;
    }

    public List<TemplateReginVo> getTemplateList(){
        if(mAdvertList!=null) {
            for(int i=0;i<mAdvertList.size();i++) {
                AdvertData advertData = mAdvertList.get(i);
                AdvertSchedule advertSchedule = advertData.getAdvertSchedule();
                if(advertSchedule.getStartTime()!=null && advertSchedule.getEndTime()!=null){
                    if (CommonUtil.compareDateState(advertSchedule.getStartTime(), advertSchedule.getEndTime())) {
                        if(mScheduleIndex!=i) {
                            mScheduleIndex = i;
                            mTemplateIndex=0;
                        }
                        initPlayerIndex();
                        Log.i("getTemplateList","mScheduleIndex = " + mScheduleIndex + "mTemplateIndex = " + mTemplateIndex);
                        return advertData.getTemplateVo().get(mTemplateIndex).getRegionList();
                    }
                }
            }
        }
        return null;
    }
    public boolean needChangeTemplate(){
        if(mAdvertList!=null) {
            if(mScheduleIndex!=-1 && mScheduleIndex < mAdvertList.size()){
                AdvertData advertData = mAdvertList.get(mScheduleIndex);
                List<TemplateVo> templateVo = advertData.getTemplateVo();
                if(templateVo.size()>1){
                    Log.i(TAG,"need change template mTemplateIndex = "  + mTemplateIndex);
                    mTemplateIndex+=1;
                    Log.i(TAG,"need change template mTemplateIndex = "  + mTemplateIndex + "template list size = " + templateVo.size());
                    mTemplateIndex = mTemplateIndex%templateVo.size();
                    Log.i(TAG,"need change template mTemplateIndex = "  + mTemplateIndex);
                    return true;
                }else{
                    return false;
                }
                //advertData.getTemplateVo().get(0).getRegionList();
            }
        }
        return false;
    }
    public  PlayingAdvert  getValidPlayUrl(Long positionId,boolean checkTemplate) {
        String url=null;
        boolean urlvalid = false;
        lock.lock();
        int playindex = 0;
        PlayingAdvert playAdvertItem = null;
        if(mAdvertList!=null && mAdvertList.size()>0 && mScheduleIndex>=0) {
            AdvertData advertData = mAdvertList.get(mScheduleIndex);
            Map<Long,List<PlayingAdvert>> advertMap = advertData.getAdvertMap();
            /*
            for (Map.Entry<Long, List<PlayingAdvert>> entry : advertMap.entrySet()) {
                Long id = entry.getKey();
                Log.i(TAG, "getValidPlayUrl mAdvertMap id = " + id);
            }
            */
            adurls = advertMap.get(positionId);
            if (mAdvertIndex.containsKey(positionId))
                playindex = mAdvertIndex.get(positionId);
            if(checkTemplate) {
                if (playindex >= adurls.size()) {
                    if (needChangeTemplate()) {
                        if (mAdListEventListener != null) mAdListEventListener.onTemplateUpdate();
                        return null;
                    }
                }
            }
            Log.i(TAG, "getValidPlayUrl  positionId = " + positionId + "playindex = " + playindex);
            if (adurls != null && adurls.size() > 0) {
                //Log.i(TAG,"adurls size  = " + adurls.size() + "playindex = " + playindex);
                //Log.i(TAG,"adurls_local size  = " + adurls_local.size() + "playindex = " + playindex);
                playAdvertItem = findPlayUrl(positionId, playindex);
                if (playAdvertItem != null) {
                    urlvalid = true;
                    int index = playindex % adurls.size();
                    //url = adurls.get(index).getPath();
                    AdvertApp.setPlayingAdvert(playAdvertItem);
                    //PlayRecord record = new PlayRecord();
                } else {
                    urlvalid = false;
                }
            }

            lock.unlock();
        }else {
            if (urlvalid == false && adurls_local.size() > 0) {
                playindex = mAdvertIndex.get(positionId);
                Log.i(TAG, "getValidPlayUrl  positionId = " + positionId + "playindex = " + playindex);
                if(playindex>=adurls_local.size()){
                    if (checkTemplate) {
                        mAdvertIndex.put(positionId, 0);
                        if (mAdListEventListener != null) mAdListEventListener.onTemplateUpdate();
                        return null;
                    }
                }
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
                mAdvertIndex.put(positionId, ++playindex);
            }
        }
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

    public void saveAdvertVersion(List<PositionVer> versionList) {
        if(mAdvertList!=null) {
            Gson gson = new Gson();
            String str = gson.toJson(mAdvertList);
            //Log.i(TAG, "save advertMap = " + str);
            DevRing.cacheManager().diskCache("advertList").put("playList", str);
            for (int i = 0; i < versionList.size(); i++) {
                PositionVer advertPosition = versionList.get(i);
                AdvertVersion.setAdVersion(advertPosition.getAdvertPositionId().intValue(), advertPosition.getVersion());
            }
            if (mAdListEventListener != null) {
                mAdListEventListener.onAdListUpdate();
            }
        }
    }
    public PlayingAdvert getPlayingAd(Long posid){
        int playindex = 0;
        if(mAdvertList!=null && mAdvertList.size()>0 && mScheduleIndex>=0) {
            AdvertData advertData = mAdvertList.get(mScheduleIndex);
            Map<Long, List<PlayingAdvert>> advertMap = advertData.getAdvertMap();
            List<PlayingAdvert> adList = advertMap.get(posid);
            if (mAdvertIndex.containsKey(posid))
                playindex = mAdvertIndex.get(posid);
            if (playindex > 0) playindex--;
            if (adList != null && adList.size() > 0) {
                int index = playindex % adList.size();
                PlayingAdvert item = adList.get(index);
                return item;
            }
        }
        return null;
    }
    public void setPlayListUpdate(String isUpdated){
        DevRing.cacheManager().diskCache("advertList").put("isUpdated",isUpdated);
    }
    public boolean isAdListUpdated(){
        boolean res = false;
        String isUpdated = DevRing.cacheManager().diskCache("advertList").getString("isUpdated","0");
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
