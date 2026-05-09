async function carregarUsuarios() {
    const corpo = document.getElementById('corpo');
    const vazio = document.getElementById('vazio');
    const tabela = document.getElementById('tabela');

    try {
        const res = await fetch('/usuarios');
        const lista = await res.json();

        if (lista.length === 0) {
            tabela.style.display = 'none';
            vazio.style.display = 'block';
            return;
        }

        lista.forEach(u => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${u.nome}</td>
                <td>${u.email}</td>
                <td><span>${u.perfil}</span></td>
            `;
            corpo.appendChild(tr);
        });

    } catch (e) {
        console.log('Erro:', e);
        vazio.textContent = 'Erro ao carregar usuários.';
        vazio.style.display = 'block';
        tabela.style.display = 'none';
    }
}

carregarUsuarios();