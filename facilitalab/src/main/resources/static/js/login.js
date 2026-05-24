async function login() {
    const btn = document.getElementById('btnLogin');
    const msg = document.getElementById('mensagem');

    const body = {
        email: document.getElementById('email').value.trim(),
        senha: document.getElementById('senha').value,
    };

    const erros = [];

    if (!body.email) {
        erros.push('O e-mail é obrigatório.');
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(body.email)) {
        erros.push('E-mail inválido.');
    }

    if (!body.senha) {
        erros.push('A senha é obrigatória.');
    }

    if (erros.length > 0) {
        mostrar(msg, erros, 'erro');
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Entrando...';
    msg.className = '';
    msg.style.display = 'none';

    try {
        const res = await fetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
        });

        if (res.ok) {
            const data = await res.json();
            localStorage.setItem('token',  data.token);
            localStorage.setItem('nome',   data.nome);
            localStorage.setItem('perfil', data.perfil);
            window.location.href = '/dashboard';
        } else if (res.status === 401) {
            mostrar(msg, ['E-mail ou senha incorretos.'], 'erro');
        } else if (res.status === 400) {
            try {
                const errosBack = await res.json();
                mostrar(msg, errosBack.errors ?? ['Erro de validação.'], 'erro');
            } catch {
                mostrar(msg, ['Erro de validação.'], 'erro');
            }
        } else {
            mostrar(msg, [`Erro ${res.status}. Tente novamente.`], 'erro');
        }
    } catch {
        mostrar(msg, ['Não foi possível conectar ao servidor.'], 'erro');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Entrar';
    }
}

function mostrar(el, textos, tipo) {
    el.innerHTML = Array.isArray(textos)
        ? textos.map(t => `<p>${t}</p>`).join('')
        : `<p>${textos}</p>`;
    el.className = tipo;
    el.style.display = 'block';
}

document.addEventListener('DOMContentLoaded', () => {
    const splash      = document.getElementById('splash');
    const loginScreen = document.getElementById('login-screen');

    // Splash sai com fade + slide up após a barra de progresso (~2s)
    setTimeout(() => {
        splash.classList.add('hide');

        // Login entra logo depois do splash sair
        setTimeout(() => {
            loginScreen.classList.add('show');
        }, 400);
    }, 2000);

    // Permite submeter com Enter em qualquer campo do formulário
    ['email', 'senha'].forEach(id => {
        document.getElementById(id).addEventListener('keydown', e => {
            if (e.key === 'Enter') login();
        });
    });
});
