package com.grandartisans.advert.app.constant;

/**
 * author:  ljy
 * date:    2017/9/25
 * description: 网络地址常量类
 */

public interface UrlConstants {

    //String BASE_URL = "https://api.douban.com/";
    //String BASE_URL = "https://api.dsp.grandartisans.cn/v1/";
    String BASE_URL = "https://api.dsp.grandartisans.cn/";
    //String BASE_URL = "http://api.lycoou.com//v1/";
    //String BASE_URL = "http://192.1.1.33:8081/v1/";
    String GET_PLAYING_MOVIE = "v2/movie/in_theaters";
    String GET_COMMING_MOVIE = "v2/movie/coming_soon";

    String GET_ADVERT_TOKEN = "v1/api/token";
    String GET_APP_UPGRADE = "v1/api/version/app/check";
    String GET_SYSTEM_UPGRADE = "v1/api/version/system/check";
    String GET_ADVERT_LIST = "v1/api/schedule/timesV2";
    String GET_ADVERT_INFO = "v1/api/system/Info";
    String GET_ADVERT_WEATHER = "v1/api/system/weather";
    String  SEND_HEARTBEAT = "v1/api/heartbeat/advertV1";
    String REPORT_INFO = "v1/api/heartbeat/info";
    String REPORT_EVENT = "v1/api/event/export";
    String ACTIVE_CHECK = "v1/api/terminal/check/new";
    String GET_SYSTEM_UPGRADE_OLD = "PhpApi/Public/go3c?m=Upgrade.getUpgradeInfo";
}
