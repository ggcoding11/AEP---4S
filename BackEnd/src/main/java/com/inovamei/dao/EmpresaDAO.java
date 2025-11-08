package com.inovamei.dao;

import com.inovamei.config.DatabaseConnection;
import com.inovamei.model.Empresa;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Optional;

public class EmpresaDAO {

    public boolean existsByCnpjOrEmail(String cnpj, String email) {
        String sql = "SELECT 1 FROM empresas WHERE cnpj = ? OR email_contato = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cnpj);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar empresa", e);
        }
    }

    public Empresa create(Empresa emp) {
        String sql = "INSERT INTO empresas (nome_empresa, cnpj, email_contato, setor, cidade, senha_hash) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, emp.getNomeEmpresa());
            ps.setString(2, emp.getCnpj());
            ps.setString(3, emp.getEmailContato());
            ps.setString(4, emp.getSetor());
            ps.setString(5, emp.getCidade());
            String hash = BCrypt.hashpw(emp.getSenha(), BCrypt.gensalt(12));
            ps.setString(6, hash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    emp.setId(keys.getInt(1));
                }
            }
            // fetch data_cadastro
            return findById(emp.getId()).orElse(emp);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar empresa", e);
        }
    }

    public Optional<Empresa> findById(int id) {
        String sql = "SELECT id_empresa, nome_empresa, cnpj, email_contato, setor, cidade, data_cadastro FROM empresas WHERE id_empresa = ?";
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
            throw new RuntimeException("Erro ao buscar empresa por id", e);
        }
    }

    public Optional<Empresa> findByCnpjSenha(String cnpj, String senha) {
        String sql = "SELECT id_empresa, nome_empresa, cnpj, email_contato, setor, cidade, senha_hash, data_cadastro FROM empresas WHERE cnpj = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cnpj);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("senha_hash");
                    if (hash != null && BCrypt.checkpw(senha, hash)) {
                        Empresa e = new Empresa();
                        e.setId(rs.getInt("id_empresa"));
                        e.setNomeEmpresa(rs.getString("nome_empresa"));
                        e.setCnpj(rs.getString("cnpj"));
                        e.setEmailContato(rs.getString("email_contato"));
                        e.setSetor(rs.getString("setor"));
                        e.setCidade(rs.getString("cidade"));
                        e.setDataCadastro(rs.getTimestamp("data_cadastro"));
                        return Optional.of(e);
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar empresa", e);
        }
    }

    private Empresa mapper(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setId(rs.getInt("id_empresa"));
        e.setNomeEmpresa(rs.getString("nome_empresa"));
        e.setCnpj(rs.getString("cnpj"));
        e.setEmailContato(rs.getString("email_contato"));
        e.setSetor(rs.getString("setor"));
        e.setCidade(rs.getString("cidade"));
        e.setDataCadastro(rs.getTimestamp("data_cadastro"));
        return e;
    }
}
