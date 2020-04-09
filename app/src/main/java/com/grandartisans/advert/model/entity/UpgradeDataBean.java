package com.grandartisans.advert.model.entity;

/**
 * 
 * @类描述：获取rom升级信息 data部分实体类
 * @项目名称：TXBootSettings
 * @包名： com.example.txbootsettings.bean
 * @类名称：UpgradeDataBean	
 * @创建人：Administrator
 * @创建时间：2014-9-9下午5:30:19	
 * @修改人：Administrator
 * @修改时间：2014-9-9下午5:30:19	
 * @修改备注：
 * @version v1.0
 * @see [nothing]
 * @bug [nothing]
 * @Copyright pivos
 * @mail 939757170@qq.com
 */
public class UpgradeDataBean {
	String md5;
	String download_link;
	String desc;
	String rom_version;
	public String getRom_version() {
		return rom_version;
	}
	public void setRom_version(String rom_version) {
		this.rom_version = rom_version;
	}
	int force;
	String size;
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public String getDownload_link() {
		return download_link;
	}
	public void setDownload_link(String download_link) {
		this.download_link = download_link;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getForce() {
		return force;
	}
	public void setForce(int force) {
		this.force = force;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
}
