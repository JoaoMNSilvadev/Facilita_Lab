const { test, expect } = require('@playwright/test');

const GESTOR = {
  email: process.env.E2E_EMAIL ?? 'admin@facilitalab.com',
  senha: process.env.E2E_SENHA ?? 'admin123',
};

let dentistaId = null;
let cor = null;


async function tokenDoGestor(request) {
  const res = await request.post('/auth/login', {
    data: { email: GESTOR.email, senha: GESTOR.senha },
  });
  expect(res.ok()).toBeTruthy();
  return (await res.json()).token;
}

function dataEmDias(n) {
  const d = new Date();
  d.setDate(d.getDate() + n);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return { iso: `${yyyy}-${mm}-${dd}`, br: `${dd}/${mm}/${yyyy}` };
}


test.afterEach(async ({ request }) => {
  const token = await tokenDoGestor(request);
  const auth = { Authorization: `Bearer ${token}` };

  const res = await request.get('/pedidos', { headers: auth });
  if (res.ok()) {
    const pedidos = await res.json();
    for (const p of pedidos.filter(p => p.cor === cor)) {
      await request.delete(`/pedidos/${p.id}`, { headers: auth });
    }
  }

  if (dentistaId) {
    await request.delete(`/usuarios/${dentistaId}`, { headers: auth });
  }
  dentistaId = null;
  cor = null;
});


test('DENTISTA cria um pedido e ele aparece na lista com estado "Aguardando Triagem"', async ({ page, request }) => {
  const sufixo = Date.now();
  const dentista = {
    nome: `Dentista Teste ${sufixo}`,
    email: `dentista.${sufixo}@teste.com`,
    senha: 'senha123',
  };
  cor = `Shade-${sufixo}`; 
  const prazo = dataEmDias(7);

  const token = await tokenDoGestor(request);
  const resUsuario = await request.post('/usuarios', {
    headers: { Authorization: `Bearer ${token}` },
    data: {
      nome: dentista.nome,
      email: dentista.email,
      senha: dentista.senha,
      perfil: 'DENTISTA',
      cpf: String(sufixo).slice(-11),
      telefone: '71999998888',
      cro: 'CRO-BA 123456',
    },
  });
  expect(resUsuario.status()).toBe(201);
  dentistaId = (await resUsuario.json()).id;

  await page.goto('/login');
  await expect(page.locator('#login-screen')).toHaveClass(/show/, { timeout: 15000 }); // espera a splash
  await page.fill('#email', dentista.email);
  await page.fill('#senha', dentista.senha);
  await page.click('#btnLogin');
  await expect(page).toHaveURL(/\/dashboard-dentista$/);

  await page.locator('.btn-novo').click();
  await expect(page).toHaveURL(/\/cadastro-pedido$/);
  await expect(page.getByRole('heading', { name: 'Novo Pedido' })).toBeVisible();

  await page.fill('#cor', cor);
  await page.selectOption('#tipoProtese', 'COROA');
  await page.selectOption('#material', 'ZIRCONIA');
  await page.selectOption('#prioridade', 'NORMAL');
  await page.fill('#prazoEntrega', prazo.iso);
  await page.fill('#observacoes', 'Pedido criado pelo teste E2E');

  const [resposta] = await Promise.all([
    page.waitForResponse(r => r.url().endsWith('/pedidos') && r.request().method() === 'POST'),
    page.click('#btnEnviar'),
  ]);
  expect(resposta.status()).toBe(201);

  const pedido = await resposta.json();
  expect(pedido.cor).toBe(cor);
  expect(pedido.estado).toBe('AGUARDANDO_TRIAGEM');
  expect(pedido.dentistaId).toBe(dentistaId);

  await expect(page).toHaveURL(/\/lista-pedidos$/);

  const linha = page.locator('#corpo tr', { hasText: cor });
  await expect(linha).toBeVisible();
  await expect(linha).toContainText('Coroa');
  await expect(linha).toContainText('Zircônia');
  await expect(linha).toContainText(dentista.nome);
  await expect(linha).toContainText('Aguardando Triagem');
  await expect(linha).toContainText(prazo.br);
});