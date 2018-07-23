package com.grandartisans.advert.model.entity.res;

import java.util.ArrayList;
import java.util.List;

public class HeartBeatResult {
    private boolean success;
    private int status;
    private String msg;
    private String time;
    private List<HeartBeatData> data  = new ArrayList<>();

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<HeartBeatData> getData() {
        return data;
    }

    public void setData(List<HeartBeatData> data) {
        this.data = data;
    }
}
