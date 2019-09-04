package com.grandartisans.advert.model.entity.res;

import java.util.List;

public class TemplateReginVo {
    TemplateRegion templateRegion;
    private List<AdvertVo> packageAdverts;

    public TemplateRegion getTemplateRegion() {
        return templateRegion;
    }

    public void setTemplateRegion(TemplateRegion templateRegion) {
        this.templateRegion = templateRegion;
    }

    public List<AdvertVo> getPackageAdverts() {
        return packageAdverts;
    }

    public void setPackageAdverts(List<AdvertVo> packageAdverts) {
        this.packageAdverts = packageAdverts;
    }
}
