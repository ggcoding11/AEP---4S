document.addEventListener("DOMContentLoaded", () => {
  // 1. Tenta pegar os dados do usuário e o tipo do sessionStorage
  const usuarioString = sessionStorage.getItem("usuario");
  const tipoUsuario = sessionStorage.getItem("tipoUsuario");

  // 2. Encontra os elementos do menu no HTML
  const navAnonimo = document.getElementById("nav-anonimo");
  const navLogado = document.getElementById("nav-logado");
  const nomeUsuarioSpan = document.getElementById("nome-usuario-logado");
  const btnLogout = document.getElementById("btn-logout");

  // 3. Verifica se o usuário ESTÁ logado
  if (usuarioString && navLogado) {
    // Converte o texto JSON de volta para um objeto
    const usuario = JSON.parse(usuarioString);

    // Pega o nome (seja de aluno ou empresa)
    const nome =
      tipoUsuario === "aluno" ? usuario.nome_completo : usuario.nome_empresa;

    // Atualiza o menu
    nomeUsuarioSpan.textContent = `Olá, ${nome.split(" ")[0]}`; // Mostra só o primeiro nome

    // Esconde "Login/Cadastrar" e mostra "Olá, [Nome] / Sair"
    navAnonimo.classList.add("d-none");
    navLogado.classList.remove("d-none");

    // 4. Adiciona a função de Sair (Logout)
    if (btnLogout) {
      btnLogout.addEventListener("click", () => {
        // Limpa os dados do navegador
        sessionStorage.removeItem("usuario");
        sessionStorage.removeItem("tipoUsuario");

        // Redireciona para o login
        alert("Você saiu da sua conta.");
        window.location.href = "login.html";
      });
    }
  } else {
    // 5. Se NÃO ESTÁ logado, faz o contrário
    navAnonimo.classList.remove("d-none");
    navLogado.classList.add("d-none");
  }

  // 6. (Opcional) Proteção de Página
  // Se a página atual NÃO for login.html ou cadastro.html E o usuário NÃO estiver logado,
  // força o redirecionamento para o login.
  const paginaAtual = window.location.pathname.split("/").pop();
  const paginasPublicas = ["login.html", "cadastro.html", "home.html", ""]; // "" é a raiz, se aplicável

  if (!usuarioString && !paginasPublicas.includes(paginaAtual)) {
    console.warn("Usuário não logado. Redirecionando para login.");
    window.location.href = "login.html";
  }
});
