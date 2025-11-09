// Roda quando o HTML (DOM) estiver pronto
document.addEventListener("DOMContentLoaded", () => {
  const params = new URLSearchParams(window.location.search);
  const desafioId = params.get("id");

  const desafioTituloEl = document.getElementById("desafio-titulo");
  const desafioDescricaoEl = document.getElementById("desafio-descricao");
  const desafioEmpresaEl = document.getElementById("desafio-empresa");
  const desafioStatusEl = document.getElementById("desafio-status");
  const pitchesContainer = document.getElementById("pitches-container");
  const pitchCountEl = document.getElementById("pitch-count");

  const loadingMessage = document.getElementById("loading-message"); // --- 1. VERIFICAÇÃO DE ID E AUTENTICAÇÃO ---

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

  const empresaLogadaId = parseInt(usuarioLogado.id); // Garante que é número // --- 2. FUNÇÕES DE BUSCA ---

  const fetchDesafioDetails = async () => {
    try {
      const response = await fetch(
        `http://localhost:4567/desafios/${desafioId}`
      );
      if (!response.ok) throw new Error("Desafio não encontrado");

      const result = await response.json();
      const desafio = result.desafio; // CORREÇÃO: Usar desafio.empresaId (camelCase)

      if (desafio.empresaId !== empresaLogadaId) {
        desafioTituloEl.textContent = "Acesso Negado.";
        loadingMessage.innerHTML =
          "<p class='text-center text-danger'>Este desafio pertence a outra empresa.</p>";
        return null;
      }

      desafioTituloEl.textContent = desafio.titulo;
      desafioDescricaoEl.innerHTML = formatDescription(desafio.descricao);
      desafioEmpresaEl.textContent = `Postado por: ${desafio.nomeEmpresa}`; // CORREÇÃO: Usar desafio.statusDesafio (camelCase)
      const status = desafio.statusDesafio || "Pendente";
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
      pitchCountEl.textContent = pitches.length; // Verifica se o desafio já foi concluído para desabilitar botões

      const desafioStatus = desafioStatusEl.textContent;
      const isConcluido =
        desafioStatus === "Concluído" || // CORREÇÃO: Usar pitch.statusPitch (camelCase)
        pitches.some((p) => p.statusPitch === "Vencedor");

      if (pitches.length === 0) {
        pitchesContainer.innerHTML = `<div class="col-12"><p class="text-center text-muted">Nenhum pitch enviado para este desafio ainda.</p></div>`;
      } else {
        pitches.forEach((pitch) => renderPitchCard(pitch, isConcluido));
      }
    } catch (error) {
      pitchesContainer.innerHTML = `<div class="col-12"><p class='text-center text-danger'>Erro ao buscar pitches: ${error.message}</p></div>`;
      console.error("Erro ao buscar pitches:", error);
    }
  }; // --- 3. FUNÇÃO DE SELEÇÃO DE VENCEDOR (NOVO) ---

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
      // O Back-end espera snake_case nos DTOs de PUT/POST, então mantemos:
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
        alert("Vencedor selecionado e Desafio marcado como Concluído! 🎉"); // Recarrega TUDO para atualizar status e remover botões
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
  }; // --- 4. FUNÇÕES DE RENDERIZAÇÃO ---

  const formatDescription = (description) => {
    if (!description) return "<p>Nenhuma descrição fornecida.</p>";
    let formatted = description.replace(/\n/g, "<br>");
    formatted = formatted.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");
    return formatted;
  };

  // Auxiliar para gerar as iniciais para o avatar placeholder
  const getInitials = (name) => {
    if (!name) return "??";
    const parts = name.split(" ");
    if (parts.length > 1) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return parts[0][0].toUpperCase();
  };

  const renderPitchCard = (pitch, isConcluido) => {
    // Verificações
    const isWinner = pitch.statusPitch === "Vencedor";
    const statusClass = isWinner ? "bg-success" : "bg-secondary";

    // Status Badge
    const statusBadge = `<span class="badge ${statusClass} me-2 fw-bold">${pitch.statusPitch}</span>`;

    // Botão de Seleção (Se aplicável)
    const selectButton =
      !isConcluido && !isWinner
        ? `<button class="btn btn-sm btn-primary btn-select-pitch" data-pitch-id="${pitch.id}">Selecionar Vencedor</button>`
        : "";

    // Novo: Avatar Placeholder (usando as iniciais)
    const initials = getInitials(pitch.alunoNome);

    // NOVO HTML: Mais visual e com destaque para o status/vencedor
    const cardHtml = `
        <div class="col-md-6 col-lg-4 mb-4">
            <div class="card h-100 ${
              isWinner
                ? "border-success border-3 winner-card shadow-lg"
                : "shadow-sm"
            }">
                <div class="card-body d-flex flex-column">
                    
                    <div class="d-flex align-items-center mb-3">
                        <div class="avatar-placeholder me-3">
                            ${initials}
                        </div>
                        <div>
                            <h5 class="card-title fw-bold mb-0">${
                              pitch.alunoNome || "Aluno Desconhecido"
                            }</h5>
                            <p class="card-text text-muted mb-0 small">ID Pitch: ${
                              pitch.id
                            } | Aluno: ${pitch.alunoId}</p>
                        </div>
                    </div>
                    
                    <hr class="mt-0">
                    
                    <p class="card-text mb-1">
                        <i class="bi bi-book me-2 text-primary"></i> ${
                          pitch.curso || "Curso não informado"
                        }
                    </p>
                    <p class="card-text mb-3">
                        <i class="bi bi-calendar-check me-2 text-primary"></i> Semestre: ${
                          pitch.semestre || "-"
                        }
                    </p>
                    
                    <div class="mt-auto pt-3">
                        <a href="${
                          pitch.urlVideoPitch
                        }" target="_blank" class="btn btn-danger btn-block w-100 mb-2 btn-video-pitch">
                            <i class="bi bi-play-btn-fill me-1"></i> ASSISTIR VÍDEO PITCH
                        </a>
                        <div class="d-flex justify-content-between align-items-center mt-2">
                            <div>${statusBadge}</div>
                            ${selectButton}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
    pitchesContainer.insertAdjacentHTML("beforeend", cardHtml);
  }; // --- 5. EXECUÇÃO PRINCIPAL E EVENT LISTENER --- // ... (Código de execução e listener mantido)

  const init = async () => {
    loadingMessage.innerHTML =
      "<p class='text-center text-muted'>Carregando detalhes do desafio...</p>";
    const desafio = await fetchDesafioDetails();
    if (desafio) {
      loadingMessage.innerHTML =
        "<p class='text-center text-muted'>Carregando pitches...</p>";
      await fetchPitches();
      loadingMessage.innerHTML = ""; // Remove a mensagem de loading
    }
  };

  pitchesContainer.addEventListener("click", (e) => {
    if (e.target.classList.contains("btn-select-pitch")) {
      const pitchId = e.target.dataset.pitchId;
      selectWinner(pitchId);
    }
  });

  init();
});
