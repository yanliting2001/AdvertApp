package com.grandartisans.advert.model.entity.post;

public class ActiveCheckParamsBean {
	private String deviceClientid;
	private String  rqeuestUuid;
	private String  sign;
	private long  timestamp;
	public String getDeviceClientid() {
		return deviceClientid;
	}
	public void setDeviceClientid(String deviceClientid) {
		this.deviceClientid = deviceClientid;
	}
	public String getRqeuestUuid() {
		return rqeuestUuid;
	}
	public void setRqeuestUuid(String rqeuestUuid) {
		this.rqeuestUuid = rqeuestUuid;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
