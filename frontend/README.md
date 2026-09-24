# PetShop — Front-end

[![front-end](https://img.shields.io/github/actions/workflow/status/IWMVI/PetShop/frontend.yml?branch=main&label=front-end&logo=githubactions&style=flat&labelColor=2b2214&logoColor=f5b800)](https://github.com/IWMVI/PetShop/actions/workflows/frontend.yml) [![Conventional Commits 1.0.0](https://img.shields.io/badge/Conventional%20Commits-1.0.0-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=conventionalcommits)](https://www.conventionalcommits.org/pt-br/v1.0.0/)

[![Angular 20](https://img.shields.io/badge/Angular-20-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=angular)](https://angular.dev/) [![NG-ZORRO 20](https://img.shields.io/badge/NG--ZORRO-20-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=antdesign)](https://ng.ant.design/) [![Node.js 24](https://img.shields.io/badge/Node.js-24-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=nodedotjs)](https://nodejs.org/) [![Jest 30](https://img.shields.io/badge/Jest-30-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=jest)](https://jestjs.io/) [![Playwright 1.63](https://img.shields.io/badge/Playwright-1.63-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800)](https://playwright.dev/)

Aplicação Angular que consome a API do PetShop (back-end Spring Boot em `backend/`, neste
mesmo repositório). Permite gerenciar tutores, pets, serviços e agendamentos.

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

1. Suba o banco e o back-end (a partir da raiz do repositório):

   ```bash
   docker compose up -d
   cd backend && ./gradlew bootRun
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

### Acesso por outros dispositivos na mesma rede

O `npm start` sobe o servidor de desenvolvimento em `0.0.0.0:4200` (veja a opção `host`
em `angular.json`), então a aplicação também pode ser acessada por outros dispositivos
conectados à mesma rede local, usando o IP da máquina que está rodando o front-end e o
back-end (ex.: `http://192.168.0.10:4200`). O proxy `/api` continua funcionando normalmente,
pois é resolvido pelo próprio processo do `ng serve`, sempre em relação a `localhost:8080`
na mesma máquina.

Veja o [README da raiz](../README.md#acesso-por-outros-dispositivos-na-mesma-rede) para o
passo a passo completo, incluindo como liberar as portas no firewall.

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
- **Status:** exiba qualquer status com `<app-status>` (`shared/status`), escolhendo o tom:
  `destaque` (amarelo), `sucesso`, `alerta`, `erro` ou `neutro`. Ex.:
  `<app-status tom="sucesso">Concluído</app-status>`. O menu mostra a saúde da API
  (`/actuator/health`) com o mesmo componente.
- **Celular:** abaixo de 768px o menu lateral vira uma gaveta aberta pelo botão ☰. Nas
  tabelas, marque colunas secundárias com `ocultar-celular` (< 768px) ou `ocultar-pequeno`
  (< 576px), no `<th>` e no `<td>`, e use `so-pequeno` para repetir a informação essencial
  em outra coluna. Botões do cabeçalho e dos formulários ocupam a largura toda
  automaticamente. Para saber o tamanho da tela no código, use o `TelaService`.
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
