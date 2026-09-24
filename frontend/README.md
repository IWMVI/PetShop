# PetShop — Front-end

Aplicação Angular que consome a API do PetShop (back-end Spring Boot na raiz deste
repositório). Permite gerenciar tutores, pets, serviços e agendamentos.

> O guia geral (banco, back-end e front-end juntos) está no [README da raiz](../README.md).
> Este arquivo detalha só o front-end.

## Sumário

- [Requisitos](#requisitos)
- [Como subir a aplicação](#como-subir-a-aplicação)
- [Como rodar os testes](#como-rodar-os-testes)
- [Como rodar o lint](#como-rodar-o-lint)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Padrões para criar novas telas](#padrões-para-criar-novas-telas)
- [Stack](#stack)

## Requisitos

- Node.js 24 e npm 11
- Back-end rodando em `http://localhost:8080` (veja o README da raiz)

## Como subir a aplicação

1. Suba o banco e o back-end (na raiz do repositório):

   ```bash
   docker compose up -d
   ./gradlew bootRun
   ```

2. Instale as dependências e inicie o front-end (nesta pasta, `frontend/`):

   ```bash
   npm install
   npm start
   ```

3. Acesse `http://localhost:4200`.

O `ng serve` usa o `proxy.conf.json`: toda chamada a `/api/**` é repassada para
`http://localhost:8080` (sem o prefixo `/api`). Por isso não é preciso configurar CORS
no back-end durante o desenvolvimento.

> Alterações no `angular.json` (por exemplo, na lista de estilos) só valem depois de
> reiniciar o `npm start`.

A busca de CEP usa a API pública e gratuita do [ViaCEP](https://viacep.com.br), chamada
direto pelo navegador.

### Build de produção

```bash
npm run build
```

Os arquivos ficam em `dist/frontend/browser/`. Em produção, sirva-os atrás de um proxy
que encaminhe `/api` para o back-end, como o `ng serve` faz em desenvolvimento.

## Como rodar os testes

### Unitários (Jest)

```bash
npm test                 # roda todos os testes
npm run test:watch       # modo observação
npm run test:coverage    # com relatório de cobertura (pasta coverage/)
```

Os testes ficam ao lado do código (`*.spec.ts`). Providers comuns aos testes de
componentes (rotas, animações, ícones, modal) estão em `src/testing/providers.ts`.

### Ponta a ponta (Playwright)

```bash
npx playwright install chromium   # só na primeira vez
npm run e2e                       # roda os testes em modo headless
npm run e2e:ui                    # abre a interface do Playwright
```

- Os testes ficam em `e2e/`.
- **A API é simulada em memória** (`e2e/mock-api.ts`, incluindo paginação, busca e ViaCEP),
  então não é preciso subir o back-end nem o banco.
- O Playwright sobe o próprio servidor na porta **4300**, para não reaproveitar um `npm start`
  que esteja aberto na 4200.

## Como rodar o lint

```bash
npm run lint           # ESLint (TypeScript + templates, incluindo regras de acessibilidade)
npm run lint:fix       # corrige o que for automático
npm run format         # formata tudo com o Prettier
npm run format:check   # só verifica a formatação
```

Para validar tudo de uma vez (formatação, lint, testes unitários e build), como no CI:

```bash
npm run check
```

## Estrutura do projeto

```
src/
├── styles.scss            # ponto de entrada dos estilos globais
├── styles/
│   ├── _tokens.scss       # paleta (tons de amarelo) em variáveis CSS
│   ├── _base.scss         # tipografia e elementos HTML
│   ├── _ng-zorro.scss     # ajustes de tema dos componentes NG-ZORRO
│   └── _layout.scss       # classes de página compartilhadas
├── testing/               # utilitários dos testes unitários
└── app/
    ├── core/              # modelos, clientes da API e consulta de CEP
    ├── shared/            # peças reutilizáveis (veja abaixo)
    └── features/          # telas: tutores, pets, serviços e agendamentos
```

Cada componente tem arquivos próprios de `.ts`, `.html`, `.scss` e `.spec.ts`.

## Padrões para criar novas telas

- **Estrutura da página:** envolva a tela em `<app-pagina>` (`shared/pagina`). Ele monta a
  trilha de navegação, o título e a área de ações:

  ```html
  <app-pagina titulo="Serviços" [trilha]="trilha()">
    <a paginaAcoes nz-button nzType="primary" class="btn-novo" routerLink="novo">Novo serviço</a>
    <span paginaResumo>Texto opcional abaixo do título</span>
    <!-- conteúdo da tela -->
  </app-pagina>
  ```

- **Listagens:** use `listagemPaginada()` (`shared/listagem`) com um `nz-table` configurado
  com `[nzFrontPagination]="false"`. Ela carrega do servidor só a página exibida
  (10 itens) e aplica a busca com debounce.
- **Ações por linha:** use um menu hambúrguer (`nz-dropdown` com o ícone `menu`). Para
  ações destrutivas, peça confirmação com o `ConfirmacaoService`.
- **Botões "Novo …":** `nz-button nzType="primary"` com a classe `btn-novo`, para que todos
  tenham a mesma largura.
- **Formulários:** use `nz-form` em layout vertical, com cada campo em
  `<div nz-col>` e um `<nz-form-item>` dentro, para o espaçamento do grid funcionar.
  Máscaras: diretiva `appMascara` com `MASCARAS.cpf`, `MASCARAS.cep` ou `MASCARAS.telefone`.
  Valores em reais: diretiva `appMoeda`.
- **Avisos:** use o `ToastService` (`sucesso`/`erro`) e `mensagemDeErro()` para converter
  os erros da API.
- **Ícones:** registre os ícones Lucide novos em `shared/icons.ts`.

## Stack

| Camada           | Tecnologia                                   |
| ---------------- | -------------------------------------------- |
| Framework        | Angular 20 (componentes standalone, signals) |
| Componentes      | NG-ZORRO (Ant Design para Angular)           |
| Ícones           | Lucide (`lucide-angular`)                    |
| Testes unitários | Jest + `jest-preset-angular`                 |
| Testes E2E       | Playwright (Chromium)                        |
| Lint e formato   | ESLint (`angular-eslint`) + Prettier         |
