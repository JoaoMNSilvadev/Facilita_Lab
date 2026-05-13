const FUNCIONARIOS = ['GESTOR', 'RECEPCAO', 'CADISTA'];
const id = window.location.pathname.split('/').pop();

function atualizarCampos() {
    const perfil = document.getElementById('perfil').value;

    const camposFuncionario = document.getElementById('campos-funcionario');
    const campoCro          = document.getElementById('campo-cro');

    if (FUNCIONARIOS.includes(perfil)) {
        camposFuncionario.style.display = 'flex';
        campoCro.style.display = 'none';
        document.getElementById('cro').value = '';
    } else if (perfil === 'DENTISTA') {
        camposFuncionario.style.display = 'none';
        campoCro.style.display = 'block';
        document.getElementById('salario').value = '';
        document.getElementById('cep').value = '';
    } else {
        camposFuncionario.style.display = 'none';
        campoCro.style.display = 'none';
    }
}

async function carregarUsuario() {
    try {
        const res = await fetch(`/usuarios/${id}`);
        if (!res.ok) throw new Error();
        const u = await res.json();

        document.getElementById('nome').value     = u.nome;
        document.getElementById('email').value    = u.email;
        document.getElementById('perfil').value   = u.perfil;
        document.getElementById('cpf').value      = u.cpf      ?? '';
        document.getElementById('telefone').value = u.telefone ?? '';

        // Dispara a exibição dos campos condicionais antes de preencher
        atualizarCampos();

        if (FUNCIONARIOS.includes(u.perfil)) {
            document.getElementById('salario').value = u.salario ?? '';
            document.getElementById('cep').value     = u.cep     ?? '';
        } else if (u.perfil === 'DENTISTA') {
            document.getElementById('cro').value = u.cro ?? '';
        }

    } catch {
        mostrar(document.getElementById('mensagem'), ['Erro ao carregar dados do usuário.'], 'erro');
    }
}

async function salvar() {
    const btn            = document.getElementById('btnSalvar');
    const msg            = document.getElementById('mensagem');
    const perfil         = document.getElementById('perfil').value;
    const senha          = document.getElementById('senha').value;
    const confirmarSenha = document.getElementById('confirmarSenha').value;

    const body = {
        nome:     document.getElementById('nome').value.trim(),
        email:    document.getElementById('email').value.trim(),
        perfil,
        cpf:      document.getElementById('cpf').value.trim(),
        telefone: document.getElementById('telefone').value.trim(),
        salario:  FUNCIONARIOS.includes(perfil) ? (document.getElementById('salario').value || null) : null,
        cep:      FUNCIONARIOS.includes(perfil) ? (document.getElementById('cep').value.trim() || null) : null,
        cro:      perfil === 'DENTISTA'          ? (document.getElementById('cro').value.trim() || null) : null,
    };

    if (senha) body.senha = senha;

    // --- Validação ---
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
    }

    if (!body.perfil) {
        erros.push('O perfil é obrigatório.');
    }

    if (!body.cpf) {
        erros.push('O CPF é obrigatório.');
    } else if (body.cpf.replace(/\D/g, '').length !== 11) {
        erros.push('CPF inválido.');
    }

    if (!body.telefone) {
        erros.push('O telefone é obrigatório.');
    }

    if (FUNCIONARIOS.includes(perfil)) {
        if (!body.salario) erros.push('O salário é obrigatório para funcionários.');
        if (!body.cep)     erros.push('O CEP é obrigatório para funcionários.');
    }

    if (perfil === 'DENTISTA' && !body.cro) {
        erros.push('O CRO é obrigatório para dentistas.');
    }

    if (senha || confirmarSenha) {
        if (senha.length < 6)         erros.push('A senha deve ter no mínimo 6 caracteres.');
        if (senha !== confirmarSenha) erros.push('As senhas não coincidem.');
    }

    if (erros.length > 0) {
        mostrar(msg, erros, 'erro');
        return;
    }

    // --- Envio ---
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
            mostrar(msg, [`Erro ${res.status}: ${await res.text()}`], 'erro');
        }
    } catch {
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