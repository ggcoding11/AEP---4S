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

  // 8. Chama a função para carregar tudo
  carregarDetalhesDesafio();
});
