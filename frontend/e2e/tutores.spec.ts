import { Page, expect, test } from '@playwright/test';
import { MockApi } from './mock-api';

/** Abre o menu de ações (hambúrguer) da linha que contém o texto e escolhe uma opção. */
async function acaoNaLinha(page: Page, textoLinha: string, opcao: string) {
  await page
    .getByRole('row', { name: new RegExp(textoLinha) })
    .getByRole('button', { name: 'Ações' })
    .click();
  await page.locator('.ant-dropdown-menu').getByText(opcao, { exact: true }).click();
}

test.describe('Tutores', () => {
  test('lista e filtra tutores, inclusive por CPF', async ({ page }) => {
    const api = await MockApi.instalar(page);
    api.tutor({ nome: 'Ana Souza', cpf: '52998224725' });
    api.tutor({
      nome: 'Bruno Lima',
      cpf: '11144477735',
      endereco: {
        cep: '13010-000',
        logradouro: 'Avenida Paulista',
        bairro: 'Centro',
        cidade: 'Campinas',
        estado: 'SP',
      },
    });

    await page.goto('/');
    await expect(page).toHaveURL(/\/tutores$/);
    await expect(page.getByRole('row')).toHaveCount(3);
    await expect(page.getByText('529.982.247-25')).toBeVisible();

    const busca = page.getByPlaceholder('Buscar por nome, CPF, e-mail ou endereço');
    await busca.fill('paulista');
    await expect(page.getByRole('row')).toHaveCount(2);
    await expect(page.getByRole('link', { name: 'Bruno Lima' })).toBeVisible();

    await busca.fill('529.982');
    await expect(page.getByRole('row')).toHaveCount(2);
    await expect(page.getByRole('link', { name: 'Ana Souza' })).toBeVisible();
  });

  test('cadastra tutor com máscaras e endereço preenchido pelo CEP', async ({ page }) => {
    const api = await MockApi.instalar(page);
    await page.goto('/tutores/novo');

    await page.getByRole('button', { name: 'Salvar' }).click();
    await expect(page.getByText('Informe o nome.')).toBeVisible();

    await page.getByLabel('Nome').fill('Carla Dias');
    await page.getByLabel('CPF').pressSequentially('12345678900');
    await expect(page.getByLabel('CPF')).toHaveValue('123.456.789-00');
    await expect(page.getByText('CPF inválido.')).toBeVisible();
    await page.getByLabel('CPF').fill('');
    await page.getByLabel('CPF').pressSequentially('52998224725');
    await expect(page.getByLabel('CPF')).toHaveValue('529.982.247-25');

    await page.getByLabel('E-mail').fill('carla@test.com');
    await page.getByLabel('Telefone').pressSequentially('11912345678');
    await expect(page.getByLabel('Telefone')).toHaveValue('(11) 91234-5678');

    await page.getByLabel('CEP').pressSequentially('01001000');
    await expect(page.getByLabel('CEP')).toHaveValue('01001-000');
    await expect(page.getByText('Endereço preenchido pelo CEP.')).toBeVisible();
    await expect(page.getByLabel('Logradouro')).toHaveValue('Praça da Sé');
    await expect(page.getByLabel('Cidade')).toHaveValue('São Paulo');
    await expect(page.getByLabel('Número')).toBeFocused();
    await page.getByLabel('Número').fill('100');

    await page.getByRole('button', { name: 'Salvar' }).click();

    await expect(page.getByText('Tutor cadastrado.', { exact: true })).toBeVisible();
    await expect(page.getByRole('heading', { level: 1, name: 'Carla Dias' })).toBeVisible();
    const salvo = api.tutores.at(-1)!;
    expect(salvo['cpf']).toBe('52998224725');
    expect(salvo['telefone']).toBe('11912345678');
  });

  test('avisa quando o CEP não é encontrado', async ({ page }) => {
    await MockApi.instalar(page);
    await page.goto('/tutores/novo');
    await page.getByLabel('CEP').pressSequentially('99999999');
    await expect(
      page.getByText('CEP não encontrado. Preencha o endereço manualmente.'),
    ).toBeVisible();
  });

  test('oferece recuperar o cadastro quando o CPF é de um tutor excluído', async ({ page }) => {
    const api = await MockApi.instalar(page);
    const antigo = api.tutor({ nome: 'Carla Antiga', cpf: '52998224725', email: 'carla@test.com' });
    antigo['excluido'] = true;

    await page.goto('/tutores');
    await expect(page.getByText('Nenhum tutor cadastrado ainda.')).toBeVisible();

    await page.goto('/tutores/novo');
    await page.getByLabel('CPF').pressSequentially('52998224725');

    const modal = page.locator('.ant-modal');
    await expect(modal.getByText('Recuperar cadastro')).toBeVisible();
    await expect(modal.getByText('Carla Antiga')).toBeVisible();
    await expect(modal.getByText('carla@test.com')).toBeVisible();
    await modal.getByRole('button', { name: 'Restaurar cadastro' }).click();

    await expect(page.getByText('Cadastro restaurado. Revise os dados do tutor.')).toBeVisible();
    await expect(page).toHaveURL(new RegExp(`/tutores/${antigo.id}/editar$`));
    await expect(page.getByLabel('Nome')).toHaveValue('Carla Antiga');
    expect(antigo['excluido']).toBe(false);
  });

  test('exibe o erro de e-mail duplicado vindo da API', async ({ page }) => {
    const api = await MockApi.instalar(page);
    api.tutor({ email: 'ana@test.com' });
    await page.goto('/tutores/novo');

    await page.getByLabel('Nome').fill('Outra Ana');
    await page.getByLabel('CPF').pressSequentially('11144477735');
    await page.getByLabel('E-mail').fill('ana@test.com');
    await page.getByLabel('Telefone').pressSequentially('11912345678');
    await page.getByLabel('CEP').pressSequentially('01001000');
    await expect(page.getByLabel('Logradouro')).toHaveValue('Praça da Sé');
    await page.getByRole('button', { name: 'Salvar' }).click();

    await expect(page.getByText('E-mail já cadastrado.')).toBeVisible();
    await expect(page).toHaveURL(/\/tutores\/novo$/);
  });

  test('exclui um tutor pelo menu de ações após confirmação', async ({ page }) => {
    const api = await MockApi.instalar(page);
    api.tutor({ nome: 'Ana Souza' });
    await page.goto('/tutores');

    await acaoNaLinha(page, 'Ana Souza', 'Excluir');
    const modal = page.locator('.ant-modal-confirm');
    await expect(modal.getByText('Excluir o tutor Ana Souza?')).toBeVisible();
    await modal.getByRole('button', { name: 'Excluir' }).click();

    await expect(page.getByText('Tutor excluído.', { exact: true })).toBeVisible();
    await expect(page.getByText('Nenhum tutor cadastrado ainda.')).toBeVisible();
  });

  test('pagina a listagem no servidor com 10 tutores por página', async ({ page }) => {
    const api = await MockApi.instalar(page);
    for (let i = 1; i <= 12; i++) api.tutor({ nome: `Tutor ${String(i).padStart(2, '0')}` });
    const pedidos: string[] = [];
    page.on('request', (r) => {
      if (r.url().includes('/api/tutores?')) pedidos.push(new URL(r.url()).search);
    });

    await page.goto('/tutores');
    await expect(page.getByRole('row')).toHaveCount(11);
    await expect(page.getByRole('link', { name: 'Tutor 10' })).toBeVisible();

    await page.locator('.ant-pagination-item').filter({ hasText: '2' }).click();
    await expect(page.getByRole('row')).toHaveCount(3);
    await expect(page.getByRole('link', { name: 'Tutor 12' })).toBeVisible();

    expect(pedidos).toEqual(['?pagina=0&tamanho=10', '?pagina=1&tamanho=10']);
  });
});
