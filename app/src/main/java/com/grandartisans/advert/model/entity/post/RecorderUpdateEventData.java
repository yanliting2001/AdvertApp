package com.grandartisans.advert.model.entity.post;

public class RecorderUpdateEventData {
    long templateid;
    String monitorPath;
    int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

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
