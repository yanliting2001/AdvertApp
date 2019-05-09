package com.grandartisans.advert.model.entity.post;

public class RecorderUpdateEventData {
    long templateid;
    String monitorPath;

    public long getTemplateid() {
        return templateid;
    }

    public void setTemplateid(long templateid) {
        this.templateid = templateid;
    }

    public String getPath() {
        return monitorPath;
    }

    public void setPath(String path) {
        this.monitorPath = path;
    }
}
