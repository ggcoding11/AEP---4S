package com.inovamei.api.dto;

/**
 * DTO (Data Transfer Object) para receber os dados de criação de Pitch do frontend.
 * Os campos públicos são mapeados automaticamente pelo Jackson/ObjectMapper.
 */
public class PitchCreateRequest {
    public int id_desafio;
    public int id_aluno;
    public String url_video_pitch;
}