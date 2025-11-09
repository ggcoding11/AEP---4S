// Roda quando o HTML (DOM) estiver pronto
document.addEventListener("DOMContentLoaded", () => {
  // 1. Encontra o container onde os cards dos desafios devem entrar
  // (Vamos adicionar este ID no .html no próximo passo)
  const desafiosContainer = document.getElementById("desafios-container");

  // 2. Função assíncrona para buscar os desafios
  // --- FUNÇÃO CORRIGIDA ---
  async function carregarDesafios() {
    if (!desafiosContainer) return; // Se não achar o container, para

    // Limpa os cards estáticos ou de carregamento inicial
    desafiosContainer.innerHTML = "";

    try {
      // 3. Faz a "ligação" (fetch) para o backend
      const response = await fetch("http://localhost:4567/desafios");

      if (!response.ok) {
        throw new Error("Não foi possível carregar os desafios.");
      }

      const data = await response.json();

      // 4. Se houver sucesso e desafios, RENDERIZA CADA UM.
      if (data.success && data.desafios.length > 0) {
        // CORREÇÃO AQUI: Em vez de construir o HTML, chame a função de renderização
        data.desafios.forEach((desafio) => {
          renderDesafioCard(desafio);
        });
      } else {
        // Se não houver desafios no banco
        desafiosContainer.innerHTML =
          "<p>Nenhum desafio cadastrado no momento.</p>";
      }
    } catch (error) {
      console.error("Erro ao buscar desafios:", error);
      desafiosContainer.innerHTML =
        "<p class='text-danger'>Erro ao carregar desafios. Verifique o servidor.</p>";
    }
  }

  // Adicione esta função auxiliar ao seu js/home.js

  const formatDescription = (description) => {
    if (!description) return "Nenhuma descrição detalhada fornecida.";
    // Converte quebras de linha em <br>
    let formatted = description.replace(/\n/g, "<br>");
    // Formata os títulos em negrito (**Titulo**) para <strong>
    formatted = formatted.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");
    return formatted;
  };

  const renderDesafioCard = (desafio) => {
    // Pega o ID da empresa logada (se existir)
    const usuarioLogado = JSON.parse(localStorage.getItem("usuario"));

    const idDesafioEmpresa = parseInt(desafio.empresaId);

    const empresaLogadaId =
      usuarioLogado && usuarioLogado.tipo === "empresa"
        ? parseInt(usuarioLogado.id)
        : null;

    const isEmpresaDona = idDesafioEmpresa === empresaLogadaId;

    // Converte a descrição para um resumo
    const resumo =
      formatDescription(desafio.descricao).substring(0, 150) + "...";

    // Adiciona o botão de gerenciamento/resposta
    let actionButton = "";

    if (isEmpresaDona) {
      // Se for a Empresa Dona, mostra o botão de Gerenciar Pitches
      actionButton = `
            <a href="gestaoPitches.html?id=${desafio.id}" class="btn btn-warning w-100 mt-3">
                Gerenciar Pitches
            </a>
        `;
    } else {
      // Se for Aluno ou Anônimo, mostra o botão de Responder
      // Verifica se há ID logado, senão, o botão é para Login
      const linkHref = usuarioLogado
        ? `respostaForm.html?id=${desafio.id}` // <-- CORRIGIDO AQUI: desafio.id
        : "login.html";
      actionButton = `
            <a href="${linkHref}" class="btn btn-primary w-100 mt-3">
                Responder Desafio
            </a>
        `;
    }

    const cardHtml = `
        <div class="col-md-6 col-lg-4">
            <div class="card h-100 shadow-sm">
                <div class="card-body d-flex flex-column">
                    <h5 class="card-title fw-bold">${desafio.titulo}</h5>
                    <h6 class="card-subtitle mb-2 text-muted">Por: ${desafio.nomeEmpresa}</h6>
                    <div class="card-text mb-3 flex-grow-1">${resumo}</div>
                    ${actionButton}
                </div>
            </div>
        </div>
    `;

    document
      .getElementById("desafios-container")
      .insertAdjacentHTML("beforeend", cardHtml);
  };

  // 8. Chama a função para carregar tudo
  carregarDesafios();
});
