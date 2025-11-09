// Espera o HTML carregar
document.addEventListener("DOMContentLoaded", () => {
  // Encontra os dois formulários de cadastro (IDs CORRIGIDOS)
  const formAluno = document.getElementById("form-aluno");
  const formEmpresa = document.getElementById("form-empresa");

  // --- LÓGICA PARA O CADASTRO DE ALUNO ---
  if (formAluno) {
    formAluno.addEventListener("submit", async (event) => {
      event.preventDefault();

      // Pega os valores dos campos (IDs CORRIGIDOS)
      const nome = document.getElementById("input-nome-aluno").value;
      const email = document.getElementById("input-email-aluno").value;
      const curso = document.getElementById("input-curso").value; // Corrigido
      const semestre = parseInt(
        document.getElementById("input-semestre").value
      ); // Corrigido
      const habilidades = document.getElementById("input-habilidades").value; // Corrigido

      // NOTA: Seu HTML usa <input type="file"> (id="input-vinculo")
      // O backend espera uma URL (url_comprovante).
      // Para o trabalho funcionar, vamos simular o envio de uma URL.
      const comprovanteUrl = "https://comprovante.teste.com/aluno.pdf";

      const senha = document.getElementById("input-senha-aluno").value;
      const confirmaSenha = document.getElementById(
        "input-senha-aluno-confirma"
      ).value; // Corrigido

      if (senha !== confirmaSenha) {
        alert("As senhas não conferem!");
        return;
      }

      const alunoData = {
        nome_completo: nome,
        email_institucional: email,
        curso: curso,
        semestre: semestre,
        habilidades: habilidades,
        url_comprovante: comprovanteUrl, // Enviando a URL simulada
        foto_perfil_url: "", // Opcional
        senha: senha,
      };

      try {
        const response = await fetch("http://localhost:8090/alunos", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(alunoData),
        });
        const data = await response.json();
        if (data.success) {
          alert(
            "Aluno cadastrado com sucesso! Você será redirecionado para o login."
          );
          window.location.href = "login.html";
        } else {
          alert("Erro no cadastro: " + data.error.message);
        }
      } catch (error) {
        console.error("Erro ao cadastrar aluno:", error);
        alert(
          "Não foi possível conectar ao servidor. Verifique se o backend está rodando."
        );
      }
    });
  }

  // --- LÓGICA PARA O CADASTRO DE EMPRESA ---
  if (formEmpresa) {
    formEmpresa.addEventListener("submit", async (event) => {
      event.preventDefault();

      // Pega os valores dos campos (IDs CORRIGIDOS)
      const nome = document.getElementById("input-nome-empresa").value;
      const cnpj = document.getElementById("input-cnpj-cadastro").value; // Corrigido
      const email = document.getElementById("input-email-empresa").value;
      const setor = document.getElementById("input-setor").value; // Corrigido
      const cidade = document.getElementById("input-cidade").value; // Corrigido
      const senha = document.getElementById("input-senha-empresa").value;
      const confirmaSenha = document.getElementById(
        "input-senha-empresa-confirma"
      ).value; // Corrigido

      if (senha !== confirmaSenha) {
        alert("As senhas não conferem!");
        return;
      }

      const empresaData = {
        nome_empresa: nome,
        cnpj: cnpj,
        email_contato: email,
        setor: setor,
        cidade: cidade,
        senha: senha,
      };

      try {
        const response = await fetch("http://localhost:8090/empresas", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(empresaData),
        });
        const data = await response.json();
        if (data.success) {
          alert(
            "Empresa cadastrada com sucesso! Você será redirecionada para o login."
          );
          window.location.href = "login.html";
        } else {
          alert("Erro no cadastro: " + data.error.message);
        }
      } catch (error) {
        console.error("Erro ao cadastrar empresa:", error);
        alert(
          "Não foi possível conectar ao servidor. Verifique se o backend está rodando."
        );
      }
    });
  }
});
