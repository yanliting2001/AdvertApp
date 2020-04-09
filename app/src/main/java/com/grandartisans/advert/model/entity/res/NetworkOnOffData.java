package com.grandartisans.advert.model.entity.res;

/**
 * Created by Administrator on 2020/3/11.
 */

public class NetworkOnOffData {
    Long networkStartTime;
    Long networkEndTime;

    public Long getNetworkStartTime() {
        return networkStartTime;
    }

    public void setNetworkStartTime(Long networkStartTime) {
        this.networkStartTime = networkStartTime;
    }

    public Long getNetworkEndTime() {
        return networkEndTime;
    }

    public void setNetworkEndTime(Long networkEndTime) {
        this.networkEndTime = networkEndTime;
    }
}
