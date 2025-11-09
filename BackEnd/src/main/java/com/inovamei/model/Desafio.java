package com.inovamei.model;

import java.time.LocalDateTime;

public class Desafio {
    private int idDesafio;
    private int idEmpresa;
    private String titulo;
    private String posicaoAtual;
    private String processoAtual;
    private String problemasEncontrados;
    private String impactoNegocio;
    private String oQueFacilitar;
    private String status; // e.g., 'aberto', 'fechado' conforme banco
    private LocalDateTime dataCriacao;

    public int getIdDesafio() {
        return idDesafio;
    }

    public void setIdDesafio(int idDesafio) {
        this.idDesafio = idDesafio;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPosicaoAtual() {
        return posicaoAtual;
    }

    public void setPosicaoAtual(String posicaoAtual) {
        this.posicaoAtual = posicaoAtual;
    }

    public String getProcessoAtual() {
        return processoAtual;
    }

    public void setProcessoAtual(String processoAtual) {
        this.processoAtual = processoAtual;
    }

    public String getProblemasEncontrados() {
        return problemasEncontrados;
    }

    public void setProblemasEncontrados(String problemasEncontrados) {
        this.problemasEncontrados = problemasEncontrados;
    }

    public String getImpactoNegocio() {
        return impactoNegocio;
    }

    public void setImpactoNegocio(String impactoNegocio) {
        this.impactoNegocio = impactoNegocio;
    }

    public String getOQueFacilitar() {
        return oQueFacilitar;
    }

    public void setOQueFacilitar(String oQueFacilitar) {
        this.oQueFacilitar = oQueFacilitar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}