package com.inovamei.dao;

import com.inovamei.config.DatabaseConnection;
import com.inovamei.model.Desafio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DesafioDAO {

    // --- NOVO MÉTODO: CREATE ---
    public Desafio create(Desafio desafio) {
        // Assume que 'titulo', 'descricao', e 'id_empresa' estão definidos
        String sql = "INSERT INTO desafios (titulo, descricao, id_empresa, status_desafio) VALUES (?, ?, ?, 'Pendente')";

        // Para buscar o nome da empresa após a criação (necessário para o DTO de resposta no Main.java)
        String sqlSelectNomeEmpresa = "SELECT nome_empresa FROM empresas WHERE id_empresa = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, desafio.getTitulo());
            pstmt.setString(2, desafio.getDescricao());
            pstmt.setInt(3, desafio.getEmpresaId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar desafio, nenhuma linha afetada.");
            }

            // 1. Recupera o ID gerado pelo banco
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    desafio.setId(generatedKeys.getInt(1)); // Define o ID gerado no objeto
                    desafio.setStatusDesafio("Pendente");
                } else {
                    throw new SQLException("Falha ao criar desafio, ID não obtido.");
                }
            }

            // 2. Busca o nome da empresa pelo ID para o DTO de resposta
            try (PreparedStatement pstmtEmpresa = conn.prepareStatement(sqlSelectNomeEmpresa)) {
                pstmtEmpresa.setInt(1, desafio.getEmpresaId());
                try (ResultSet rsEmpresa = pstmtEmpresa.executeQuery()) {
                    if (rsEmpresa.next()) {
                        desafio.setNomeEmpresa(rsEmpresa.getString("nome_empresa"));
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar desafio: " + e.getMessage(), e);
        }
        return desafio;
    }

    public List<Desafio> findAll() {
        // SQL CORRIGIDO (usando 'titulo' e 'descricao' do seu .sql)
        String sql = "SELECT d.id_desafio, d.titulo, d.descricao, d.id_empresa, e.nome_empresa " +
                "FROM desafios d " +
                "JOIN empresas e ON d.id_empresa = e.id_empresa " +
                "ORDER BY d.id_desafio DESC";

        List<Desafio> desafios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Desafio desafio = new Desafio();

                // Mapeamento CORRIGIDO (SQL -> Java)
                desafio.setId(rs.getInt("id_desafio"));
                desafio.setTitulo(rs.getString("titulo")); // Corrigido
                desafio.setDescricao(rs.getString("descricao")); // Corrigido
                desafio.setEmpresaId(rs.getInt("id_empresa"));
                desafio.setNomeEmpresa(rs.getString("nome_empresa"));

                desafios.add(desafio);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar desafios: " + e.getMessage(), e);
        }
        return desafios;
    }

    public Optional<Desafio> findById(int id) {
        String sql = "SELECT d.id_desafio, d.titulo, d.descricao, d.id_empresa, e.nome_empresa " +
                "FROM desafios d " +
                "JOIN empresas e ON d.id_empresa = e.id_empresa " +
                "WHERE d.id_desafio = ?"; // <-- A Mágica do WHERE

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id); // Define o ID

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Desafio desafio = new Desafio();
                    desafio.setId(rs.getInt("id_desafio"));
                    desafio.setTitulo(rs.getString("titulo"));
                    desafio.setDescricao(rs.getString("descricao"));
                    desafio.setEmpresaId(rs.getInt("id_empresa"));
                    desafio.setNomeEmpresa(rs.getString("nome_empresa"));
                    return Optional.of(desafio); // Retorna o desafio encontrado
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar desafio por ID: " + e.getMessage(), e);
        }
        return Optional.empty(); // Retorna vazio se não encontrar
    }
}