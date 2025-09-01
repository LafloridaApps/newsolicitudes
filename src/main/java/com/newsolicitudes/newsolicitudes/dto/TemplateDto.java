package com.newsolicitudes.newsolicitudes.dto;

public class TemplateDto {

    private Long id;
    private String nombre;
    private String docFile;

    public TemplateDto(Long id, String nombre, String docFile) {
        this.id = id;
        this.nombre = nombre;
        this.docFile = docFile;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocFile() {
        return docFile;
    }

    public void setDocFile(String docFile) {
        this.docFile = docFile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
