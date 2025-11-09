package com.inovamei.model;

public class Desafio {
    private int id;
    private String titulo;
    private String descricao;
    private int empresaId;
    private String nomeEmpresa; // Vamos buscar isso com um JOIN

    // Construtor, Getters e Setters
    public Desafio(int id, String titulo, String descricao, int empresaId, String nomeEmpresa) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.empresaId = empresaId;
        this.nomeEmpresa = nomeEmpresa;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }
}