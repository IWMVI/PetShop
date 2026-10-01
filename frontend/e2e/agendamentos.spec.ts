import { expect, test } from '@playwright/test';
import { MockApi } from './mock-api';

test.describe('Pets e agendamentos', () => {
  test('cadastra um pet para o tutor', async ({ page }) => {
    const api = await MockApi.instalar(page);
    const tutor = api.tutor({ nome: 'Ana Souza' });

    await page.goto(`/tutores/${tutor.id}`);
    await expect(page.getByText('Nenhum pet cadastrado para este tutor.')).toBeVisible();
    await page.getByRole('link', { name: 'Novo pet' }).click();

    await page.getByLabel('Nome').fill('Mingau');
    await page.getByLabel('Espécie').fill('Gato');
    await page.getByLabel('Idade (anos)').fill('2');
    await page.getByRole('button', { name: 'Salvar' }).click();

    await expect(page.getByText('Pet cadastrado.', { exact: true })).toBeVisible();
    await expect(page.getByRole('heading', { level: 1, name: 'Mingau' })).toBeVisible();
  });

  test('agenda, lista e cancela um atendimento', async ({ page }) => {
    const api = await MockApi.instalar(page);
    const tutor = api.tutor();
    const pet = api.pet(tutor.id, { nome: 'Rex' });
    api.servico({ nome: 'Banho', preco: 50 });
    api.servico({ nome: 'Tosa', preco: 30.5 });

    await page.goto(`/tutores/${tutor.id}/pets/${pet.id}`);
    await page.getByRole('link', { name: 'Novo agendamento' }).click();

    await page.getByRole('button', { name: 'Salvar' }).click();
    await expect(page.getByText('Selecione ao menos um serviço.')).toBeVisible();
    await expect(page.getByText('Informe a data e a hora.')).toBeVisible();

    const data = page.getByLabel('Data e hora');
    await data.click();
    await data.pressSequentially('31/12/2099 10:00');
    await page.locator('.ant-picker-ok').getByRole('button').click();
    await expect(data).toHaveValue('31/12/2099 10:00');

    const servicos = page.getByLabel('Serviços');
    await servicos.click();
    const opcao = (nome: string) =>
      page.locator('.ant-select-item-option').filter({ hasText: nome });
    await expect(opcao('Banho')).toBeVisible();
    await expect(opcao('Tosa')).toBeVisible();

    // A busca de serviços acontece no servidor.
    await servicos.pressSequentially('tos');
    await expect(opcao('Banho')).toBeHidden();
    await opcao('Tosa').click();
    await servicos.fill('');
    await opcao('Banho').click();
    await page.keyboard.press('Escape');

    await expect(page.getByText('Total estimado: R$ 80,50')).toBeVisible();

    await page.getByRole('button', { name: 'Salvar' }).click();
    await expect(page.getByText('Agendamento criado.', { exact: true })).toBeVisible();

    const linha = page.getByRole('row', { name: /31\/12\/2099/ });
    await expect(linha).toContainText('Tosa, Banho');
    await expect(linha).toContainText('Agendado');
    expect(api.agendamentos.get(pet.id)![0]['dataHora']).toBe('2099-12-31T10:00:00');

    await linha.getByRole('button', { name: 'Ações' }).click();
    await page.locator('.ant-dropdown-menu').getByText('Cancelar agendamento').click();
    await page
      .locator('.ant-modal-confirm')
      .getByRole('button', { name: 'Cancelar agendamento' })
      .click();
    await expect(page.getByText('Agendamento cancelado.', { exact: true })).toBeVisible();
    // Cancelar também faz a exclusão lógica no back-end: o agendamento sai da listagem.
    await expect(linha).toBeHidden();
    await expect(page.getByText('Nenhum agendamento para este pet.')).toBeVisible();
  });
});
