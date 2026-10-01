import { defineConfig, devices } from '@playwright/test';

/**
 * Testes E2E do front-end. A API é simulada em memória (e2e/mock-api.ts),
 * então não é preciso subir o back-end nem o PostgreSQL.
 * Usa a porta 4300 para não reaproveitar o `ng serve` de desenvolvimento (4200).
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  reporter: process.env['CI'] ? 'github' : 'list',
  use: {
    baseURL: 'http://localhost:4300',
    locale: 'pt-BR',
    timezoneId: 'America/Sao_Paulo',
    trace: 'on-first-retry',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm start -- --port 4300',
    url: 'http://localhost:4300',
    reuseExistingServer: !process.env['CI'],
    timeout: 120_000,
  },
});
