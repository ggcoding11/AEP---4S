document.addEventListener("DOMContentLoaded", () => {
  // 1. Tenta pegar os dados do usuário do LOCAL STORAGE
  const usuarioString = localStorage.getItem("usuario");

  // 2. Encontra os elementos do menu no HTML
  const navAnonimo = document.getElementById("nav-anonimo");
  const navLogado = document.getElementById("nav-logado");
  const nomeUsuarioSpan = document.getElementById("nome-usuario-logado");
  const btnLogout = document.getElementById("btn-logout");

  // NOVO: Encontra o botão de criar desafio (ID adicionado na home.html)
  const btnCriarDesafio = document.getElementById("btn-criar-desafio");

  // 3. Verifica se o usuário ESTÁ logado
  if (usuarioString && navLogado) {
    // Converte o texto JSON de volta para um objeto
    const usuario = JSON.parse(usuarioString);

    const nomeCompleto = usuario.nome;

    // Atualiza o menu (Mostra só o primeiro nome)
    if (nomeUsuarioSpan && nomeCompleto) {
      nomeUsuarioSpan.textContent = `Olá, ${nomeCompleto.split(" ")[0]}`;
    }

    // Esconde "Login/Cadastrar" e mostra "Olá, [Nome] / Sair"
    if (navAnonimo) navAnonimo.classList.add("d-none");
    if (navLogado) navLogado.classList.remove("d-none");

    // 4. LÓGICA CONDICIONAL: Mostrar botão 'Criar Desafio' apenas para EMPRESAS
    if (usuario.tipo === "empresa" && btnCriarDesafio) {
      btnCriarDesafio.classList.remove("d-none"); // Torna o botão visível
    } else if (btnCriarDesafio) {
      btnCriarDesafio.classList.add("d-none"); // Garante que esteja escondido para Aluno/Outro
    }

    // 5. Adiciona a função de Sair (Logout)
    if (btnLogout) {
      btnLogout.addEventListener("click", () => {
        // Limpa o localStorage
        localStorage.removeItem("usuario");

        alert("Você saiu da sua conta.");
        window.location.href = "login.html";
      });
    }
  } else {
    // 6. Se NÃO ESTÁ logado, faz o contrário
    if (navAnonimo) navAnonimo.classList.remove("d-none");
    if (navLogado) navLogado.classList.add("d-none");
  }

  // 7. Proteção de Página (Mantida)
  const paginaAtual = window.location.pathname.split("/").pop();
  const paginasPublicas = ["login.html", "cadastro.html", "home.html", ""];

  if (!usuarioString && !paginasPublicas.includes(paginaAtual)) {
    console.warn("Usuário não logado. Redirecionando para login.");
    window.location.href = "login.html";
  }
});
