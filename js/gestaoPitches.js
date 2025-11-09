document.addEventListener("DOMContentLoaded", () => {
  const params = new URLSearchParams(window.location.search);
  const desafioId = params.get("id");

  const desafioTituloEl = document.getElementById("desafio-titulo");
  const desafioDescricaoEl = document.getElementById("desafio-descricao");
  const desafioEmpresaEl = document.getElementById("desafio-empresa");
  const desafioStatusEl = document.getElementById("desafio-status");
  const pitchesContainer = document.getElementById("pitches-container");
  const pitchCountEl = document.getElementById("pitch-count");

  const loadingMessage = document.getElementById("loading-message");

  // --- 1. VERIFICAÇÃO DE ID E AUTENTICAÇÃO ---
  const usuarioLogado = JSON.parse(localStorage.getItem("usuario"));

  if (!desafioId) {
    desafioTituloEl.textContent = "Erro: ID do Desafio não encontrado na URL.";
    loadingMessage.innerHTML =
      "<p class='text-center text-danger'>Você deve acessar esta página a partir de um desafio válido.</p>";
    return;
  }

  if (!usuarioLogado || usuarioLogado.tipo !== "empresa") {
    desafioTituloEl.textContent = "Acesso Negado.";
    loadingMessage.innerHTML =
      "<p class='text-center text-danger'>Apenas **Empresas** logadas podem gerenciar pitches.</p>";
    return;
  }

  const empresaLogadaId = usuarioLogado.id;

  // --- 2. FUNÇÕES DE BUSCA ---

  const fetchDesafioDetails = async () => {
    try {
      const response = await fetch(
        `http://localhost:4567/desafios/${desafioId}`
      );
      if (!response.ok) throw new Error("Desafio não encontrado");

      const result = await response.json();
      const desafio = result.desafio;

      if (desafio.id_empresa !== empresaLogadaId) {
        desafioTituloEl.textContent = "Acesso Negado.";
        loadingMessage.innerHTML =
          "<p class='text-center text-danger'>Este desafio pertence a outra empresa.</p>";
        return null;
      }

      desafioTituloEl.textContent = desafio.titulo;
      desafioDescricaoEl.innerHTML = formatDescription(desafio.descricao);
      desafioEmpresaEl.textContent = `Postado por: ${desafio.nomeEmpresa}`;
      // Atualiza o status do desafio na página principal de gestão
      const status = desafio.status_desafio || "Pendente";
      desafioStatusEl.textContent = status;
      desafioStatusEl.className = `badge ${
        status === "Concluído" ? "bg-success" : "bg-warning"
      }`;

      return desafio;
    } catch (error) {
      desafioTituloEl.textContent = "Erro ao carregar desafio";
      loadingMessage.innerHTML = `<p class='text-center text-danger'>${error.message}.</p>`;
      console.error("Erro ao buscar detalhes do desafio:", error);
      return null;
    }
  };

  const fetchPitches = async () => {
    try {
      const response = await fetch(
        `http://localhost:4567/desafios/pitches?id=${desafioId}`
      );
      if (!response.ok) throw new Error("Falha ao carregar pitches");

      const result = await response.json();
      const pitches = result.pitches || [];

      pitchesContainer.innerHTML = "";
      pitchCountEl.textContent = pitches.length;

      // Verifica se o desafio já foi concluído para desabilitar botões
      const desafioStatus = desafioStatusEl.textContent;
      const isConcluido =
        desafioStatus === "Concluído" ||
        pitches.some((p) => p.status_pitch === "Vencedor");

      if (pitches.length === 0) {
        pitchesContainer.innerHTML = `<div class="col-12"><p class="text-center text-muted">Nenhum pitch enviado para este desafio ainda.</p></div>`;
      } else {
        pitches.forEach((pitch) => renderPitchCard(pitch, isConcluido));
      }
    } catch (error) {
      pitchesContainer.innerHTML = `<div class="col-12"><p class='text-center text-danger'>Erro ao buscar pitches: ${error.message}</p></div>`;
      console.error("Erro ao buscar pitches:", error);
    }
  };

  // --- 3. FUNÇÃO DE SELEÇÃO DE VENCEDOR (NOVO) ---
  const selectWinner = async (pitchId) => {
    if (
      !confirm(
        "Tem certeza que deseja selecionar este Pitch como o VENCEDOR? Esta ação marcará o desafio como CONCLUÍDO."
      )
    ) {
      return;
    }

    const button = document.querySelector(`[data-pitch-id="${pitchId}"]`);
    button.disabled = true;
    button.textContent = "Processando...";

    const data = {
      id_pitch: parseInt(pitchId),
      id_desafio: parseInt(desafioId),
    };

    try {
      // Chama o novo endpoint PUT no Backend
      const response = await fetch("http://localhost:4567/pitches/vencedor", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      const result = await response.json();

      if (response.ok && result.success) {
        alert("Vencedor selecionado e Desafio marcado como Concluído! 🎉");
        // Recarrega TUDO para atualizar status e remover botões
        await fetchDesafioDetails();
        await fetchPitches();
      } else {
        const errorMsg = result.error
          ? result.error.message
          : "Erro desconhecido.";
        alert("Falha ao selecionar vencedor: " + errorMsg);
        button.disabled = false;
        button.textContent = "Selecionar como Vencedor";
      }
    } catch (error) {
      console.error("Erro de conexão:", error);
      alert(
        "Erro ao conectar com o servidor. Verifique se o backend está ativo."
      );
      button.disabled = false;
      button.textContent = "Selecionar como Vencedor";
    }
  };

  // --- 4. FUNÇÕES DE RENDERIZAÇÃO ---

  const formatDescription = (description) => {
    if (!description) return "<p>Nenhuma descrição fornecida.</p>";
    let formatted = description.replace(/\n/g, "<br>");
    formatted = formatted.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");
    return formatted;
  };

  const renderPitchCard = (pitch, isConcluido) => {
    const isWinner = pitch.status_pitch === "Vencedor";
    const statusClass = isWinner ? "bg-success" : "bg-secondary";
    const statusBadge = `<span class="badge ${statusClass} me-2">${pitch.status_pitch}</span>`;

    // O botão aparece SE NÃO ESTIVER CONCLUÍDO E SE O PITCH AINDA NÃO FOR O VENCEDOR
    const selectButton = !isConcluido
      ? `<button class="btn btn-sm btn-primary btn-select-pitch" data-pitch-id="${pitch.id_pitch}">Selecionar como Vencedor</button>`
      : "";

    const cardHtml = `
            <div class="col-md-6 col-lg-4">
                <div class="card h-100 shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title fw-semibold">${
                          pitch.alunoNome || "Aluno Desconhecido"
                        }</h5>
                        <p class="card-text text-muted">
                            ${
                              pitch.curso || "Curso não informado"
                            } - Semestre: ${pitch.semestre || "-"}
                        </p>
                        <hr>
                        <a href="${
                          pitch.urlVideoPitch
                        }" target="_blank" class="btn btn-outline-danger btn-sm mb-3">
                            <i class="bi bi-play-circle me-1"></i> Assistir Pitch
                        </a>
                        <div class="d-flex justify-content-between align-items-center">
                            <div>${statusBadge}</div>
                            ${selectButton}
                        </div>
                    </div>
                </div>
            </div>
        `;
    pitchesContainer.insertAdjacentHTML("beforeend", cardHtml);
  };

  // --- 5. EXECUÇÃO PRINCIPAL E EVENT LISTENER ---

  const init = async () => {
    const desafio = await fetchDesafioDetails();
    if (desafio) {
      await fetchPitches();
    }
  };

  // Adiciona o Event Listener Delegado para os botões de seleção
  pitchesContainer.addEventListener("click", (e) => {
    if (e.target.classList.contains("btn-select-pitch")) {
      const pitchId = e.target.dataset.pitchId;
      selectWinner(pitchId);
    }
  });

  init();
});
