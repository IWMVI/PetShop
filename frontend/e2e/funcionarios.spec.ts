import { expect, test } from '@playwright/test';
import { MockApi } from './mock-api';

test.describe('Funcionários', () => {
  test('cadastra, edita e exclui um funcionário', async ({ page }) => {
    const api = await MockApi.instalar(page);
    await page.goto('/funcionarios');
    await expect(page.getByText('Nenhum funcionário cadastrado ainda.')).toBeVisible();

    await page.getByRole('link', { name: 'Novo funcionário' }).click();
    await page.getByRole('button', { name: 'Salvar' }).click();
    await expect(page.getByText('Informe o nome (de 3 a 150 caracteres).')).toBeVisible();
    await expect(page.getByText('Selecione o cargo.')).toBeVisible();

    await page.getByLabel('Nome').fill('Dra. Ana Souza');
    await page.getByLabel('CPF').fill('52998224725');
    await expect(page.getByLabel('CPF')).toHaveValue('529.982.247-25');
    await page.getByLabel('Telefone').fill('11988887777');
    await page.getByLabel('Cargo').click();
    await page.locator('.ant-select-item-option').filter({ hasText: 'Veterinário(a)' }).click();
    await page.getByRole('button', { name: 'Salvar' }).click();

    await expect(page.getByText('Funcionário cadastrado.', { exact: true })).toBeVisible();
    const linha = page.getByRole('row', { name: /Dra\. Ana Souza/ });
    await expect(linha).toContainText('Veterinário(a)');
    await expect(linha).toContainText('529.982.247-25');
    expect(api.funcionarios[0]['cpf']).toBe('52998224725');

    await linha.getByRole('button', { name: 'Ações' }).click();
    await page.locator('.ant-dropdown-menu').getByText('Editar', { exact: true }).click();
    await expect(page.getByLabel('Nome')).toHaveValue('Dra. Ana Souza');
    await expect(page.getByLabel('Telefone')).toHaveValue('(11) 98888-7777');
    await page.getByLabel('Nome').fill('Dra. Ana Lima');
    await page.getByRole('button', { name: 'Salvar' }).click();
    await expect(page.getByRole('row', { name: /Dra\. Ana Lima/ })).toBeVisible();

    await page
      .getByRole('row', { name: /Dra\. Ana Lima/ })
      .getByRole('button', { name: 'Ações' })
      .click();
    await page.locator('.ant-dropdown-menu').getByText('Excluir', { exact: true }).click();
    await page.locator('.ant-modal-confirm-btns .ant-btn-dangerous').click();
    await expect(page.getByText('Nenhum funcionário cadastrado ainda.')).toBeVisible();
  });

  test('recusa CPF já cadastrado', async ({ page }) => {
    const api = await MockApi.instalar(page);
    api.funcionario({ nome: 'Bia', cpf: '52998224725' });
    await page.goto('/funcionarios/novo');

    await page.getByLabel('Nome').fill('Carlos');
    await page.getByLabel('CPF').fill('52998224725');
    await page.getByLabel('Telefone').fill('11988887777');
    await page.getByLabel('Cargo').click();
    await page.locator('.ant-select-item-option').filter({ hasText: 'Tosador(a)' }).click();
    await page.getByRole('button', { name: 'Salvar' }).click();

    await expect(page.getByText('CPF já cadastrado.')).toBeVisible();
    expect(api.funcionarios).toHaveLength(1);
  });

  test('busca funcionários pelo nome', async ({ page }) => {
    const api = await MockApi.instalar(page);
    api.funcionario({ nome: 'Ana' });
    api.funcionario({ nome: 'Bruno' });
    await page.goto('/funcionarios');
    await expect(page.getByRole('row', { name: /Bruno/ })).toBeVisible();

    await page.getByLabel('Buscar funcionários').fill('bru');
    await expect(page.getByRole('row', { name: /Ana/ })).toBeHidden();
    await expect(page.getByRole('row', { name: /Bruno/ })).toBeVisible();
  });
});
