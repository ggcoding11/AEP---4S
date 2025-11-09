package com.inovamei;

import com.inovamei.dao.DesafioDAO;
import com.inovamei.model.Desafio;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.inovamei.api.dto.AlunoCreateRequest;
import com.inovamei.api.dto.EmpresaCreateRequest;
import com.inovamei.api.dto.LoginAlunoRequest;
import com.inovamei.api.dto.LoginEmpresaRequest;
import com.inovamei.dao.EmpresaDAO;
import com.inovamei.model.Empresa;
import com.inovamei.dao.AlunoDAO;
import com.inovamei.model.Aluno;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    private static final ObjectMapper OM = new ObjectMapper();
    public static void main(String[] args) throws IOException {
        int port = 4567;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/health", exchange -> {
            if (isOptions(exchange)) { sendCors(exchange, 200, ""); return; }
            sendJson(exchange, 200, OM.writeValueAsString(Map.of("status", "ok")));
        });

        AlunoDAO alunoDAO = new AlunoDAO();
        EmpresaDAO empresaDAO = new EmpresaDAO();
        DesafioDAO desafioDAO = new DesafioDAO(); // <-- CRIE A INSTÂNCIA
        Gson gson = new Gson();

        server.createContext("/empresas", exchange -> {
            if (isOptions(exchange)) { sendCors(exchange, 200, ""); return; }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, error("METHOD_NOT_ALLOWED", "Method Not Allowed"));
                return;
            }
            String body = readBody(exchange);
            EmpresaCreateRequest req;
            try {
                req = OM.readValue(body, EmpresaCreateRequest.class);
            } catch (Exception ex) {
                sendJson(exchange, 400, error("BAD_REQUEST", "JSON inválido"));
                return;
            }

            String nome = trim(req.nome_empresa);
            String cnpj = trim(req.cnpj);
            String email = trim(req.email_contato);
            String setor = trim(req.setor);
            String cidade = trim(req.cidade);
            String senha = trim(req.senha);

            if (isBlank(nome) || isBlank(cnpj) || isBlank(email) || isBlank(senha)) {
                sendJson(exchange, 400, error("VALIDATION_ERROR", "Campos obrigatórios: nome_empresa, cnpj, email_contato, senha"));
                return;
            }

            if (empresaDAO.existsByCnpjOrEmail(cnpj, email)) {
                sendJson(exchange, 409, error("DUPLICATE", "CNPJ ou e-mail já cadastrado"));
                return;
            }

            Empresa e = new Empresa();
            e.setNomeEmpresa(nome);
            e.setCnpj(cnpj);
            e.setEmailContato(email);
            e.setSetor(setor);
            e.setCidade(cidade);
            e.setSenha(senha);

            Empresa created = empresaDAO.create(e);

            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id_empresa", created.getId());
            dto.put("nome_empresa", created.getNomeEmpresa());
            dto.put("cnpj", created.getCnpj());
            dto.put("email_contato", created.getEmailContato());
            dto.put("setor", created.getSetor());
            dto.put("cidade", created.getCidade());
            dto.put("data_cadastro", created.getDataCadastro() != null ? created.getDataCadastro().toString() : null);

            sendJson(exchange, 201, OM.writeValueAsString(Map.of("success", true, "empresa", dto)));
        });

        server.createContext("/login-empresa", exchange -> {
            if (isOptions(exchange)) { sendCors(exchange, 200, ""); return; }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, error("METHOD_NOT_ALLOWED", "Method Not Allowed"));
                return;
            }
            String body = readBody(exchange);
            LoginEmpresaRequest req;
            try {
                req = OM.readValue(body, LoginEmpresaRequest.class);
            } catch (Exception ex) {
                sendJson(exchange, 400, error("BAD_REQUEST", "JSON inválido"));
                return;
            }
            String cnpj = trim(req.cnpj);
            String senha = trim(req.senha);
            if (isBlank(cnpj) || isBlank(senha)) {
                sendJson(exchange, 400, error("VALIDATION_ERROR", "Informe CNPJ e senha"));
                return;
            }
            empresaDAO.findByCnpjSenha(cnpj, senha)
                    .ifPresentOrElse(emp -> {
                        Map<String, Object> dto = new LinkedHashMap<>();
                        dto.put("id_empresa", emp.getId());
                        dto.put("nome_empresa", emp.getNomeEmpresa());
                        dto.put("cnpj", emp.getCnpj());
                        dto.put("email_contato", emp.getEmailContato());
                        dto.put("setor", emp.getSetor());
                        dto.put("cidade", emp.getCidade());
                        try {
                            sendJson(exchange, 200, OM.writeValueAsString(Map.of("success", true, "empresa", dto)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }, () -> {
                        try {
                            sendJson(exchange, 401, error("UNAUTHORIZED", "CNPJ ou senha inválidos"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        });

        // Cadastro de alunos
        server.createContext("/alunos", exchange -> {
            if (isOptions(exchange)) { sendCors(exchange, 200, ""); return; }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, error("METHOD_NOT_ALLOWED", "Method Not Allowed"));
                return;
            }
            String body = readBody(exchange);
            AlunoCreateRequest req;
            try {
                req = OM.readValue(body, AlunoCreateRequest.class);
            } catch (Exception ex) {
                sendJson(exchange, 400, error("BAD_REQUEST", "JSON inválido"));
                return;
            }

            String nome = trim(req.nome_completo);
            String email = trim(req.email_institucional);
            String curso = trim(req.curso);
            Integer semestre = req.semestre;
            String habilidades = trim(req.habilidades);
            String urlComprovante = trim(req.url_comprovante);
            String fotoPerfilUrl = trim(req.foto_perfil_url);
            String senhaAluno = trim(req.senha);

            if (isBlank(nome) || isBlank(email) || isBlank(curso) || semestre == null || semestre <= 0 || isBlank(urlComprovante) || isBlank(senhaAluno)) {
                sendJson(exchange, 400, error("VALIDATION_ERROR", "Campos obrigatórios: nome_completo, email_institucional, curso, semestre (>0), url_comprovante, senha"));
                return;
            }

            if (alunoDAO.existsByEmail(email)) {
                sendJson(exchange, 409, error("DUPLICATE", "E-mail institucional já cadastrado"));
                return;
            }

            Aluno a = new Aluno();
            a.setNomeCompleto(nome);
            a.setEmailInstitucional(email);
            a.setCurso(curso);
            a.setSemestre(semestre);
            a.setHabilidades(habilidades);
            a.setUrlComprovante(urlComprovante);
            a.setFotoPerfilUrl(fotoPerfilUrl);
            a.setSenha(senhaAluno);

            Aluno createdAluno = alunoDAO.create(a);

            Map<String, Object> dtoAluno = new LinkedHashMap<>();
            dtoAluno.put("id_aluno", createdAluno.getId());
            dtoAluno.put("nome_completo", createdAluno.getNomeCompleto());
            dtoAluno.put("email_institucional", createdAluno.getEmailInstitucional());
            dtoAluno.put("curso", createdAluno.getCurso());
            dtoAluno.put("semestre", createdAluno.getSemestre());
            dtoAluno.put("habilidades", createdAluno.getHabilidades());
            dtoAluno.put("url_comprovante", createdAluno.getUrlComprovante());
            dtoAluno.put("foto_perfil_url", createdAluno.getFotoPerfilUrl());
            dtoAluno.put("data_cadastro", createdAluno.getDataCadastro() != null ? createdAluno.getDataCadastro().toString() : null);

            sendJson(exchange, 201, OM.writeValueAsString(Map.of("success", true, "aluno", dtoAluno)));
        });

        // Login de alunos
        server.createContext("/login-aluno", exchange -> {
            if (isOptions(exchange)) { sendCors(exchange, 200, ""); return; }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, error("METHOD_NOT_ALLOWED", "Method Not Allowed"));
                return;
            }
            String body = readBody(exchange);
            LoginAlunoRequest req;
            try {
                req = OM.readValue(body, LoginAlunoRequest.class);
            } catch (Exception ex) {
                sendJson(exchange, 400, error("BAD_REQUEST", "JSON inválido"));
                return;
            }
            String email = trim(req.email_institucional);
            String senha = trim(req.senha);
            if (isBlank(email) || isBlank(senha)) {
                sendJson(exchange, 400, error("VALIDATION_ERROR", "Informe email_institucional e senha"));
                return;
            }
            alunoDAO.findByEmailSenha(email, senha)
                    .ifPresentOrElse(aluno -> {
                        Map<String, Object> dto = new LinkedHashMap<>();
                        dto.put("id_aluno", aluno.getId());
                        dto.put("nome_completo", aluno.getNomeCompleto());
                        dto.put("email_institucional", aluno.getEmailInstitucional());
                        dto.put("curso", aluno.getCurso());
                        dto.put("semestre", aluno.getSemestre());
                        dto.put("foto_perfil_url", aluno.getFotoPerfilUrl());
                        try {
                            sendJson(exchange, 200, OM.writeValueAsString(Map.of("success", true, "aluno", dto)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }, () -> {
                        try {
                            sendJson(exchange, 401, error("UNAUTHORIZED", "E-mail ou senha inválidos"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        });

        // --- ENDPOINT PARA BUSCAR TODOS OS DESAFIOS ---
        server.createContext("/desafios", exchange -> {
            try {
                if (isOptions(exchange)) { sendCors(exchange, 200, ""); return; }

                // Apenas GET é permitido
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, error("METHOD_NOT_ALLOWED", "Method Not Allowed"));
                    return;
                }

                List<Desafio> desafios = desafioDAO.findAll();

                // Converte a lista de desafios para JSON e envia
                sendJson(exchange, 200, OM.writeValueAsString(Map.of("success", true, "desafios", desafios)));

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sendJson(exchange, 500, error("INTERNAL_SERVER_ERROR", "Ocorreu um erro inesperado"));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        // --- FIM DO NOVO BLOCO ---

        server.setExecutor(null);
        server.start();
        System.out.println("HTTP server started on port " + port);

    }

    // --- Helpers ---
    private static boolean isOptions(HttpExchange ex) {
        return "OPTIONS".equalsIgnoreCase(ex.getRequestMethod());
    }

    private static void sendCors(HttpExchange exchange, int code, String body) throws IOException {
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(code, body.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(body.getBytes(StandardCharsets.UTF_8)); }
    }

    private static void sendJson(HttpExchange exchange, int code, String body) throws IOException {
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, body.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(body.getBytes(StandardCharsets.UTF_8)); }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String error(String code, String message) {
        try {
            return OM.writeValueAsString(Map.of(
                    "error", Map.of(
                            "code", code,
                            "message", message
                    )
            ));
        } catch (Exception e) {
            return "{\"error\":{\"code\":\"" + code + "\",\"message\":\"" + message.replace("\"", "'") + "\"}}";
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }
}
