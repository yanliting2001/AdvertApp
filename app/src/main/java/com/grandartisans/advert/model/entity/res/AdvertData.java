package com.grandartisans.advert.model.entity.res;

import com.grandartisans.advert.model.entity.PlayingAdvert;
import java.util.List;
import java.util.Map;

public class AdvertData {
    private AdvertSchedule advertSchedule;
    private Map<Long,List<PlayingAdvert>> advertMap;
    private List<TemplateVo> templateVo ;

    public AdvertSchedule getAdvertSchedule() {
        return advertSchedule;
    }

    public void setAdvertSchedule(AdvertSchedule advertSchedule) {
        this.advertSchedule = advertSchedule;
    }

    public Map<Long, List<PlayingAdvert>> getAdvertMap() {
        return advertMap;
    }

    public void setAdvertMap(Map<Long, List<PlayingAdvert>> advertMap) {
        this.advertMap = advertMap;
    }

    public List<TemplateVo> getTemplateVo() {
        return templateVo;
    }

    public void setTemplateVo(List<TemplateVo> templateVo) {
        this.templateVo = templateVo;
    }
}
