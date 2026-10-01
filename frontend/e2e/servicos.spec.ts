import { expect, test } from '@playwright/test';
import { MockApi } from './mock-api';

test.describe('Serviços', () => {
  test('cadastra e edita um serviço', async ({ page }) => {
    const api = await MockApi.instalar(page);
    await page.goto('/servicos');
    await expect(page.getByText('Nenhum serviço cadastrado.')).toBeVisible();

    await page.getByRole('link', { name: 'Novo serviço' }).click();
    await page.getByLabel('Nome').fill('Hidratação');
    const preco = page.getByLabel('Preço');
    for (const [tecla, esperado] of [
      ['7', '0,07'],
      ['5', '0,75'],
      ['9', '7,59'],
      ['0', '75,90'],
    ]) {
      await preco.press(tecla);
      await expect(preco).toHaveValue(esperado);
    }
    await page.getByLabel('Duração estimada (min)').fill('30');
    await page.getByRole('button', { name: 'Salvar' }).click();

    await expect(page.getByText('Serviço cadastrado.', { exact: true })).toBeVisible();
    const linha = page.getByRole('row', { name: /Hidratação/ });
    await expect(linha).toContainText('R$ 75,90');
    await expect(linha).toContainText('30 min');

    await linha.getByRole('button', { name: 'Ações' }).click();
    await page.locator('.ant-dropdown-menu').getByText('Editar', { exact: true }).click();
    await expect(page.getByLabel('Nome')).toHaveValue('Hidratação');
    await expect(page.getByLabel('Preço')).toHaveValue('75,90');
    await page.getByLabel('Nome').fill('Hidratação premium');
    await page.getByRole('button', { name: 'Salvar' }).click();

    await expect(page.getByRole('row', { name: /Hidratação premium/ })).toBeVisible();
    expect(api.servicos[0]['nome']).toBe('Hidratação premium');
    expect(api.servicos[0]['preco']).toBe(75.9);
  });
});

test('formata preços com milhar e centavos na digitação', async ({ page }) => {
  await MockApi.instalar(page);
  await page.goto('/servicos/novo');
  const preco = page.getByLabel('Preço');
  await preco.pressSequentially('123456');
  await expect(preco).toHaveValue('1.234,56');
  await preco.press('Backspace');
  await expect(preco).toHaveValue('123,45');
});
