package com.newsolicitudes.newsolicitudes.config;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Component
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    private String funcionarioUrl;

    private String departamentoUrl;

    private String firmaUrl;

    private String pdfUrl;

    private String mailUrl;

    private String consultafirmaUrl;

    public String getFuncionarioUrl() {
        return funcionarioUrl;
    }

    public void setFuncionarioUrl(String funcionarioUrl) {
        this.funcionarioUrl = funcionarioUrl;
    }

    public String getDepartamentoUrl() {
        return departamentoUrl;
    }

    public void setDepartamentoUrl(String departamentoUrl) {
        this.departamentoUrl = departamentoUrl;
    }

    public String getFirmaUrl() {
        return firmaUrl;
    }

    public void setFirmaUrl(String firmaUrl) {
        this.firmaUrl = firmaUrl;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getMailUrl() {
        return mailUrl;
    }

    public void setMailUrl(String mailUrl) {
        this.mailUrl = mailUrl;
    }

    public String getConsultafirmaUrl() {
        return consultafirmaUrl;
    }

    public void setConsultafirmaUrl(String consultafirmaUrl) {
        this.consultafirmaUrl = consultafirmaUrl;
    }

}