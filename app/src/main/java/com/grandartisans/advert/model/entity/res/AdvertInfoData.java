package com.grandartisans.advert.model.entity.res;

public class AdvertInfoData<T> {
    private Long id;
    private Long iorder;
    private Long status;
    private String startTime;
    private String endTime;

    /**
     * 所属ID
     */
    private Long groupId;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型 1小区 2模板
     */
    private Long type;

    /**
     * 时间|0否1是
     */
    private Long vTime;

    /**
     * 天气|0否1是
     */
    private Long weather;

    /**
     * 背景色
     */
    private String backColor;

    /**
     * 字体颜色
     */
    private String fontColor;

    /**
     * 字体大小
     */
    private Long fontSize;

    /**
     * 滚动速度
     */
    private Long velocity;

    /**
     * 文字信息
     */
    private String writing;
    /*紧急通知类型1：文字；2：图片*/
    private Long urgenttype;
    /*紧急通知显示时长*/
    private Long urgenttime;
    /*紧急通知显示内容*/
    private String urgentwriting;

    public Long getUrgenttype() {
        return urgenttype;
    }

    public void setUrgenttype(Long urgenttype) {
        this.urgenttype = urgenttype;
    }

    public Long getUrgenttime() {
        return urgenttime;
    }

    public void setUrgenttime(Long urgenttime) {
        this.urgenttime = urgenttime;
    }

    public String getUrgentwriting() {
        return urgentwriting;
    }

    public void setUrgentwriting(String urgentwriting) {
        this.urgentwriting = urgentwriting;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIorder() {
        return iorder;
    }

    public void setIorder(Long iorder) {
        this.iorder = iorder;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Long getvTime() {
        return vTime;
    }

    public void setvTime(Long vTime) {
        this.vTime = vTime;
    }

    public Long getWeather() {
        return weather;
    }

    public void setWeather(Long weather) {
        this.weather = weather;
    }

    public String getBackColor() {
        return backColor;
    }

    public void setBackColor(String backColor) {
        this.backColor = backColor;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public Long getFontSize() {
        return fontSize;
    }

    public void setFontSize(Long fontSize) {
        this.fontSize = fontSize;
    }

    public Long getVelocity() {
        return velocity;
    }

    public void setVelocity(Long velocity) {
        this.velocity = velocity;
    }

    public String getWriting() {
        return writing;
    }

    public void setWriting(String writing) {
        this.writing = writing;
    }
}
