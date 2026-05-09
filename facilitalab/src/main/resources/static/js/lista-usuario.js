function editar(id) {
    window.location.href = `/editar/${id}`;
}

async function deletar(id, btn) {
    if (!confirm('Deseja realmente excluir este usuário?')) return;

    btn.disabled = true;
    btn.textContent = '...';

    try {
        const res = await fetch(`/usuarios/${id}`, { method: 'DELETE' });
        if (res.status === 204) {
            btn.closest('tr').remove();
        } else {
            alert('Erro ao excluir usuário.');
            btn.disabled = false;
            btn.textContent = 'Excluir';
        }
    } catch (e) {
        alert('Não foi possível conectar ao servidor.');
        btn.disabled = false;
        btn.textContent = 'Excluir';
    }
}

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
            const data = u.dataCriacao
                ? new Date(u.dataCriacao).toLocaleDateString('pt-BR', {
                    day: '2-digit', month: '2-digit', year: 'numeric'
                })
                : '—';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${u.nome}</td>
                <td>${u.email}</td>
                <td><span>${u.perfil}</span></td>
                <td>${data}</td>
                <td class="acoes">
                    <button class="btn-editar" onclick="editar(${u.id})">Editar</button>
                    <button class="btn-deletar" onclick="deletar(${u.id}, this)">Excluir</button>
                </td>
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