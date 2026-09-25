import { expect, test } from '@playwright/test';
import { MockApi } from './mock-api';

test.describe('Histórico do pet', () => {
  test('mostra o estado vazio e registra um evento com funcionário', async ({ page }) => {
    const api = await MockApi.instalar(page);
    const tutor = api.tutor();
    const pet = api.pet(tutor.id, { nome: 'Rex' });
    api.funcionario({ nome: 'Dra. Ana', cargo: 'VETERINARIO' });

    await page.goto(`/tutores/${tutor.id}/pets/${pet.id}`);
    await expect(page.getByText('Nenhum evento registrado para este pet.')).toBeVisible();
    await page.getByRole('link', { name: 'Novo evento' }).click();

    await page.getByRole('button', { name: 'Registrar evento' }).click();
    await expect(page.getByText('Selecione o tipo do evento.')).toBeVisible();
    await expect(page.getByText('Informe a data e a hora do evento.')).toBeVisible();
    await expect(page.getByText('Descreva o que aconteceu.')).toBeVisible();

    await page.getByLabel('Tipo do evento').click();
    await page.locator('.ant-select-item-option').filter({ hasText: 'Vacinação' }).click();

    const data = page.getByLabel('Data e hora do evento');
    await data.click();
    await data.pressSequentially('10/01/2026 14:30');
    await page.locator('.ant-picker-ok').getByRole('button').click();
    await expect(data).toHaveValue('10/01/2026 14:30');

    const funcionario = page.getByLabel('Funcionário');
    await funcionario.click();
    await page.locator('.ant-select-item-option').filter({ hasText: 'Dra. Ana' }).click();

    await page.getByLabel('Descrição').fill('Vacina V10 - primeira dose');
    await page.getByRole('button', { name: 'Registrar evento' }).click();

    await expect(page.getByText('Evento registrado no histórico.', { exact: true })).toBeVisible();
    const linha = page.getByRole('row', { name: /Vacina V10/ });
    await expect(linha).toContainText('10/01/2026');
    await expect(linha).toContainText('Vacinação');
    await expect(linha).toContainText('Dra. Ana');
    expect(api.historico.get(pet.id as number)).toHaveLength(1);
  });

  test('registra um evento externo sem funcionário', async ({ page }) => {
    const api = await MockApi.instalar(page);
    const tutor = api.tutor();
    const pet = api.pet(tutor.id);

    await page.goto(`/tutores/${tutor.id}/pets/${pet.id}/historico/novo`);
    await page.getByLabel('Tipo do evento').click();
    await page.locator('.ant-select-item-option').filter({ hasText: 'Outro' }).click();
    const data = page.getByLabel('Data e hora do evento');
    await data.click();
    await data.pressSequentially('05/03/2026 09:00');
    await page.locator('.ant-picker-ok').getByRole('button').click();
    await page.getByLabel('Descrição').fill('Vacina aplicada em outra clínica');
    await page.getByRole('button', { name: 'Registrar evento' }).click();

    await expect(page.getByText('Evento registrado no histórico.', { exact: true })).toBeVisible();
    const linha = page.getByRole('row', { name: /outra clínica/ });
    await expect(linha).toContainText('Outro');
    await expect(linha).toContainText('—');
  });

  test('lista os eventos do mais recente para o mais antigo', async ({ page }) => {
    const api = await MockApi.instalar(page);
    const tutor = api.tutor();
    const pet = api.pet(tutor.id);
    const lista = api.historico.get(pet.id as number)!;
    const base = { petId: pet.id, funcionarioId: null, funcionarioNome: null, createdAt: '' };
    lista.push(
      {
        ...base,
        id: 1,
        tipoEvento: 'CONSULTA',
        descricao: 'Consulta antiga',
        dataEvento: '2025-01-10T10:00:00',
      },
      {
        ...base,
        id: 2,
        tipoEvento: 'PROCEDIMENTO',
        descricao: 'Cirurgia recente',
        dataEvento: '2026-02-10T10:00:00',
      },
    );

    await page.goto(`/tutores/${tutor.id}/pets/${pet.id}`);
    const corpo = page.locator('tbody').last();
    await expect(corpo.getByRole('row').first()).toContainText('Cirurgia recente');
    await expect(corpo.getByRole('row').last()).toContainText('Consulta antiga');
  });

  test('é imutável: os eventos não oferecem ação de editar nem excluir', async ({ page }) => {
    const api = await MockApi.instalar(page);
    const tutor = api.tutor();
    const pet = api.pet(tutor.id);
    api.historico.get(pet.id as number)!.push({
      id: 1,
      petId: pet.id,
      tipoEvento: 'CONSULTA',
      descricao: 'Consulta de rotina',
      dataEvento: '2026-01-10T10:00:00',
      funcionarioId: null,
      funcionarioNome: null,
      createdAt: '',
    });

    await page.goto(`/tutores/${tutor.id}/pets/${pet.id}`);
    const linha = page.getByRole('row', { name: /Consulta de rotina/ });
    await expect(linha).toBeVisible();
    await expect(linha.getByRole('button')).toHaveCount(0);
    await expect(linha.getByRole('link')).toHaveCount(0);
  });

  test('avisa quando a API recusa o evento', async ({ page }) => {
    const api = await MockApi.instalar(page);
    const tutor = api.tutor();
    const pet = api.pet(tutor.id);
    // Simula a API recusando o evento (a tela já barra datas futuras, mas a regra é do servidor).
    await page.route('**/api/pets/*/historico', (route) =>
      route.request().method() === 'POST'
        ? route.fulfill({
            status: 400,
            contentType: 'application/json',
            body: JSON.stringify({ mensagem: 'Data do evento não pode estar no futuro.' }),
          })
        : route.fallback(),
    );

    await page.goto(`/tutores/${tutor.id}/pets/${pet.id}/historico/novo`);
    await page.getByLabel('Tipo do evento').click();
    await page.locator('.ant-select-item-option').filter({ hasText: 'Consulta' }).click();
    const data = page.getByLabel('Data e hora do evento');
    await data.click();
    await data.pressSequentially('10/01/2026 14:30');
    await page.locator('.ant-picker-ok').getByRole('button').click();
    await page.getByLabel('Descrição').fill('Retorno');
    await page.getByRole('button', { name: 'Registrar evento' }).click();

    await expect(page.getByText('Data do evento não pode estar no futuro.')).toBeVisible();
    await expect(page).toHaveURL(/historico\/novo$/);
  });
});
