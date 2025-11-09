// Roda quando o HTML (DOM) estiver pronto
document.addEventListener("DOMContentLoaded", () => {
  // 1. Encontra o container onde os cards dos desafios devem entrar
  // (Vamos adicionar este ID no .html no próximo passo)
  const desafiosContainer = document.getElementById("desafios-container");

  // 2. Função assíncrona para buscar os desafios
  async function carregarDesafios() {
    if (!desafiosContainer) return; // Se não achar o container, para

    try {
      // 3. Faz a "ligação" (fetch) para o backend
      const response = await fetch("http://localhost:4567/desafios");

      if (!response.ok) {
        throw new Error("Não foi possível carregar os desafios.");
      }

      const data = await response.json();

      // 4. Limpa os cards estáticos (GT Foods, etc.)
      desafiosContainer.innerHTML = "";

      // 5. Verifica se o backend retornou desafios
      if (data.success && data.desafios.length > 0) {
        // 6. Loop (for) para criar um card para cada desafio
        data.desafios.forEach((desafio) => {
          // (Usamos desafio.titulo, desafio.nomeEmpresa, etc.,
          // que são os nomes do Desafio.java)
          const cardHTML = `
                        <div class="col-md-6 col-lg-4">
                            <div class="card h-100 shadow-sm">
                                <div class="card-body d-flex flex-column">
                                    <h5 class="card-title">${desafio.titulo}</h5>
                                    <h6 class="card-subtitle mb-2 text-muted">Empresa: ${desafio.nomeEmpresa}</h6>
                                    <p class="card-text">${desafio.descricao}</p>
                                    
                                    <a href="respostaForm.html?id=${desafio.id}" class="btn btn-primary mt-auto align-self-start">
                                        Ver Desafio
                                    </a>
                                </div>
                            </div>
                        </div>
                    `;
          // 7. Insere o card novo no container
          desafiosContainer.insertAdjacentHTML("beforeend", cardHTML);
        });
      } else {
        // Se não houver desafios no banco
        desafiosContainer.innerHTML =
          "<p>Nenhum desafio cadastrado no momento.</p>";
      }
    } catch (error) {
      console.error("Erro ao buscar desafios:", error);
      desafiosContainer.innerHTML =
        "<p class='text-danger'>Erro ao carregar desafios. Tente novamente.</p>";
    }
  }

  // 8. Chama a função para carregar tudo
  carregarDesafios();
});
