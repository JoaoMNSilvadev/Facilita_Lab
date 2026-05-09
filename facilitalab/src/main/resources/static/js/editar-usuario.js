const id = window.location.pathname.split('/').pop();

async function carregarUsuario() {
    try {
        const res = await fetch(`/usuarios/${id}`);
        if (!res.ok) throw new Error();
        const u = await res.json();
        document.getElementById('nome').value   = u.nome;
        document.getElementById('email').value  = u.email;
        document.getElementById('perfil').value = u.perfil;
    } catch (e) {
        mostrar(document.getElementById('mensagem'), ['Erro ao carregar dados do usuário.'], 'erro');
    }
}

async function salvar() {
    const btn            = document.getElementById('btnSalvar');
    const msg            = document.getElementById('mensagem');

    const nome           = document.getElementById('nome').value.trim();
    const email          = document.getElementById('email').value.trim();
    const perfil         = document.getElementById('perfil').value;
    const senha          = document.getElementById('senha').value;
    const confirmarSenha = document.getElementById('confirmarSenha').value;

    const erros = [];
    if (!nome)   erros.push('O nome é obrigatório.');
    if (!email)  erros.push('O e-mail é obrigatório.');
    if (!perfil) erros.push('O perfil é obrigatório.');

    if (senha || confirmarSenha) {
        if (senha.length < 6)      erros.push('A senha deve ter no mínimo 6 caracteres.');
        if (senha !== confirmarSenha) erros.push('As senhas não coincidem.');
    }

    if (erros.length > 0) {
        mostrar(msg, erros, 'erro');
        return;
    }

    const body = { nome, email, perfil };
    if (senha) body.senha = senha;

    btn.disabled = true;
    btn.textContent = 'Salvando...';
    msg.className = '';
    msg.style.display = 'none';

    try {
        const res = await fetch(`/usuarios/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (res.status === 200) {
            window.location.href = '/lista';
        } else if (res.status === 400) {
            try {
                const errosBack = await res.json();
                mostrar(msg, errosBack.errors ?? ['Erro de validação.'], 'erro');
            } catch {
                mostrar(msg, ['Erro de validação.'], 'erro');
            }
        } else {
            const err = await res.text();
            mostrar(msg, [`Erro ${res.status}: ${err}`], 'erro');
        }
    } catch (e) {
        mostrar(msg, ['Não foi possível conectar ao servidor.'], 'erro');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Salvar Alterações';
    }
}

function mostrar(el, textos, tipo) {
    el.innerHTML = Array.isArray(textos)
        ? textos.map(t => `<p>${t}</p>`).join('')
        : `<p>${textos}</p>`;
    el.className = tipo;
    el.style.display = 'block';
}

carregarUsuario();