package com.inovamei.dao;

import com.inovamei.config.DatabaseConnection;
import com.inovamei.model.Desafio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DesafioDAO {

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

}