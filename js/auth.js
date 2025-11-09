document.addEventListener("DOMContentLoaded", () => {
  // 1. Tenta pegar os dados do usuário do LOCAL STORAGE (CORREÇÃO AQUI!)
  const usuarioString = localStorage.getItem("usuario");

  // 2. Encontra os elementos do menu no HTML
  const navAnonimo = document.getElementById("nav-anonimo");
  const navLogado = document.getElementById("nav-logado");
  const nomeUsuarioSpan = document.getElementById("nome-usuario-logado");
  const btnLogout = document.getElementById("btn-logout");

  // 3. Verifica se o usuário ESTÁ logado
  if (usuarioString && navLogado) {
    // Converte o texto JSON de volta para um objeto
    const usuario = JSON.parse(usuarioString);

    // O objeto 'usuario' agora tem os campos 'nome' e 'tipo' graças à correção no login.js
    const nomeCompleto = usuario.nome;

    // Atualiza o menu (Mostra só o primeiro nome)
    if (nomeUsuarioSpan && nomeCompleto) {
        nomeUsuarioSpan.textContent = `Olá, ${nomeCompleto.split(" ")[0]}`; 
    }
    
    // Esconde "Login/Cadastrar" e mostra "Olá, [Nome] / Sair"
    navAnonimo.classList.add("d-none");
    navLogado.classList.remove("d-none");

    // 4. Adiciona a função de Sair (Logout)
    if (btnLogout) {
      btnLogout.addEventListener("click", () => {
        // Limpa APENAS o localStorage (CORREÇÃO AQUI!)
        localStorage.removeItem("usuario");

        // Redireciona para o login
        alert("Você saiu da sua conta.");
        window.location.href = "login.html";
      });
    }
  } else {
    // 5. Se NÃO ESTÁ logado, faz o contrário
    if (navAnonimo) navAnonimo.classList.remove("d-none");
    if (navLogado) navLogado.classList.add("d-none");
  }

  // 6. Proteção de Página (Mantida)
  const paginaAtual = window.location.pathname.split("/").pop();
  const paginasPublicas = ["login.html", "cadastro.html", "home.html", ""];

  if (!usuarioString && !paginasPublicas.includes(paginaAtual)) {
    console.warn("Usuário não logado. Redirecionando para login.");
    window.location.href = "login.html";
  }
});