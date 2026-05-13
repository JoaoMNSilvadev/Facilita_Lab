const FUNCIONARIOS = ['GESTOR', 'RECEPCAO', 'CADISTA'];

function atualizarCampos() {
    const perfil = document.getElementById('perfil').value;

    const camposFuncionario = document.getElementById('campos-funcionario');
    const campoCro          = document.getElementById('campo-cro');

    if (FUNCIONARIOS.includes(perfil)) {
        camposFuncionario.style.display = 'flex';
        campoCro.style.display = 'none';
        // Limpa CRO ao trocar de tipo
        document.getElementById('cro').value = '';
    } else if (perfil === 'DENTISTA') {
        camposFuncionario.style.display = 'none';
        campoCro.style.display = 'block';
        // Limpa campos de funcionário ao trocar de tipo
        document.getElementById('salario').value = '';
        document.getElementById('cep').value = '';
    } else {
        camposFuncionario.style.display = 'none';
        campoCro.style.display = 'none';
    }
}

async function cadastrar() {
    const btn  = document.getElementById('btnCadastrar');
    const msg  = document.getElementById('mensagem');
    const perfil = document.getElementById('perfil').value;

    const body = {
        nome:     document.getElementById('nome').value.trim(),
        email:    document.getElementById('email').value.trim(),
        senha:    document.getElementById('senha').value,
        perfil,
        cpf:      document.getElementById('cpf').value.trim(),
        telefone: document.getElementById('telefone').value.trim(),
        // Campos condicionais — null se não aplicável
        salario:  FUNCIONARIOS.includes(perfil) ? (document.getElementById('salario').value || null) : null,
        cep:      FUNCIONARIOS.includes(perfil) ? (document.getElementById('cep').value.trim() || null) : null,
        cro:      perfil === 'DENTISTA'          ? (document.getElementById('cro').value.trim() || null) : null,
    };

    // --- Validação no front ---
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

    if (erros.length > 0) {
        mostrar(msg, erros.join('\n'), 'erro');
        return;
    }

    // --- Envio ---
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
                mostrar(msg, errosBackend.errors?.join('\n') ?? JSON.stringify(errosBackend), 'erro');
            } catch {
                mostrar(msg, 'Erro de validação.', 'erro');
            }
        } else {
            mostrar(msg, `Erro ${res.status}: ${await res.text()}`, 'erro');
        }
    } catch {
        mostrar(msg, 'Não foi possível conectar ao servidor.', 'erro');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Cadastrar';
    }
}

function mostrar(el, texto, tipo) {
    el.innerHTML = texto.split('\n').map(l => `<p>${l}</p>`).join('');
    el.className = tipo;
}