async function cadastrar() {
    const btn = document.getElementById('btnCadastrar');
    const msg = document.getElementById('mensagem');

    const body = {
        nome:   document.getElementById('nome').value.trim(),
        email:  document.getElementById('email').value.trim(),
        senha:  document.getElementById('senha').value,
        perfil: document.getElementById('perfil').value
    };

    // Validação campo a campo
    const erros = [];

    if (!body.nome) {
        erros.push('O nome é obrigatório.');
    } else if (body.nome.length < 3 || body.nome.length > 100) {
        erros.push('O nome deve ter entre 3 e 100 caracteres.');
    }

    if (!body.email) {
        erros.push('O e-mail é obrigatório.');
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(body.email)) {
        erros.push('E-mail inválido.');
    } else if (body.email.length > 250) {
        erros.push('O e-mail deve ter no máximo 250 caracteres.');
    }

    if (!body.senha) {
        erros.push('A senha é obrigatória.');
    } else if (body.senha.length < 6 || body.senha.length > 100) {
        erros.push('A senha deve ter entre 6 e 100 caracteres.');
    }

    if (!body.perfil) {
        erros.push('O perfil é obrigatório.');
    }

    if (erros.length > 0) {
        mostrar(msg, erros.join('\n'), 'erro');
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Enviando...';
    msg.className = '';
    msg.style.display = 'none';

    try {
        const res = await fetch('/usuarios', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (res.status === 201) {
            window.location.href = '/lista';
        } else if (res.status === 400) {
            try {
                const errosBackend = await res.json();
                if (errosBackend.errors) {
                    mostrar(msg, errosBackend.errors.join('\n'), 'erro');
                } else {
                    mostrar(msg, JSON.stringify(errosBackend), 'erro');
                }
            } catch {
                mostrar(msg, 'Erro de validação.', 'erro');
            }
        } else {
            const err = await res.text();
            mostrar(msg, `Erro ${res.status}: ${err}`, 'erro');
        }
    } catch (e) {
        mostrar(msg, 'Não foi possível conectar ao servidor.', 'erro');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Cadastrar';
    }
}

function mostrar(el, texto, tipo) {
    el.textContent = texto;
    el.className = tipo;
    el.style.display = 'block';
}

function limparForm() {
    ['nome', 'email', 'senha'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('perfil').value = '';
}