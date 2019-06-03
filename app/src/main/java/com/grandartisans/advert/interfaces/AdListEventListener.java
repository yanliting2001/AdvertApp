package com.grandartisans.advert.interfaces;

import com.grandartisans.advert.model.entity.res.AdvertInfoData;
import com.grandartisans.advert.model.entity.res.AdvertWeatherData;

public interface AdListEventListener {
    public void onAdListUpdate();
    public void onInfoUpdate(AdvertInfoData data);
    public void onWeatherUpdate(AdvertWeatherData data);
}
