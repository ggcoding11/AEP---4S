// Roda quando o HTML (DOM) estiver pronto
document.addEventListener("DOMContentLoaded", () => {
  // 1. Pega os "parâmetros" da URL (ex: ?id=1)
  const urlParams = new URLSearchParams(window.location.search);
  const desafioId = urlParams.get("id"); // Pega o valor do 'id'

  // 2. Se não houver ID na URL, avisa o usuário e para
  if (!desafioId) {
    document.getElementById("desafio-container").innerHTML =
      "<p class='text-danger'>Erro: Desafio não especificado.</p>";
    return;
  }

  // 3. Encontra os lugares no HTML onde vamos injetar os dados
  const desafioTitulo = document.getElementById("desafio-titulo");
  const desafioEmpresa = document.getElementById("desafio-empresa");
  const desafioDescricao = document.getElementById("desafio-descricao");
  const loadingSpinner = document.getElementById("loading-spinner");

  // 4. Função para buscar os dados do desafio específico
  async function carregarDetalhesDesafio() {
    try {
      // 5. Faz o fetch para o backend usando o ID (ex: /desafios/1)
      const response = await fetch(
        `http://localhost:4567/desafios/${desafioId}`
      );

      // 6. Esconde o spinner de "carregando"
      loadingSpinner.classList.add("d-none");

      if (!response.ok) {
        // Se der erro (ex: 404 - Não Encontrado)
        document.getElementById("desafio-container").innerHTML =
          "<p class='text-danger'>Erro ao carregar o desafio. Verifique se o ID é válido.</p>";
        return;
      }

      const data = await response.json();

      // 7. Preenche o HTML com os dados recebidos
      if (data.success && data.desafio) {
        const desafio = data.desafio;

        // (Usamos .titulo, .nomeEmpresa, .descricao,
        // que são os nomes do Desafio.java / DesafioDAO.java)
        desafioTitulo.textContent = desafio.titulo;
        desafioEmpresa.textContent = `Proposto por: ${desafio.nomeEmpresa}`;
        desafioDescricao.textContent = desafio.descricao;
      } else {
        throw new Error("Resposta inesperada do servidor.");
      }
    } catch (error) {
      console.error("Erro ao buscar detalhes do desafio:", error);
      loadingSpinner.classList.add("d-none");
      document.getElementById("desafio-container").innerHTML =
        "<p class='text-danger'>Erro ao conectar com o servidor.</p>";
    }
  }

  const formPitch = document.getElementById("form-pitch");
  // Encontra a coluna do formulário para escondê-la se a pessoa não for Aluno
  const formColumn = document.querySelector(".col-lg-4");

  // Tenta obter o usuário logado
  const usuarioLogado = JSON.parse(localStorage.getItem("usuario"));

  if (!usuarioLogado || usuarioLogado.tipo !== "aluno") {
    // Se não houver login OU se for uma empresa
    if (formColumn) {
      formColumn.innerHTML =
        "<div class='alert alert-warning'>Faça login como **Aluno** para enviar uma solução.</div>";
    }
    return;
  }

  // O aluno está logado, adicione o listener de submissão
  formPitch.addEventListener("submit", async (e) => {
    e.preventDefault(); // Impede o envio padrão

    // Pega os valores
    const urlVideoPitch = document.getElementById("videoPitch").value.trim();
    // Nota: O campo 'descPitch' que você colocou não está na model de Pitch,
    // mas é bom para um futuro DTO, por enquanto vamos ignorar.

    if (!urlVideoPitch) {
      alert("Por favor, forneça o link do vídeo do Pitch.");
      return;
    }

    const dadosPitch = {
      id_desafio: parseInt(desafioId),
      id_aluno: usuarioLogado.id, // ID do aluno logado
      url_video_pitch: urlVideoPitch,
      // status_pitch é 'Enviado' por default no backend
    };

    try {
      // Faremos um POST para um novo endpoint que vamos criar: /pitches/create
      const response = await fetch("http://localhost:4567/pitches/create", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(dadosPitch),
      });

      const result = await response.json();

      if (response.ok && result.success) {
        alert("Pitch enviado com sucesso! A empresa será notificada.");
        // Impede envios duplicados, desabilitando o formulário
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
      console.error("Erro de conexão:", error);
      alert(
        "Erro ao conectar com o servidor. Verifique se o backend está ativo."
      );
    }
  });

  // 8. Chama a função para carregar tudo
  carregarDetalhesDesafio();
});
