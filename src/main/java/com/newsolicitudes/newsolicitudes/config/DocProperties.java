package com.newsolicitudes.newsolicitudes.config;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Component
@ConfigurationProperties(prefix = "doc")
public class DocProperties {

    private String templatesPath;
    private String volumePath;

    public String getTemplatesPath() {
        return templatesPath;
    }

    public void setTemplatesPath(String templatesPath) {
        this.templatesPath = templatesPath;
    }

    public String getVolumePath() {
        return volumePath;
    }

    public void setVolumePath(String volumePath) {
        this.volumePath = volumePath;
    }

}