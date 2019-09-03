package com.grandartisans.advert.model.entity.res;

import java.util.List;

public class AdvertData {
    AdvertSchedule advertSchedule;
    private List<AdvertFile> fileList;

    public AdvertSchedule getAdvertSchedule() {
        return advertSchedule;
    }

    public void setAdvertSchedule(AdvertSchedule advertSchedule) {
        this.advertSchedule = advertSchedule;
    }

    public List<AdvertFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<AdvertFile> fileList) {
        this.fileList = fileList;
    }
}
