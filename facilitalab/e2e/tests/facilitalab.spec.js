const { test, expect } = require('@playwright/test');

const GESTOR = {
  email: process.env.E2E_EMAIL ?? 'admin@facilitalab.com',
  senha: process.env.E2E_SENHA ?? 'admin123',
};

async function loginComoGestor(page) {
  await page.goto('/login');

  await expect(page.locator('#login-screen')).toHaveClass(/show/, { timeout: 15000 });

  await page.fill('#email', GESTOR.email);
  await page.fill('#senha', GESTOR.senha);
  await page.click('#btnLogin');

  await expect(page).toHaveURL(/\/dashboard$/);
}

test('GESTOR cadastra um usuário, vê na lista e exclui', async ({ page }) => {
  const sufixo = Date.now();
  const usuario = {
    nome: `Recepcao Teste ${sufixo}`,
    email: `recepcao.${sufixo}@teste.com`,
    cpf: String(sufixo).slice(-11),
  };

  await loginComoGestor(page);

  await page.goto('/cadastro-usuario');
  await page.fill('#nome', usuario.nome);
  await page.fill('#email', usuario.email);
  await page.fill('#senha', 'senha123');
  await page.selectOption('#perfil', 'RECEPCAO');
  await page.fill('#cpf', usuario.cpf);
  await page.fill('#telefone', '71999998888');
  await page.click('#btnCadastrar');

  await expect(page).toHaveURL(/\/lista-usuarios$/);
  const linha = page.locator('#corpo tr', { hasText: usuario.email });
  await expect(linha).toBeVisible();
  await expect(linha).toContainText('Recepção');

  page.once('dialog', dialog => dialog.accept());
  await linha.getByRole('button', { name: 'Excluir' }).click();

  await expect(linha).toHaveCount(0);
});