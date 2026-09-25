import { expect, test } from '@playwright/test';
import { MockApi } from './mock-api';

// Tela de celular (iPhone 12/13/14): 390 x 844.
test.use({ viewport: { width: 390, height: 844 } });

test.describe('Layout de celular', () => {
  test('navega pelo menu em gaveta e mostra o status da API', async ({ page }) => {
    const api = await MockApi.instalar(page);
    api.servico({ nome: 'Banho' });
    await page.goto('/tutores');

    // O menu lateral dá lugar à barra superior com o botão de menu.
    await expect(page.locator('nz-sider')).toHaveCount(0);
    await expect(page.getByRole('status', { name: 'API online' })).toBeVisible();

    await page.getByRole('button', { name: 'Abrir menu' }).click();
    const gaveta = page.locator('.menu-celular');
    await expect(gaveta.getByText('API online')).toBeVisible();
    await gaveta.getByText('Serviços').click();

    await expect(page).toHaveURL(/\/servicos$/);
    await expect(page.getByRole('heading', { level: 1, name: 'Serviços' })).toBeVisible();
    // A gaveta fecha deslizando para fora da tela (continua no DOM, fora da área visível).
    await expect(page.locator('.ant-drawer-open')).toHaveCount(0);
    await expect(gaveta).not.toBeInViewport();
  });

  test('esconde as colunas secundárias da tabela de tutores', async ({ page }) => {
    const api = await MockApi.instalar(page);
    api.tutor({ nome: 'Ana Souza', cpf: '52998224725' });
    await page.goto('/tutores');

    const cabecalhos = page.getByRole('columnheader');
    await expect(cabecalhos.filter({ hasText: 'Nome' })).toBeVisible();
    await expect(cabecalhos.filter({ hasText: 'Telefone' })).toBeVisible();
    for (const coluna of ['CPF', 'E-mail', 'Endereço']) {
      await expect(cabecalhos.filter({ hasText: coluna })).toBeHidden();
    }
    // Nenhum conteúdo deve ultrapassar a largura da tela.
    const larguraDaPagina = await page.evaluate(() => document.documentElement.scrollWidth);
    expect(larguraDaPagina).toBeLessThanOrEqual(390);
  });

  test('resume serviços e total abaixo da data e mostra o status em badge', async ({ page }) => {
    const api = await MockApi.instalar(page);
    const tutor = api.tutor();
    const pet = api.pet(tutor.id, { nome: 'Rex' });
    const banho = api.servico({ nome: 'Banho', preco: 50 });
    api.agendamentos.get(pet.id)!.push({
      id: 900,
      petId: pet.id,
      dataHora: '2099-12-31T10:00:00',
      observacoes: null,
      status: 'AGENDADO',
      valorTotal: 50,
      servicos: [{ servicoId: banho.id, nome: 'Banho', precoCobrado: 50 }],
    });
    await page.goto(`/tutores/${tutor.id}/pets/${pet.id}`);

    const linha = page.getByRole('row', { name: /31\/12\/2099/ });
    await expect(linha.getByText('Banho · R$ 50,00')).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Total' })).toBeHidden();
    await expect(linha.getByRole('status')).toHaveText('Agendado');
  });

  test('empilha os botões do formulário com a ação principal em cima', async ({ page }) => {
    await MockApi.instalar(page);
    await page.goto('/servicos/novo');

    const salvar = await page.getByRole('button', { name: 'Salvar' }).boundingBox();
    const cancelar = await page.getByRole('link', { name: 'Cancelar' }).boundingBox();
    expect(salvar!.y).toBeLessThan(cancelar!.y);
    expect(salvar!.width).toBeGreaterThan(300);
  });
});
