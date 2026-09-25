import { expect, test } from '@playwright/test';
import { MockApi } from './mock-api';

test.describe('Menu lateral', () => {
  test('centraliza os ícones, na horizontal e na vertical, quando recolhido', async ({ page }) => {
    await MockApi.instalar(page);
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto('/tutores');
    await page.locator('.ant-layout-sider-trigger').click();
    await expect(page.locator('nz-sider')).toHaveCSS('width', '64px');

    const itens = page.locator('nz-sider li[nz-menu-item]');
    await expect(itens).toHaveCount(3);
    for (const item of await itens.all()) {
      const caixa = (await item.boundingBox())!;
      const link = (await item.locator('a').boundingBox())!;
      const icone = (await item.locator('lucide-icon').boundingBox())!;
      const centro = (c: { x: number; y: number; width: number; height: number }) => ({
        x: c.x + c.width / 2,
        y: c.y + c.height / 2,
      });

      expect(centro(icone).x).toBeCloseTo(centro(caixa).x, 0);
      expect(centro(icone).y).toBeCloseTo(centro(caixa).y, 0);
      // O link preenche o item, então tocar em qualquer ponto dele navega.
      expect(link.width).toBeCloseTo(caixa.width, 0);
      expect(link.height).toBeCloseTo(caixa.height, 0);
    }
  });

  test('mostra o rótulo ao lado do ícone quando expandido', async ({ page }) => {
    await MockApi.instalar(page);
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto('/tutores');

    const item = page.locator('nz-sider li[nz-menu-item]').first();
    await expect(item).toContainText('Tutores');
    const icone = (await item.locator('lucide-icon').boundingBox())!;
    const rotulo = (await item.locator('.rotulo').boundingBox())!;
    expect(rotulo.x).toBeGreaterThan(icone.x + icone.width);
    expect(icone.y + icone.height / 2).toBeCloseTo(rotulo.y + rotulo.height / 2, -1);
  });
});
