package com.inovamei.dao;

import com.inovamei.config.DatabaseConnection;
import com.inovamei.model.Aluno;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Optional;

public class AlunoDAO {

    public boolean existsByEmail(String emailInstitucional) {
        String sql = "SELECT 1 FROM alunos WHERE email_institucional = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailInstitucional);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar aluno por email", e);
        }
    }

    public Aluno create(Aluno a) {
        String sql = "INSERT INTO alunos (nome_completo, email_institucional, curso, semestre, habilidades, url_comprovante, foto_perfil_url, senha_hash) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getNomeCompleto());
            ps.setString(2, a.getEmailInstitucional());
            ps.setString(3, a.getCurso());
            ps.setInt(4, a.getSemestre());
            ps.setString(5, a.getHabilidades());
            ps.setString(6, a.getUrlComprovante());
            ps.setString(7, a.getFotoPerfilUrl());
            String hash = BCrypt.hashpw(a.getSenha(), BCrypt.gensalt(12));
            ps.setString(8, hash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setId(keys.getInt(1));
                }
            }
            return findById(a.getId()).orElse(a);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar aluno", e);
        }
    }

    public Optional<Aluno> findById(int id) {
        String sql = "SELECT id_aluno, nome_completo, email_institucional, curso, semestre, habilidades, url_comprovante, foto_perfil_url, data_cadastro FROM alunos WHERE id_aluno = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar aluno por id", e);
        }
    }

    public Optional<Aluno> findByEmailSenha(String email, String senha) {
        String sql = "SELECT id_aluno, nome_completo, email_institucional, curso, semestre, habilidades, url_comprovante, foto_perfil_url, senha_hash, data_cadastro FROM alunos WHERE email_institucional = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("senha_hash");
                    if (hash != null && BCrypt.checkpw(senha, hash)) {
                        Aluno a = new Aluno();
                        a.setId(rs.getInt("id_aluno"));
                        a.setNomeCompleto(rs.getString("nome_completo"));
                        a.setEmailInstitucional(rs.getString("email_institucional"));
                        a.setCurso(rs.getString("curso"));
                        a.setSemestre(rs.getInt("semestre"));
                        a.setHabilidades(rs.getString("habilidades"));
                        a.setUrlComprovante(rs.getString("url_comprovante"));
                        a.setFotoPerfilUrl(rs.getString("foto_perfil_url"));
                        a.setDataCadastro(rs.getTimestamp("data_cadastro"));
                        return Optional.of(a);
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar aluno", e);
        }
    }

    private Aluno mapper(ResultSet rs) throws SQLException {
        Aluno a = new Aluno();
        a.setId(rs.getInt("id_aluno"));
        a.setNomeCompleto(rs.getString("nome_completo"));
        a.setEmailInstitucional(rs.getString("email_institucional"));
        a.setCurso(rs.getString("curso"));
        a.setSemestre(rs.getInt("semestre"));
        a.setHabilidades(rs.getString("habilidades"));
        a.setUrlComprovante(rs.getString("url_comprovante"));
        a.setFotoPerfilUrl(rs.getString("foto_perfil_url"));
        a.setDataCadastro(rs.getTimestamp("data_cadastro"));
        return a;
    }
}
