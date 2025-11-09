package com.inovamei;

import com.inovamei.api.dto.*;
import com.inovamei.dao.DesafioDAO;
import com.inovamei.dao.PitchDAO;
import com.inovamei.model.Desafio;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.inovamei.dao.EmpresaDAO;
import com.inovamei.model.Empresa;
import com.inovamei.dao.AlunoDAO;
import com.inovamei.model.Aluno;

import com.inovamei.model.Pitch;
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
        PitchDAO pitchDAO = new PitchDAO(); // <-- ADICIONE ESTA LINHA
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

        // --- ENDPOINT ÚNICO PARA DESAFIOS (LISTAR TUDO OU BUSCAR POR ID) ---
        // --- ENDPOINT ÚNICO PARA DESAFIOS (LISTAR TUDO OU BUSCAR POR ID) ---
        // CORREÇÃO: Remova a barra final para capturar /desafios E /desafios/
        server.createContext("/desafios", exchange -> {
            try {
                if (isOptions(exchange)) { sendCors(exchange, 200, ""); return; }
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, error("METHOD_NOT_ALLOWED", "Method Not Allowed"));
                    return;
                }

                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                // path /desafios -> parts = ["", "desafios"] (length 2)
                // path /desafios/1 -> parts = ["", "desafios", "1"] (length 3)

                if (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty())) {
                    // --- CASO 1: LISTAR TODOS (URL é /desafios ou /desafios/) ---
                    List<Desafio> desafios = desafioDAO.findAll();
                    sendJson(exchange, 200, OM.writeValueAsString(Map.of("success", true, "desafios", desafios)));

                } else if (parts.length == 3) {
                    // --- CASO 2: BUSCAR POR ID (URL é /desafios/1) ---
                    try {
                        int id = Integer.parseInt(parts[2]); // Pega o ID
                        desafioDAO.findById(id)
                                .ifPresentOrElse(
                                        desafio -> { // Se encontrou
                                            try {
                                                sendJson(exchange, 200, OM.writeValueAsString(Map.of("success", true, "desafio", desafio)));
                                            } catch (IOException e) { e.printStackTrace(); }
                                        },
                                        () -> { // Se não encontrou
                                            try {
                                                sendJson(exchange, 404, error("NOT_FOUND", "Desafio não encontrado"));
                                            } catch (IOException e) { e.printStackTrace(); }
                                        }
                                );
                    } catch (NumberFormatException e) {
                        sendJson(exchange, 400, error("BAD_REQUEST", "ID do desafio inválido"));
                    }
                } else {
                    sendJson(exchange, 400, error("BAD_REQUEST", "URL mal formatada"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, error("INTERNAL_SERVER_ERROR", "Ocorreu um erro inesperado"));
            }
        });
        // --- FIM DO NOVO BLOCO ---
        // --- FIM DO NOVO BLOCO ---

        // --- NOVO ENDPOINT PARA ENVIAR PITCH (POST) ---
        server.createContext("/pitches/create", exchange -> {
            if (isOptions(exchange)) { sendCors(exchange, 200, ""); return; }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, error("METHOD_NOT_ALLOWED", "Method Not Allowed"));
                return;
            }

            String body = readBody(exchange);
            PitchCreateRequest req;
            try {
                // Tenta ler o JSON do corpo (o DTO que o JS enviou)
                req = OM.readValue(body, PitchCreateRequest.class);
            } catch (Exception ex) {
                sendJson(exchange, 400, error("BAD_REQUEST", "JSON inválido"));
                return;
            }

            // Validação simples
            if (req.id_aluno <= 0 || req.id_desafio <= 0 || isBlank(req.url_video_pitch)) {
                sendJson(exchange, 400, error("VALIDATION_ERROR", "Campos obrigatórios ausentes ou inválidos (id_aluno, id_desafio, url_video_pitch)"));
                return;
            }

            try {
                // Mapeia o DTO para o Model
                Pitch novoPitch = new Pitch();
                novoPitch.setAlunoId(req.id_aluno);
                novoPitch.setDesafioId(req.id_desafio);
                novoPitch.setUrlVideoPitch(trim(req.url_video_pitch));

                // Cria no banco (o DAO cuida do INSERT)
                Pitch criado = pitchDAO.create(novoPitch);

                // Resposta de sucesso
                Map<String, Object> dto = new LinkedHashMap<>();
                dto.put("id_pitch", criado.getId());
                dto.put("status", criado.getStatusPitch());

                sendJson(exchange, 201, OM.writeValueAsString(Map.of("success", true, "pitch", dto)));
            } catch (RuntimeException e) {
                if (e.getMessage().startsWith("DUPLICATE_PITCH")) {
                    sendJson(exchange, 409, error("DUPLICATE_PITCH", "Você já enviou um pitch para este desafio."));
                } else {
                    e.printStackTrace();
                    sendJson(exchange, 500, error("INTERNAL_SERVER_ERROR", "Ocorreu um erro ao salvar o pitch: " + e.getMessage()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, error("INTERNAL_SERVER_ERROR", "Ocorreu um erro inesperado."));
            }
        });

        // --- ENDPOINT PARA CRIAR NOVO DESAFIO (POST) ---
        server.createContext("/desafios/create", exchange -> {
            if (isOptions(exchange)) { sendCors(exchange, 200, ""); return; }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, error("METHOD_NOT_ALLOWED", "Method Not Allowed. Use POST."));
                return;
            }

            String body = readBody(exchange);
            DesafioCreateRequest req;
            try {
                // Tenta mapear o JSON do frontend para o DTO
                req = OM.readValue(body, DesafioCreateRequest.class);
            } catch (Exception ex) {
                sendJson(exchange, 400, error("BAD_REQUEST", "JSON inválido"));
                return;
            }

            // Validação dos dados que vieram do formulário
            if (isBlank(req.titulo) || isBlank(req.descricao) || req.id_empresa <= 0) {
                sendJson(exchange, 400, error("VALIDATION_ERROR", "Campos obrigatórios: titulo, descricao, id_empresa"));
                return;
            }

            try {
                Desafio d = new Desafio();
                d.setTitulo(trim(req.titulo));
                d.setDescricao(trim(req.descricao));
                d.setEmpresaId(req.id_empresa); // ID da empresa logada
                // O DesafioDAO irá cuidar de popular a data_criacao e status_desafio (Pendente)

                Desafio created = desafioDAO.create(d);

                Map<String, Object> dto = new LinkedHashMap<>();
                dto.put("id_desafio", created.getId());
                dto.put("titulo", created.getTitulo());
                dto.put("nomeEmpresa", created.getNomeEmpresa());

                sendJson(exchange, 201, OM.writeValueAsString(Map.of("success", true, "desafio", dto)));

            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, error("SERVER_ERROR", "Erro interno ao criar desafio"));
            }
        });

        server.createContext("/desafios/pitches", exchange -> {
            if (isOptions(exchange)) { sendCors(exchange, 200, ""); return; }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, error("METHOD_NOT_ALLOWED", "Method Not Allowed. Use GET."));
                return;
            }

            // 1. Obtém os parâmetros da URL (requer o helper getQueryParams abaixo)
            // URL esperado: /desafios/pitches?id={id_desafio}
            Map<String, String> params = getQueryParams(exchange);
            String idDesafioStr = params.get("id");

            if (isBlank(idDesafioStr)) {
                sendJson(exchange, 400, error("VALIDATION_ERROR", "ID do desafio não fornecido (id=X)"));
                return;
            }

            try {
                int idDesafio = Integer.parseInt(idDesafioStr);

                // 2. Chama o DAO (Método que você acabou de criar no PitchDAO)
                List<Pitch> pitches = pitchDAO.findByDesafioId(idDesafio);

                // 3. Retorna a lista de pitches
                sendJson(exchange, 200, OM.writeValueAsString(Map.of("success", true, "pitches", pitches)));

            } catch (NumberFormatException ex) {
                sendJson(exchange, 400, error("BAD_REQUEST", "ID do desafio inválido"));
            } catch (Exception ex) {
                ex.printStackTrace();
                sendJson(exchange, 500, error("SERVER_ERROR", "Erro interno ao buscar pitches"));
            }
        });

        // --- ADICIONE OS IMPORTS NECESSÁRIOS NO TOPO DO ARQUIVO ---
        // import com.inovamei.api.dto.DesafioCreateRequest;
        // import com.inovamei.model.Desafio;

        // ... (o restante do Main.java continua abaixo, incluindo /desafios) ...

        server.setExecutor(null);
        server.start();
        System.out.println("HTTP server started on port " + port);

    }

    // --- Helpers ---
    private static boolean isOptions(HttpExchange ex) {
        return "OPTIONS".equalsIgnoreCase(ex.getRequestMethod());
    }

    private static Map<String, String> getQueryParams(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = new LinkedHashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    String key = idx > 0 ? java.net.URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()) : pair;
                    // Note: Usamos java.net.URLDecoder para garantir que caracteres especiais sejam lidos corretamente
                    String value = idx > 0 && pair.length() > idx + 1 ? java.net.URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name()) : null;
                    params.put(key, value);
                } catch (java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return params;
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
