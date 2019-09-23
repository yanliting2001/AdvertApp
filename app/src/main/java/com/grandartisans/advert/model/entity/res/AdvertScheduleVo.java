package com.grandartisans.advert.model.entity.res;

import java.util.ArrayList;
import java.util.List;

public class AdvertScheduleVo {
    private AdvertSchedule advertSchedule;
    private List<TemplateVo> templateVo ;

    public AdvertSchedule getAdvertSchedule() {
        return advertSchedule;
    }

    public void setAdvertSchedule(AdvertSchedule advertSchedule) {
        this.advertSchedule = advertSchedule;
    }

    public List<TemplateVo> getTemplateVo() {
        return templateVo;
    }

    public void setTemplateVo(List<TemplateVo> templateVo) {
        this.templateVo = templateVo;
    }
}
