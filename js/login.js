document.addEventListener("DOMContentLoaded", () => {
  // IDs CORRIGIDOS para bater com login.html
  const formAluno = document.getElementById("login-form-aluno");
  const formEmpresa = document.getElementById("login-form-empresa");

  // --- LÓGICA PARA O LOGIN DO ALUNO ---
  if (formAluno) {
    formAluno.addEventListener("submit", async (event) => {
      event.preventDefault();
      const email = document.getElementById("input-email-aluno").value;
      const senha = document.getElementById("input-senha-aluno").value;
      const loginData = {
        email_institucional: email,
        senha: senha,
      };

      try {
        const response = await fetch("http://localhost:4567/login-aluno", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(loginData),
        });
        const data = await response.json();
        if (data.success) {
          alert("Login de aluno realizado com sucesso!");
          // Salva os dados do usuário no navegador
          sessionStorage.setItem("usuario", JSON.stringify(data.aluno));
          sessionStorage.setItem("tipoUsuario", "aluno");
          // Redireciona para a home
          window.location.href = "home.html";
        } else {
          alert("Erro no login: " + data.error.message);
        }
      } catch (error) {
        console.error("Erro ao tentar fazer login:", error);
        alert(
          "Não foi possível conectar ao servidor. Verifique se o backend está rodando."
        );
      }
    });
  }

  // --- LÓGICA PARA O LOGIN DA EMPRESA ---
  if (formEmpresa) {
    formEmpresa.addEventListener("submit", async (event) => {
      event.preventDefault();

      // ID CORRIGIDO para bater com login.html
      const cnpj = document.getElementById("input-cnpj-login").value;
      const senha = document.getElementById("input-senha-empresa").value;
      const loginData = {
        cnpj: cnpj,
        senha: senha,
      };

      try {
        const response = await fetch("http://localhost:4567/login-empresa", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(loginData),
        });
        const data = await response.json();
        if (data.success) {
          alert("Login de empresa realizado com sucesso!");
          sessionStorage.setItem("usuario", JSON.stringify(data.empresa));
          sessionStorage.setItem("tipoUsuario", "empresa");
          window.location.href = "home.html";
        } else {
          alert("Erro no login: " + data.error.message);
        }
      } catch (error) {
        console.error("Erro ao tentar fazer login:", error);
        alert(
          "Não foi possível conectar ao servidor. Verifique se o backend está rodando."
        );
      }
    });
  }
});
