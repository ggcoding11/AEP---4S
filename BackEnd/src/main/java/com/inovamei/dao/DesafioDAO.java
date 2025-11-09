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
        String sql = "INSERT INTO desafios (titulo, id_empresa, posicao_atual, processo_atual, problemas_encontrados, impacto_negocio, o_que_facilitar, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pendente')";

        String sqlSelectNomeEmpresa = "SELECT nome_empresa FROM empresas WHERE id_empresa = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, desafio.getTitulo());
            pstmt.setInt(2, desafio.getEmpresaId());

            // Mapeando a descrição para as 5 colunas TEXT (Solução para funcionar com seu modelo atual)
            pstmt.setString(3, desafio.getDescricao()); // posicao_atual
            pstmt.setString(4, desafio.getDescricao()); // processo_atual
            pstmt.setString(5, desafio.getDescricao()); // problemas_encontrados
            pstmt.setString(6, desafio.getDescricao()); // impacto_negocio
            pstmt.setString(7, desafio.getDescricao()); // o_que_facilitar

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

    // DesafioDAO.java
    public List<Desafio> findAll() {
        // CORREÇÃO: Colunas 'descricao' e 'status_desafio' foram substituídas por 'posicao_atual' e 'status' (nomes do seu SQL).
        String sql = "SELECT d.id_desafio, d.titulo, d.id_empresa, e.nome_empresa, d.posicao_atual, d.status " +
                "FROM desafios d " +
                "JOIN empresas e ON d.id_empresa = e.id_empresa " +
                "ORDER BY d.data_criacao DESC";

        List<Desafio> desafios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Desafio desafio = new Desafio();
                desafio.setId(rs.getInt("id_desafio"));
                desafio.setTitulo(rs.getString("titulo"));
                // Mapeia a coluna 'posicao_atual' do banco para a propriedade 'descricao' do Java
                desafio.setDescricao(rs.getString("posicao_atual"));
                desafio.setEmpresaId(rs.getInt("id_empresa"));
                desafio.setNomeEmpresa(rs.getString("nome_empresa"));
                // Mapeia a coluna 'status' do banco para a propriedade 'statusDesafio' do Java
                desafio.setStatusDesafio(rs.getString("status"));
                desafios.add(desafio);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar desafios: " + e.getMessage(), e);
        }
        return desafios;
    }

    // DesafioDAO.java - findById (DEPOIS)
    public Optional<Desafio> findById(int id) {
        // CORREÇÃO: Usando a coluna 'posicao_atual' do banco no lugar de 'descricao'
        String sql = "SELECT d.id_desafio, d.titulo, d.id_empresa, e.nome_empresa, d.posicao_atual, d.status " +
                "FROM desafios d " +
                "JOIN empresas e ON d.id_empresa = e.id_empresa " +
                "WHERE d.id_desafio = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Desafio desafio = new Desafio();
                    desafio.setId(rs.getInt("id_desafio"));
                    desafio.setTitulo(rs.getString("titulo"));
                    // Mapeia 'posicao_atual' para a propriedade 'descricao'
                    desafio.setDescricao(rs.getString("posicao_atual"));
                    desafio.setEmpresaId(rs.getInt("id_empresa"));
                    desafio.setNomeEmpresa(rs.getString("nome_empresa"));
                    desafio.setStatusDesafio(rs.getString("status")); // Usa a coluna correta
                    return Optional.of(desafio);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar desafio por ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
}