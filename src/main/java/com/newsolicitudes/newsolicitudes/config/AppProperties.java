package com.newsolicitudes.newsolicitudes.config;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String linkUrl;

    private String intranetUrl;

    public String getLinkUrl() {
        return linkUrl;
    }
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getIntranetUrl() {
        return intranetUrl;
    }

    public void setIntranetUrl(String intranetUrl) {
        this.intranetUrl = intranetUrl;
    }

}
