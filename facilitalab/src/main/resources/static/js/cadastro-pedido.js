async function carregarDentistas() {
    const sel = document.getElementById('dentistaId');
    try {
        const res = await fetch('/usuarios/perfil/DENTISTA');
        const dentistas = await res.json();
        sel.innerHTML = '<option value="">Selecione o dentista...</option>';
        dentistas.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = d.nome;
            sel.appendChild(opt);
        });
    } catch {
        sel.innerHTML = '<option value="">Erro ao carregar dentistas</option>';
    }
}

async function cadastrar() {
    const btn = document.getElementById('btnCadastrar');
    const msg = document.getElementById('mensagem');

    const body = {
        cor:          document.getElementById('cor').value.trim(),
        tipoProtese:  document.getElementById('tipoProtese').value,
        material:     document.getElementById('material').value,
        prazoEntrega: document.getElementById('prazoEntrega').value,
        dentistaId:   document.getElementById('dentistaId').value
            ? Number(document.getElementById('dentistaId').value)
            : null,
        observacoes:  document.getElementById('observacoes').value.trim() || null,
    };

    const erros = [];

    if (!body.cor) {
        erros.push('A cor é obrigatória.');
    } else if (body.cor.length > 50) {
        erros.push('A cor deve ter no máximo 50 caracteres.');
    }

    if (!body.tipoProtese) erros.push('O tipo de prótese é obrigatório.');
    if (!body.material)    erros.push('O material é obrigatório.');

    if (!body.prazoEntrega) {
        erros.push('O prazo de entrega é obrigatório.');
    } else if (new Date(body.prazoEntrega + 'T00:00:00') <= new Date()) {
        erros.push('O prazo de entrega deve ser uma data futura.');
    }

    if (!body.dentistaId) erros.push('O dentista é obrigatório.');

    if (erros.length > 0) {
        mostrar(msg, erros, 'erro');
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Enviando...';
    msg.className = '';
    msg.style.display = 'none';

    try {
        const res = await fetch('/pedidos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
        });

        if (res.status === 201) {
            window.location.href = '/lista-pedidos';
        } else if (res.status === 400) {
            try {
                const errosBackend = await res.json();
                mostrar(msg, errosBackend.errors ?? ['Erro de validação.'], 'erro');
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
        btn.textContent = 'Cadastrar';
    }
}

function mostrar(el, textos, tipo) {
    el.innerHTML = Array.isArray(textos)
        ? textos.map(t => `<p>${t}</p>`).join('')
        : `<p>${textos}</p>`;
    el.className = tipo;
    el.style.display = 'block';
}

carregarDentistas();
