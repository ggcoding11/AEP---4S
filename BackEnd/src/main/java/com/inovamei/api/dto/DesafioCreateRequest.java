package com.inovamei.api.dto;

public class DesafioCreateRequest {
    // Corresponde ao campo 'titulo' enviado pelo formulário (input de texto)
    public String titulo;

    // Corresponde à concatenação dos 5 campos de texto do formulário
    public String descricao;

    // Corresponde ao 'id' da Empresa logada, que o js/criacaoForm.js insere.
    public int id_empresa;
}