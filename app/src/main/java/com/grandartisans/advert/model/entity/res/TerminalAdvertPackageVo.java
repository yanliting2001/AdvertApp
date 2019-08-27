/**
* <p> * Title: 叶明开发的代码系统*</p>
* <p> * Description: * </p>
* <p> * Copyright: Copyright (c) 2012-2018* </p>
* <p> * Company: java工作者 * </p>
* @author 叶明（开发）
* @version 4.0.2
*/
package com.grandartisans.advert.model.entity.res;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2018年5月2日 上午7:09:20
 * 开发者：叶明(email:guming123416@163.com,QQ:47043760)
 * 修改者：
 * 修改时间：
 * 程序作用：
 * 1、
 * 2、
 * 修改说明：
 * 1、
 * 2、
 * 版本：@version 4.0.2
 * @author 叶明
 */
@SuppressWarnings("serial")
public class TerminalAdvertPackageVo {
    private String deviceClientid;
	private List<AdvertScheduleVo> advertSchedules = new ArrayList<>();

	public String getDeviceClientid() {
		return deviceClientid;
	}

	public void setDeviceClientid(String deviceClientid) {
		this.deviceClientid = deviceClientid;
	}

	public List<AdvertScheduleVo> getAdvertSchedules() {
		return advertSchedules;
	}

	public void setAdvertSchedules(List<AdvertScheduleVo> advertSchedules) {
		this.advertSchedules = advertSchedules;
	}
}
