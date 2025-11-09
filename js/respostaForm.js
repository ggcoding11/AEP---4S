// Roda quando o HTML (DOM) estiver pronto
document.addEventListener("DOMContentLoaded", () => {
  // 1. Pega o ID da URL
  const urlParams = new URLSearchParams(window.location.search);
  const desafioId = urlParams.get("id");

  // 2. Encontra os elementos
  const desafioTitulo = document.getElementById("desafio-titulo");
  const desafioEmpresa = document.getElementById("desafio-empresa");
  const desafioDescricao = document.getElementById("desafio-descricao");
  const loadingSpinner = document.getElementById("loading-spinner");
  const formPitch = document.getElementById("form-pitch");
  const formColumn = document.querySelector(".col-lg-4");

  if (!desafioId) {
    document.getElementById("desafio-container").innerHTML =
      "<p class='text-danger'>Erro: Desafio não especificado.</p>";
    return;
  }

  // --- LÓGICA DE CARREGAMENTO DOS DETALHES ---
  async function carregarDetalhesDesafio() {
    try {
      const response = await fetch(
        `http://localhost:8090/desafios/${desafioId}`
      );

      // Ação: Esconde o spinner de "carregando"
      if (loadingSpinner) loadingSpinner.classList.add("d-none");

      if (!response.ok) {
        // Se der erro (ex: 404 - Não Encontrado)
        document.getElementById("desafio-container").innerHTML =
          "<p class='text-danger'>Erro ao carregar o desafio. Verifique se o ID é válido.</p>";
        return;
      }

      const data = await response.json();

      if (data.success && data.desafio) {
        const desafio = data.desafio;

        desafioTitulo.textContent = desafio.titulo;
        desafioEmpresa.textContent = `Proposto por: ${desafio.nomeEmpresa}`;
        desafioDescricao.textContent = desafio.descricao;
      } else {
        throw new Error("Resposta inesperada do servidor.");
      }
    } catch (error) {
      console.error("Erro ao buscar detalhes do desafio:", error);
      if (loadingSpinner) loadingSpinner.classList.add("d-none");
      document.getElementById("desafio-container").innerHTML =
        "<p class='text-danger'>Erro ao conectar com o servidor. Verifique se o MySQL e o Java estão ativos.</p>";
    }
  }

  // --- LÓGICA DE AUTENTICAÇÃO E ENVIO DO PITCH ---
  const usuarioLogado = JSON.parse(localStorage.getItem("usuario"));
  const isAluno = usuarioLogado && usuarioLogado.tipo === "aluno";

  if (isAluno) {
    // O aluno está logado, adicione o listener de submissão
    formPitch.addEventListener("submit", async (e) => {
      e.preventDefault();

      const urlVideoPitch = document.getElementById("videoPitch").value.trim();

      if (!urlVideoPitch) {
        alert("Por favor, forneça o link do vídeo do Pitch.");
        return;
      }

      const dadosPitch = {
        desafioId: parseInt(desafioId), // Antes: id_desafio
        alunoId: parseInt(usuarioLogado.id), // Antes: id_aluno (e garante que é número)
        urlVideoPitch: urlVideoPitch, // Antes: url_video_pitch
      };

      try {
        const response = await fetch("http://localhost:8090/pitches/create", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(dadosPitch),
        });

        const result = await response.json();

        if (response.ok && result.success) {
          alert("Pitch enviado com sucesso! A empresa será notificada.");
          formPitch.querySelector('button[type="submit"]').textContent =
            "Pitch Enviado!";
          formPitch.querySelector('button[type="submit"]').disabled = true;
        } else {
          const erroMsg = result.error
            ? result.error.message
            : "Erro desconhecido.";
          alert("Falha no envio do Pitch: " + erroMsg);
          console.error("Erro detalhado:", result);
        }
      } catch (error) {
        console.error("Erro de conexão no Pitch:", error);
        alert("Erro ao conectar com o servidor para enviar o Pitch.");
      }
    });
  } else {
    // Não é aluno logado (inclui deslogado ou empresa)
    if (formColumn) {
      let msg =
        "Acesso restrito. Faça login como **Aluno** para enviar uma solução.";
      if (usuarioLogado && usuarioLogado.tipo === "empresa") {
        msg =
          "Apenas Alunos podem enviar soluções. Você está logado como **Empresa**.";
      }
      formColumn.innerHTML = `<div class='alert alert-warning'>${msg}</div>`;
    }
  }

  // 8. Chama a função para carregar tudo (no final)
  carregarDetalhesDesafio();
});
