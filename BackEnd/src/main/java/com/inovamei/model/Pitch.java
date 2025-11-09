package com.inovamei.model;

import java.time.LocalDateTime;

public class Pitch {
    private int id;
    private int desafioId;
    private int alunoId;
    private String urlVideoPitch;
    private LocalDateTime dataEnvio;
    private String statusPitch;

    // Construtor vazio (para uso do DAO)
    public Pitch() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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
}