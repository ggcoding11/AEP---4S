package com.inovamei.dao;

import com.inovamei.config.DatabaseConnection;
import com.inovamei.model.Pitch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public List<Pitch> findByDesafioId(int desafioId) {
        // SQL para buscar pitches E os dados básicos do ALUNO que enviou
        String sql = "SELECT p.id_pitch, p.url_video_pitch, p.status_pitch, p.data_envio, " +
                "a.id_aluno, a.nome_completo, a.curso, a.semestre " +
                "FROM pitches p " +
                "JOIN alunos a ON p.id_aluno = a.id_aluno " +
                "WHERE p.id_desafio = ? " +
                "ORDER BY p.data_envio DESC";

        List<Pitch> pitches = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, desafioId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Pitch pitch = new Pitch();
                    pitch.setId(rs.getInt("id_pitch"));
                    pitch.setUrlVideoPitch(rs.getString("url_video_pitch"));
                    pitch.setStatusPitch(rs.getString("status_pitch"));

                    // Note que Pitch deve ter um campo para o nome do aluno
                    pitch.setAlunoNome(rs.getString("nome_completo"));
                    pitch.setAlunoId(rs.getInt("id_aluno"));
                    try {
                        pitch.setCurso(rs.getString("curso"));
                        int sem = rs.getInt("semestre");
                        pitch.setSemestre(rs.wasNull() ? null : sem);
                    } catch (Exception ignored) {}

                    pitches.add(pitch);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pitches por ID do desafio: " + e.getMessage(), e);
        }
        return pitches;
    }

    public void markWinner(int pitchId, int desafioId) {
        String sqlWinner = "UPDATE pitches SET status_pitch = 'Vencedor' WHERE id_pitch = ? AND id_desafio = ?";
        String sqlOthers = "UPDATE pitches SET status_pitch = 'Não Selecionado' WHERE id_desafio = ? AND id_pitch <> ?";
        String sqlCloseDesafio = "UPDATE desafios SET status = 'Concluído' WHERE id_desafio = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean origAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlWinner);
                 PreparedStatement ps2 = conn.prepareStatement(sqlOthers);
                 PreparedStatement ps3 = conn.prepareStatement(sqlCloseDesafio)) {

                ps1.setInt(1, pitchId);
                ps1.setInt(2, desafioId);
                ps1.executeUpdate();

                ps2.setInt(1, desafioId);
                ps2.setInt(2, pitchId);
                ps2.executeUpdate();

                ps3.setInt(1, desafioId);
                ps3.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Erro ao marcar vencedor: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(origAuto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao marcar vencedor: " + e.getMessage(), e);
        }
    }
}