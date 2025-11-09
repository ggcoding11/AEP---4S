document.addEventListener("DOMContentLoaded", () => {
  // 1. Encontra os elementos
  const formCriacao = document.getElementById("form-criacao-desafio");
  // Adiciona os elementos do botão para controlar o spinner
  const submitButton = document.getElementById("btn-submit");
  const buttonSpinner = document.getElementById("btn-spinner");
  const buttonText = document.getElementById("btn-text");
  const formContainer = document.querySelector(".container.mt-4 .row");

  // --- 2. Verificação de Autenticação: Apenas Empresas ---
  const usuarioLogado = JSON.parse(localStorage.getItem("usuario"));

  if (!usuarioLogado || usuarioLogado.tipo !== "empresa") {
    // Se não for empresa logada, mostra mensagem de acesso negado
    if (formContainer) {
      formContainer.innerHTML =
        "<div class='alert alert-danger mx-auto w-75'>Acesso Negado. Apenas **Empresas** logadas podem criar desafios. Faça login como empresa para continuar.</div>";
    }
    return;
  }

  // O ID da empresa logada que será usado no POST
  const id_empresa_logada = usuarioLogado.id;

  // --- 3. Listener de Submissão ---
  if (formCriacao) {
    formCriacao.addEventListener("submit", async (e) => {
      e.preventDefault();

      // Validação do Bootstrap (se houver campos inválidos)
      if (!formCriacao.checkValidity()) {
        e.stopPropagation();
        formCriacao.classList.add("was-validated");
        return;
      }

      // Ativa o feedback de carregamento
      submitButton.disabled = true;
      buttonSpinner.classList.remove("d-none");
      buttonText.textContent = "Enviando...";

      // Captura de todos os campos (USANDO OS IDs CORRETOS DO HTML)
      const titulo = document.getElementById("titulo-desafio").value.trim(); // ID CORRIGIDO
      const posicao = document.getElementById("minha-posicao").value.trim();
      const processo = document.getElementById("processo-negocio").value.trim();
      const problemas = document
        .getElementById("problemas-encontrados")
        .value.trim();
      const impacto = document.getElementById("impacto-negocio").value.trim();
      const facilitado = document.getElementById("facilitado").value.trim();

      // Monta a DESCRIÇÃO LONGA concatenando todos os campos
      const descricaoCompleta = `
                **1. Minha Posição:**\n${posicao}\n
                **2. Processo Atual:**\n${processo}\n
                **3. Problemas Encontrados:**\n${problemas}\n
                **4. Impacto no Negócio:**\n${impacto}\n
                **5. O que Facilitar:**\n${facilitado}
            `.trim();

      const dados = {
        titulo: titulo,
        descricao: descricaoCompleta, // ENVIANDO A DESCRIÇÃO CONCATENADA
        id_empresa: id_empresa_logada,
      };

      try {
        const response = await fetch("http://localhost:8090/desafios/create", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(dados),
        });

        const result = await response.json();

        if (response.ok && result.success) {
          alert(
            "🎉 Desafio criado com sucesso! ID: " + result.desafio.id_desafio
          );
          window.location.href = "home.html";
        } else {
          const erroMsg = result.error
            ? result.error.message
            : "Erro desconhecido.";
          alert("Falha na criação: " + erroMsg);
          console.error("Erro detalhado:", result);
        }
      } catch (error) {
        console.error("Erro de conexão:", error);
        alert(
          "Erro ao conectar com o servidor. Verifique se o backend está ativo."
        );
      } finally {
        // Reseta o estado do botão
        submitButton.disabled = false;
        buttonSpinner.classList.add("d-none");
        buttonText.textContent = "Enviar";
      }
    });
  }

  // --- SCRIPT DO CONTADOR DE CARACTERES (AGORA CORRETAMENTE INICIALIZADO) ---
  const textareas = document.querySelectorAll("textarea[data-counter-id]");

  textareas.forEach((textarea) => {
    const counter = document.getElementById(textarea.dataset.counterId);
    // Garante que minlength é um número
    const minLength = parseInt(textarea.getAttribute("minlength"), 10);

    const updateCounter = () => {
      const currentLength = textarea.value.length;
      counter.textContent = currentLength;

      // Muda a cor do contador se atingir o mínimo
      if (currentLength >= minLength) {
        counter.classList.add("text-success");
        counter.classList.remove("text-danger");
      } else {
        counter.classList.remove("text-success");
        counter.classList.add("text-danger"); // Ajuda a destacar se o mínimo não foi atingido
      }
    };

    // Ativa a contagem a cada digitação
    textarea.addEventListener("input", updateCounter);

    // Inicializa o contador (necessário para que ele não fique '0' parado)
    updateCounter();
  });
});
