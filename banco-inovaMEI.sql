CREATE SCHEMA IF NOT EXISTS aep4s;

use aep4s;

-- CRIAÇÃO DE TABELAS

CREATE TABLE empresas (
                          id_empresa INT AUTO_INCREMENT PRIMARY KEY,
                          nome_empresa VARCHAR(255) NOT NULL,
                          cnpj VARCHAR(18) NOT NULL UNIQUE,
                          email_contato VARCHAR(255) NOT NULL UNIQUE,
                          setor VARCHAR(100),
                          cidade VARCHAR(100),
                          senha_hash VARCHAR(100) NOT NULL,
                          data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE alunos (
                        id_aluno INT AUTO_INCREMENT PRIMARY KEY,
                        nome_completo VARCHAR(255) NOT NULL,
                        email_institucional VARCHAR(255) NOT NULL UNIQUE,
                        curso VARCHAR(150) NOT NULL,
                        semestre INT NOT NULL,
                        habilidades TEXT,
                        url_comprovante VARCHAR(500) NOT NULL,
                        foto_perfil_url VARCHAR(500),
                        senha_hash VARCHAR(100) NOT NULL, -- MODIFIED
                        data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE desafios (
                          id_desafio INT AUTO_INCREMENT PRIMARY KEY,
                          id_empresa INT NOT NULL,
                          titulo VARCHAR(255) NOT NULL,
                          posicao_atual TEXT NOT NULL,
                          processo_atual TEXT NOT NULL,
                          problemas_encontrados TEXT NOT NULL,
                          impacto_negocio TEXT NOT NULL,
                          o_que_facilitar TEXT NOT NULL,
                          status ENUM('Aberto', 'EmRevisao', 'Fechado') DEFAULT 'Aberto',
                          data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
                              ON DELETE CASCADE
);

CREATE TABLE anexos_desafio (
                                id_anexo INT AUTO_INCREMENT PRIMARY KEY,
                                id_desafio INT NOT NULL,
                                url_arquivo VARCHAR(500) NOT NULL,
                                tipo_arquivo ENUM('Imagem', 'Video') NOT NULL,
                                FOREIGN KEY (id_desafio) REFERENCES desafios(id_desafio)
                                    ON DELETE CASCADE
);

CREATE TABLE pitches (
                         id_pitch INT AUTO_INCREMENT PRIMARY KEY,
                         id_desafio INT NOT NULL,
                         id_aluno INT NOT NULL,
                         url_video_pitch VARCHAR(500) NOT NULL,
                         data_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         status_pitch ENUM('Enviado', 'Vencedor', 'Destaque') DEFAULT 'Enviado',
                         FOREIGN KEY (id_desafio) REFERENCES desafios(id_desafio),
                         FOREIGN KEY (id_aluno) REFERENCES alunos(id_aluno),
                         UNIQUE KEY uq_aluno_desafio (id_aluno, id_desafio)
);

-- TRUNCATES

SET FOREIGN_KEY_CHECKS = 0; -- desativa
TRUNCATE TABLE empresas;
TRUNCATE TABLE alunos;
TRUNCATE TABLE desafios;
TRUNCATE TABLE anexos_desafio;
TRUNCATE TABLE pitches;
SET FOREIGN_KEY_CHECKS = 1; -- ativa

-- os comandos SET FKC servem para permitir que o truncate seja executado mesmo quando as tabelas são referenciadas em uma FK

-- INSERTS TESTE

INSERT INTO empresas (nome_empresa, cnpj, email_contato, setor, cidade, senha_hash) VALUES -- MODIFIED
                                                                                           ('Café do Bairro MEI', '11.222.333/0001-44', 'contato@cafedobairro.com.br', 'Alimentação', 'São Paulo', 'fake_hash_inova_mei'),
                                                                                           ('Tecno Soluções Digitais', '55.666.777/0001-88', 'suporte@tecnosolucoes.net', 'TI/Software', 'Curitiba', 'fake_hash_inova_mei'),
                                                                                           ('Artesã Kids Criativo', '99.000.111/0001-22', 'vendas@artesakids.com', 'Varejo/Artesanato', 'Rio de Janeiro', 'fake_hash_inova_mei');

INSERT INTO alunos (nome_completo, email_institucional, curso, semestre, habilidades, url_comprovante, foto_perfil_url, senha_hash) VALUES -- MODIFIED
                                                                                                                                           ('Lucas Pereira', 'lucas.pereira@aluno.edu.br', 'Análise e Des. de Sistemas', 5, 'Python, SQL, UI/UX, Power BI', 'uploads/comprovantes/lucas_doc.pdf', 'uploads/fotos/lucas_foto.jpg', 'fake_hash_inova_mei'),
                                                                                                                                           ('Sofia Mendes', 'sofia.mendes@aluno.edu.br', 'Marketing Digital', 3, 'SEO, Copywriting, Google Ads, Análise de Dados', 'uploads/comprovantes/sofia_doc.pdf', 'uploads/fotos/sofia_foto.jpg', 'fake_hash_inova_mei'),
                                                                                                                                           ('Rafael Costa', 'rafael.costa@aluno.edu.br', 'Engenharia de Produção', 7, 'Logística, Mapeamento de Processos, Lean Manufacturing', 'uploads/comprovantes/rafael_doc.pdf', 'uploads/fotos/rafael_foto.jpg', 'fake_hash_inova_mei');

INSERT INTO desafios (id_empresa, titulo, posicao_atual, processo_atual, problemas_encontrados, impacto_negocio, o_que_facilitar, status) VALUES
                                                                                                                                              (1, 'Otimização de Estoque para Cafeteria', 'Atualmente, controlamos o estoque manualmente em planilhas.', 'O barista verifica o estoque no fim do dia e preenche a planilha. O dono confere na manhã seguinte.', 'Altos índices de perdas por validade (cerca de 15% do café) e falta de insumos no pico de vendas.', 'Redução de 15% do lucro mensal devido a perdas e oportunidades perdidas.', 'Um sistema simples, talvez via app, que automatize o inventário e emita alertas de validade.', 'Aberto'),
                                                                                                                                              (2, 'Portal de Suporte ao Cliente em PHP', 'Usamos apenas e-mail e telefone para suporte, o que não escala.', 'O cliente envia um e-mail. Um técnico responde manualmente. Não há histórico organizado.', 'Demora na resposta, frustração do cliente e dificuldade em priorizar tickets urgentes.', 'Perda de clientes por insatisfação e sobrecarga da equipe de suporte.', 'Um portal web onde o cliente possa abrir um chamado, ver o status e acessar um FAQ.', 'Aberto'),
                                                                                                                                              (3, 'Solução de Embalagem para Produtos Frágeis', 'Nossos kits de artesanato são frágeis e quebram muito no transporte.', 'Usamos plástico bolha e caixa de papelão padrão, mas a movimentação dos correios é brutal.', 'Cerca de 10% dos pedidos chegam danificados, gerando custos de reenvio e reclamações.', 'Aumento de 20% nos custos logísticos (reenvio) e dano à reputação da marca.', 'Um novo design de embalagem ou um processo de empacotamento que garanta a integridade do produto.', 'EmRevisao');

INSERT INTO anexos_desafio (id_desafio, url_arquivo, tipo_arquivo) VALUES
                                                                       (1, 'uploads/desafio/estoque_cafe_planta.png', 'Imagem'),
                                                                       (2, 'uploads/desafio/video_suporte_atual.mp4', 'Video'),
                                                                       (3, 'uploads/desafio/foto_embalagem_atual.jpg', 'Imagem');

INSERT INTO pitches (id_desafio, id_aluno, url_video_pitch, status_pitch) VALUES
                                                                              (1, 1, 'uploads/pitches/pitch_lucas_vencedor.mp4', 'Vencedor'),
                                                                              (3, 2, 'uploads/pitches/pitch_sofia_destaque.mp4', 'Destaque'),
                                                                              (2, 3, 'uploads/pitches/pitch_rafael_enviado.mp4', 'Enviado');

-- SELECTS TESTE

SELECT * FROM empresas;
SELECT * FROM alunos;
SELECT * FROM desafios;
SELECT * FROM anexos_desafio;
SELECT * FROM pitches;