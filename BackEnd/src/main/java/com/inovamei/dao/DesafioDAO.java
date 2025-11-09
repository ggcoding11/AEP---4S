package com.inovamei.dao;

import com.inovamei.model.Desafio;
import com.inovamei.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DesafioDAO {

    public Desafio create(Desafio d) throws SQLException {
        String sql = "INSERT INTO desafios (id_empresa, titulo, posicao_atual, processo_atual, problemas_encontrados, impacto_negocio, o_que_facilitar) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getIdEmpresa());
            ps.setString(2, d.getTitulo());
            ps.setString(3, d.getPosicaoAtual());
            ps.setString(4, d.getProcessoAtual());
            ps.setString(5, d.getProblemasEncontrados());
            ps.setString(6, d.getImpactoNegocio());
            ps.setString(7, d.getOQueFacilitar());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return findById(id).orElse(null);
                }
            }
        }
        return null;
    }

    public Optional<Desafio> findById(int idDesafio) throws SQLException {
        String sql = "SELECT id_desafio, id_empresa, titulo, posicao_atual, processo_atual, problemas_encontrados, impacto_negocio, o_que_facilitar, status, data_criacao FROM desafios WHERE id_desafio = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDesafio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Desafio d = new Desafio();
                    d.setIdDesafio(rs.getInt("id_desafio"));
                    d.setIdEmpresa(rs.getInt("id_empresa"));
                    d.setTitulo(rs.getString("titulo"));
                    d.setPosicaoAtual(rs.getString("posicao_atual"));
                    d.setProcessoAtual(rs.getString("processo_atual"));
                    d.setProblemasEncontrados(rs.getString("problemas_encontrados"));
                    d.setImpactoNegocio(rs.getString("impacto_negocio"));
                    d.setOQueFacilitar(rs.getString("o_que_facilitar"));
                    d.setStatus(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("data_criacao");
                    d.setDataCriacao(ts != null ? ts.toLocalDateTime() : null);
                    return Optional.of(d);
                }
            }
        }
        return Optional.empty();
    }

    public List<Desafio> findAll() throws SQLException {
        String sql = "SELECT id_desafio, id_empresa, titulo, posicao_atual, processo_atual, problemas_encontrados, impacto_negocio, o_que_facilitar, status, data_criacao FROM desafios ORDER BY data_criacao DESC, id_desafio DESC";
        List<Desafio> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Desafio d = new Desafio();
                d.setIdDesafio(rs.getInt("id_desafio"));
                d.setIdEmpresa(rs.getInt("id_empresa"));
                d.setTitulo(rs.getString("titulo"));
                d.setPosicaoAtual(rs.getString("posicao_atual"));
                d.setProcessoAtual(rs.getString("processo_atual"));
                d.setProblemasEncontrados(rs.getString("problemas_encontrados"));
                d.setImpactoNegocio(rs.getString("impacto_negocio"));
                d.setOQueFacilitar(rs.getString("o_que_facilitar"));
                d.setStatus(rs.getString("status"));
                Timestamp ts = rs.getTimestamp("data_criacao");
                d.setDataCriacao(ts != null ? ts.toLocalDateTime() : null);
                list.add(d);
            }
        }
        return list;
    }

    public List<Desafio> findByEmpresaId(int empresaId) throws SQLException {
        String sql = "SELECT id_desafio, id_empresa, titulo, posicao_atual, processo_atual, problemas_encontrados, impacto_negocio, o_que_facilitar, status, data_criacao FROM desafios WHERE id_empresa = ? ORDER BY data_criacao DESC, id_desafio DESC";
        List<Desafio> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empresaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Desafio d = new Desafio();
                    d.setIdDesafio(rs.getInt("id_desafio"));
                    d.setIdEmpresa(rs.getInt("id_empresa"));
                    d.setTitulo(rs.getString("titulo"));
                    d.setPosicaoAtual(rs.getString("posicao_atual"));
                    d.setProcessoAtual(rs.getString("processo_atual"));
                    d.setProblemasEncontrados(rs.getString("problemas_encontrados"));
                    d.setImpactoNegocio(rs.getString("impacto_negocio"));
                    d.setOQueFacilitar(rs.getString("o_que_facilitar"));
                    d.setStatus(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("data_criacao");
                    d.setDataCriacao(ts != null ? ts.toLocalDateTime() : null);
                    list.add(d);
                }
            }
        }
        return list;
    }
}