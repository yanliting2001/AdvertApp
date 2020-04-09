package com.grandartisans.advert.model.entity;

/**
 * 
 * @类描述：获取rom升级信息实体类
 * @项目名称：TXBootSettings
 * @包名： com.example.txbootsettings.bean
 * @类名称：UpgradeBean	
 * @创建人：Administrator
 * @创建时间：2014-9-9下午5:32:55	
 * @修改人：Administrator
 * @修改时间：2014-9-9下午5:32:55	
 * @修改备注：
 * @version v1.0
 * @see [nothing]
 * @bug [nothing]
 * @Copyright pivos
 * @mail 939757170@qq.com
 */
public class UpgradeBean {
	ResultBean result;
	UpgradeDataBean data;
	public ResultBean getResult() {
		return result;
	}
	public void setResult(ResultBean result) {
		this.result = result;
	}
	public UpgradeDataBean getData() {
		return data;
	}
	public void setData(UpgradeDataBean data) {
		this.data = data;
	}
}
