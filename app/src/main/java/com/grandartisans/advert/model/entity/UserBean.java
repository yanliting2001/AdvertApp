package com.grandartisans.advert.model.entity;

/**
 * 
 * @类描述：用户会员信息实体类
 * @项目名称：TXBootSettings
 * @包名： com.example.txbootsettings.bean
 * @类名称：UserBean	
 * @创建人：Administrator
 * @创建时间：2014-9-4下午12:05:03	
 * @修改人：Administrator
 * @修改时间：2014-9-4下午12:05:03	
 * @修改备注：
 * @version v1.0
 * @see [nothing]
 * @bug [nothing]
 * @Copyright pivos
 * @mail 939757170@qq.com
 */
public class UserBean {
	private String openid;//QQ号对应的openID
	private int vip; //是否是会员，0为不是，1为是
	private long start;//vip 会员的开始时间（UTC 秒）。如果不是 vip，则为空
	private long end;//vip 会员的结束时间（UTC 秒）。如果不是 vip，则为空
	private int ret;//接口返回值，成功返回为 0，否则失败,返回值为100014 表示accesstoken 已过期
	private String msg;//接口返回失败信息，成功返回“ok”
	private int is_lost;
	
	public int getIs_lost() {
		return is_lost;
	}

	public void setIs_lost(int is_lost) {
		this.is_lost = is_lost;
	}
	public String getOpenid() {
		return openid;
	}
	
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public int getVip() {
		return vip;
	}
	public void setVip(int vip) {
		this.vip = vip;
	}
	public long getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public int getRet() {
		return ret;
	}
	public void setRet(int ret) {
		this.ret = ret;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}


}
