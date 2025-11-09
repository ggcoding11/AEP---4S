package com.inovamei.dao;

import com.inovamei.config.DatabaseConnection;
import com.inovamei.model.Pitch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class PitchDAO {

    /**
     * Salva um novo Pitch no banco de dados.
     * Assume que o status_pitch é 'Enviado' por default.
     */
    public Pitch create(Pitch pitch) {
        // Assume que as colunas são id_desafio, id_aluno, url_video_pitch, status_pitch
        String sql = "INSERT INTO pitches (id_desafio, id_aluno, url_video_pitch) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, pitch.getDesafioId());
            pstmt.setInt(2, pitch.getAlunoId());
            pstmt.setString(3, pitch.getUrlVideoPitch());
            // Status é definido por default no banco, não precisa ser setado aqui

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar o pitch, nenhuma linha afetada.");
            }

            // Recupera o ID gerado pelo banco
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pitch.setId(generatedKeys.getInt(1));
                    // O banco também define a data_envio e status_pitch.
                    // Para simplificar, pegamos apenas o ID, mas o status default é 'Enviado'.
                    pitch.setStatusPitch("Enviado");
                } else {
                    throw new SQLException("Falha ao criar o pitch, ID não obtido.");
                }
            }
        } catch (SQLException e) {
            // Este catch irá capturar erros como tentativas duplicadas (UNIQUE KEY uq_aluno_desafio)
            if (e.getMessage().contains("uq_aluno_desafio")) {
                throw new RuntimeException("DUPLICATE_PITCH: Você já enviou um pitch para este desafio.", e);
            }
            throw new RuntimeException("Erro ao criar o pitch: " + e.getMessage(), e);
        }
        return pitch;
    }
}