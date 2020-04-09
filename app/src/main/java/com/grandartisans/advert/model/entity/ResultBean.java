package com.grandartisans.advert.model.entity;
/**
 * 
 * @类描述：获取guid返回结果result部分实体类
 * @项目名称：TXBootSettings
 * @包名： com.example.txbootsettings.bean
 * @类名称：AuthResultBean	
 * @创建人：Administrator
 * @创建时间：2014-9-4下午8:01:09	
 * @修改人：Administrator
 * @修改时间：2014-9-4下午8:01:09	
 * @修改备注：
 * @version v1.0
 * @see [nothing]
 * @bug [nothing]
 * @Copyright pivos
 * @mail 939757170@qq.com
 */
public class ResultBean {
	int ret;
	int code;
	String msg;
	int costtime;
	public int getRet() {
		return ret;
	}
	public void setRet(int ret) {
		this.ret = ret;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getCosttime() {
		return costtime;
	}
	public void setCosttime(int costtime) {
		this.costtime = costtime;
	}
}
