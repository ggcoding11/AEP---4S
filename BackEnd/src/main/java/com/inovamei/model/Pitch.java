package com.inovamei.model;

import java.time.LocalDateTime;

public class Pitch {
    private int id;
    private int desafioId;
    private int alunoId;
    private String urlVideoPitch;
    private LocalDateTime dataEnvio;
    private String statusPitch;
    private String alunoNome;
    private String curso;
    private Integer semestre;

    // ... (restante dos campos)



    // Construtor vazio (para uso do DAO)
    public Pitch() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAlunoNome() {
        return alunoNome;
    }

    public void setAlunoNome(String alunoNome) {
        this.alunoNome = alunoNome;
    }

    public int getDesafioId() { return desafioId; }
    public void setDesafioId(int desafioId) { this.desafioId = desafioId; }

    public int getAlunoId() { return alunoId; }
    public void setAlunoId(int alunoId) { this.alunoId = alunoId; }

    public String getUrlVideoPitch() { return urlVideoPitch; }
    public void setUrlVideoPitch(String urlVideoPitch) { this.urlVideoPitch = urlVideoPitch; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public String getStatusPitch() { return statusPitch; }
    public void setStatusPitch(String statusPitch) { this.statusPitch = statusPitch; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }
}