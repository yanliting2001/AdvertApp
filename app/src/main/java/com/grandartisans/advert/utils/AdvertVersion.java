package com.grandartisans.advert.utils;

import com.grandartisans.advert.model.entity.PlayingAdvert;
import com.ljy.devring.DevRing;
import com.ljy.devring.other.RingLog;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AdvertVersion {
    public static int  getAdVersion(long id) {
        int version = -1;
        version = DevRing.cacheManager().spCache("advertVersionM").getInt(String.valueOf(id),-1);
        /*
        if(positionId == id ) {
            version = DevRing.cacheManager().spCache("advertVersion").getInt("version",-1);
        }
        */
        return version;
    }

    public static void  setAdVersion(int id,int version) {
        DevRing.cacheManager().spCache("advertVersionM").put(String.valueOf(id),version);
        //DevRing.cacheManager().spCache("advertVersion").put("version",version);
    }
    public static void resetAdVersion(){
        Map<String,Integer> advertVersionMap  = ( Map<String,Integer>)DevRing.cacheManager().spCache("advertVersionM").getAll();
        int num=0;
        Iterator<Map.Entry<String,Integer>> it = advertVersionMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,Integer> entry=it.next();
            String  key=entry.getKey();
            int value = entry.getValue();
            it.remove();        //OK
            DevRing.cacheManager().spCache("advertVersionM").remove(key);
            RingLog.d("remove save list = " +  num + "id=" + key +  "version=" + value);
            num ++ ;
        }
    }
    public static int getVersiolCount(){
        Map<String,Integer> advertVersionMap  = ( Map<String,Integer>)DevRing.cacheManager().spCache("advertVersionM").getAll();
        int count=0;
        for (Iterator<Map.Entry<String,Integer>> it = advertVersionMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String,Integer> item = it.next();//此句不能少，否则，删除当前节点，指针无法找到下一节点
            count++;
        }
        return count;
    }
    /*
    public static int  getAdPositionId() {
        int positionId = DevRing.cacheManager().spCache("advertVersion").getInt("advertPositionId",-1);
        return positionId;
    }
    */
}
