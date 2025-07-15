package com.newsolicitudes.newsolicitudes.dto;

public class JefeFunc {

    private boolean esJefe;
    private boolean esDirector;


    

    public JefeFunc(boolean esJefe, boolean esDirector) {
        this.esJefe = esJefe;
        this.esDirector = esDirector;
    }

    public boolean isEsJefe() {
        return esJefe;
    }

    public void setEsJefe(boolean esJefe) {
        this.esJefe = esJefe;
    }

    public boolean isEsDirector() {
        return esDirector;
    }

    public void setEsDirector(boolean esDirector) {
        this.esDirector = esDirector;
    }

}
