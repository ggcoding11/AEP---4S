package com.inovamei.model;

public class Desafio {
    private int id;
    private String titulo;
    private String descricao;
    private int empresaId;
    private String nomeEmpresa;

    // --- CONSTRUTOR VAZIO (O QUE O DAO PRECISA) ---
    public Desafio() {
    }

    // --- Construtor de 5 argumentos (opcional, mas bom ter) ---
    public Desafio(int id, String titulo, String descricao, int empresaId, String nomeEmpresa) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.empresaId = empresaId;
        this.nomeEmpresa = nomeEmpresa;
    }

    // --- GETTERS E SETTERS (O QUE O DAO PRECISA) ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getEmpresaId() { return empresaId; }
    public void setEmpresaId(int empresaId) { this.empresaId = empresaId; }

    public String getNomeEmpresa() { return nomeEmpresa; }
    public void setNomeEmpresa(String nomeEmpresa) { this.nomeEmpresa = nomeEmpresa; }
}