package com.grandartisans.advert.model.entity.res;

public class TemplateReginVo {
    TemplateRegion templateRegion;
    AdvertVo packageAdverts;

    public TemplateRegion getTemplateRegion() {
        return templateRegion;
    }

    public void setTemplateRegion(TemplateRegion templateRegion) {
        this.templateRegion = templateRegion;
    }

    public AdvertVo getPackageAdverts() {
        return packageAdverts;
    }

    public void setPackageAdverts(AdvertVo packageAdverts) {
        this.packageAdverts = packageAdverts;
    }
}
