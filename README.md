# PetShop

[![back-end](https://img.shields.io/github/actions/workflow/status/IWMVI/PetShop/backend.yml?branch=main&label=back-end&logo=githubactions&style=flat&labelColor=2b2214&logoColor=f5b800)](https://github.com/IWMVI/PetShop/actions/workflows/backend.yml) [![front-end](https://img.shields.io/github/actions/workflow/status/IWMVI/PetShop/frontend.yml?branch=main&label=front-end&logo=githubactions&style=flat&labelColor=2b2214&logoColor=f5b800)](https://github.com/IWMVI/PetShop/actions/workflows/frontend.yml) [![Conventional Commits 1.0.0](https://img.shields.io/badge/Conventional%20Commits-1.0.0-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=conventionalcommits)](https://www.conventionalcommits.org/pt-br/v1.0.0/)

[![Java 25](https://img.shields.io/badge/Java-25-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=openjdk)](https://openjdk.org/projects/jdk/25/) [![Spring Boot 4.1](https://img.shields.io/badge/Spring%20Boot-4.1-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=springboot)](https://spring.io/projects/spring-boot) [![PostgreSQL 17](https://img.shields.io/badge/PostgreSQL-17-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=postgresql)](https://www.postgresql.org/) [![Angular 20](https://img.shields.io/badge/Angular-20-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=angular)](https://angular.dev/) [![NG-ZORRO 20](https://img.shields.io/badge/NG--ZORRO-20-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=antdesign)](https://ng.ant.design/) [![Node.js 24](https://img.shields.io/badge/Node.js-24-f5b800?style=flat&labelColor=2b2214&logoColor=f5b800&logo=nodedotjs)](https://nodejs.org/)

Sistema de gerenciamento de um petshop: cadastro de tutores e seus pets, catálogo de
serviços e agendamentos. O repositório tem duas partes, cada uma no seu diretório:

| Parte     | Pasta                    | Tecnologia                                 | Porta  |
| --------- | ------------------------ | ------------------------------------------ | ------ |
| Back-end  | [`backend/`](backend/)   | Java 25, Spring Boot 4, PostgreSQL, Flyway | `8080` |
| Front-end | [`frontend/`](frontend/) | Angular 20, NG-ZORRO, ícones Lucide        | `4200` |

## Sumário

- [Requisitos](#requisitos)
- [Como subir a aplicação](#como-subir-a-aplicação)
- [Como rodar os testes](#como-rodar-os-testes)
- [Como rodar o lint](#como-rodar-o-lint)
- [Comandos rápidos](#comandos-rápidos)
- [Configuração](#configuração)
- [API](#api)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Stack](#stack)

## Requisitos

| Ferramenta    | Versão  | Para quê                                 |
| ------------- | ------- | ---------------------------------------- |
| JDK           | 25      | Back-end (o Gradle vem pelo `./gradlew`) |
| Docker        | —       | PostgreSQL local (`docker compose`)      |
| Node.js e npm | 24 e 11 | Front-end                                |

## Como subir a aplicação

1. **Banco de dados** (na raiz do repositório):

   ```bash
   docker compose up -d
   ```

   Sobe o PostgreSQL 17 na porta `5432`, com o banco `petshop` (usuário e senha:
   `postgres`/`postgres`).

2. **Back-end** (em outro terminal):

   ```bash
   cd backend
   ./gradlew bootRun
   ```

   A API sobe em `http://localhost:8080`, no perfil `dev`. O `DatabaseInitializer` cria
   o banco `petshop` se ele não existir, e o Flyway aplica as migrações na inicialização.

3. **Front-end** (em um terceiro terminal):

   ```bash
   cd frontend
   npm install      # só na primeira vez ou quando as dependências mudarem
   npm start
   ```

   Acesse `http://localhost:4200`. O `npm start` usa um proxy (`frontend/proxy.conf.json`)
   que repassa `/api/**` para `http://localhost:8080`, então não é preciso configurar CORS.

> Mudanças no `frontend/angular.json` só passam a valer depois de reiniciar o `npm start`.

Para parar: `Ctrl+C` no back-end e no front-end, e `docker compose down` para o banco. Os
dados continuam no volume `petshop-pgdata`.

### Build de produção

```bash
cd backend && ./gradlew bootJar     # back-end: backend/build/libs/*.jar (perfil prod: DB_URL, DB_USERNAME, DB_PASSWORD)
cd frontend && npm run build        # front-end: frontend/dist/frontend/browser/
```

Em produção, sirva o front-end atrás de um proxy que encaminhe `/api` para o back-end,
como o `npm start` faz em desenvolvimento.

## Como rodar os testes

### Back-end: unitários, integração e BDD

```bash
cd backend
./gradlew test
```

- Roda os testes unitários (JUnit 5 e Mockito), os de integração e os cenários BDD
  (Cucumber, em `backend/src/test/resources/**/*.feature`).
- Usa H2 em memória no modo PostgreSQL (perfil `test`), com as mesmas migrações Flyway.
  **Não precisa do Docker.**
- Relatório: `backend/build/reports/tests/test/index.html`.

Para rodar só uma classe ou um grupo (dentro de `backend/`):

```bash
./gradlew test --tests '*TutorServiceTest'
./gradlew test --tests '*Expurgo*'
```

### Front-end: unitários (Jest)

```bash
cd frontend
npm test                 # todos os testes
npm run test:watch       # modo observação
npm run test:coverage    # com cobertura (frontend/coverage/)
```

### Front-end: ponta a ponta (Playwright)

```bash
cd frontend
npx playwright install chromium   # só na primeira vez
npm run e2e                       # headless
npm run e2e:ui                    # interface visual do Playwright
```

- **A API é simulada em memória** (`frontend/e2e/mock-api.ts`), então não é preciso subir
  o back-end nem o banco.
- O Playwright sobe o próprio servidor na porta `4300`, sem conflitar com um `npm start`
  aberto na `4200`.

## Como rodar o lint

O lint e a formatação estão configurados no **front-end**: ESLint com `angular-eslint`,
incluindo regras de acessibilidade nos templates, e Prettier.

```bash
cd frontend
npm run lint            # ESLint
npm run lint:fix        # corrige automaticamente o que for possível
npm run format          # formata com o Prettier
npm run format:check    # só verifica a formatação
```

Para validar o front-end de uma vez (formatação, lint, testes unitários e build):

```bash
cd frontend
npm run check
```

> O **back-end não tem linter configurado** (nem Checkstyle nem Spotless). A validação
> dele é feita pela compilação e pelos testes (`./gradlew build`, em `backend/`).

## Comandos rápidos

| O quê                               | Onde        | Comando                |
| ----------------------------------- | ----------- | ---------------------- |
| Subir o banco                       | raiz        | `docker compose up -d` |
| Subir o back-end                    | `backend/`  | `./gradlew bootRun`    |
| Subir o front-end                   | `frontend/` | `npm start`            |
| Testes do back-end                  | `backend/`  | `./gradlew test`       |
| Testes unitários do front-end       | `frontend/` | `npm test`             |
| Testes E2E do front-end             | `frontend/` | `npm run e2e`          |
| Lint do front-end                   | `frontend/` | `npm run lint`         |
| Formatação do front-end             | `frontend/` | `npm run format`       |
| Validação completa do front-end     | `frontend/` | `npm run check`        |
| Build do back-end (compila e testa) | `backend/`  | `./gradlew build`      |

## Configuração

### Perfis do back-end

| Perfil | Descrição                                                            |
| ------ | -------------------------------------------------------------------- |
| `dev`  | Padrão. Aponta para `localhost:5432`, Flyway habilitado, SQL visível |
| `prod` | Conexão obrigatória por `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`      |
| `test` | Usado pelos testes (H2 em modo PostgreSQL, Flyway)                   |

Variáveis de ambiente do perfil `dev`:

| Variável      | Padrão      | Descrição           |
| ------------- | ----------- | ------------------- |
| `DB_HOST`     | `localhost` | Host do PostgreSQL  |
| `DB_PORT`     | `5432`      | Porta do PostgreSQL |
| `DB_NAME`     | `petshop`   | Nome do banco       |
| `DB_USERNAME` | `postgres`  | Usuário do banco    |
| `DB_PASSWORD` | `postgres`  | Senha do banco      |

### Expurgo de tutores excluídos

A exclusão de tutor é lógica (`deleted_at`), e até 30 dias ele pode ser recuperado pelo
CPF. Um job diário (`ExpurgoTutoresJob`) apaga **definitivamente** os tutores excluídos
há **mais de 30 dias**, com tudo o que depende deles: pets, histórico dos pets,
agendamentos (com serviços e pagamentos) e endereço. Os serviços do catálogo não são
afetados.

| Propriedade                             | Padrão        | Descrição                         |
| --------------------------------------- | ------------- | --------------------------------- |
| `petshop.expurgo-tutores.dias-retencao` | `30`          | Dias até a exclusão definitiva    |
| `petshop.expurgo-tutores.cron`          | `0 0 3 * * *` | Horário do job (diário, às 03:00) |

## API

Com o back-end rodando, a documentação interativa fica em:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

### Endpoints

| Recurso      | Endpoints                                                                               |
| ------------ | --------------------------------------------------------------------------------------- |
| Tutores      | `GET/POST /tutores`, `GET/PUT/DELETE /tutores/{id}`                                     |
|              | `GET /tutores/cpf/{cpf}` (inclui excluídos), `POST /tutores/{id}/restaurar`             |
| Pets         | `GET/POST /tutores/{tutorId}/pets`, `GET/PUT/DELETE /tutores/{tutorId}/pets/{petId}`    |
| Serviços     | `GET/POST /servicos`, `GET/PUT/DELETE /servicos/{id}`                                   |
| Agendamentos | `GET/POST /pets/{petId}/agendamentos`, `GET/PUT/DELETE /pets/{petId}/agendamentos/{id}` |

Os erros de negócio retornam `{ "mensagem": "..." }`: `404` para registro não encontrado,
`409` para e-mail ou CPF já cadastrado, `400` para dados inválidos.

### Listagens paginadas

`GET /tutores`, `GET /servicos` e `GET /pets/{petId}/agendamentos` são paginados:

| Parâmetro | Padrão | Descrição                                                             |
| --------- | ------ | --------------------------------------------------------------------- |
| `pagina`  | `0`    | Índice da página (começa em 0)                                        |
| `tamanho` | `10`   | Itens por página (máximo 50)                                          |
| `busca`   | —      | Tutores: nome, e-mail, logradouro ou CPF. Serviços: nome ou descrição |

```bash
curl "http://localhost:8080/tutores?pagina=0&tamanho=10&busca=wallace"
```

```json
{ "itens": [ ... ], "pagina": 0, "tamanho": 10, "total": 1, "totalPaginas": 1 }
```

### Exemplos

Cadastrar tutor. O CPF é obrigatório, validado pelos dígitos verificadores e salvo só com
dígitos. CPF repetido, inclusive de tutor excluído, retorna `409`.

```bash
curl -X POST http://localhost:8080/tutores \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Wallace",
    "cpf": "529.982.247-25",
    "email": "wallace@test.com",
    "telefone": "11111111111",
    "endereco": {
      "cep": "01001-001",
      "logradouro": "Praça da Sé",
      "numero": "1",
      "complemento": "Lado Ímpar",
      "bairro": "Sé",
      "cidade": "São Paulo",
      "estado": "SP"
    }
  }'
```

Recuperar tutor excluído pelo CPF:

```bash
curl http://localhost:8080/tutores/cpf/52998224725     # { "id", "nome", "email", "excluido" }
curl -X POST http://localhost:8080/tutores/1/restaurar
```

Cadastrar pet:

```bash
curl -X POST http://localhost:8080/tutores/1/pets \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Rex",
    "especie": "Cachorro",
    "raca": "Labrador",
    "idade": 3,
    "peso": 25.5
  }'
```

Agendar serviços para um pet. Cancelar (`DELETE`) muda o status para `CANCELADO` e
remove o agendamento das listagens.

```bash
curl -X POST http://localhost:8080/pets/1/agendamentos \
  -H "Content-Type: application/json" \
  -d '{
    "dataHora": "2099-12-31T10:00:00",
    "observacoes": "Levar com coleira",
    "servicoIds": [1, 2]
  }'
```

## Estrutura do projeto

```
.
├── README.md                    # este guia
├── docker-compose.yml           # PostgreSQL local (usado pelo back-end)
├── docs/                        # diagramas Mermaid (arquitetura, classes, entidades)
├── backend/                     # API Spring Boot (projeto Gradle)
│   ├── build.gradle
│   ├── gradlew
│   └── src/
│       ├── main/java/br/iwmvi/petshop
│       │   ├── common/          # base CRUD (controller, serviço, repositório com soft delete), paginação
│       │   ├── config/          # JPA, criação do banco, tarefas agendadas
│       │   ├── exception/       # exceções de domínio e GlobalExceptionHandler
│       │   ├── tutor/           # tutores (inclui o job de expurgo em tutor/job)
│       │   ├── endereco/        # endereço do tutor
│       │   ├── pet/             # pets de cada tutor
│       │   ├── servico/         # catálogo de serviços
│       │   └── agendamento/     # agendamentos (validadores em agendamento/validator)
│       ├── main/resources/db/migration/   # migrações Flyway (V1…V14)
│       └── test/                # testes JUnit, integração e Cucumber (.feature)
└── frontend/                    # aplicação Angular (veja frontend/README.md)
```

Cada módulo do back-end segue a mesma divisão: `controller/`, `dto/`, `mapper/`,
`model/`, `repository/` e `service/`.

Os padrões para criar telas no front-end (componente de página, listagens paginadas,
formulários, máscaras) estão no [README do front-end](frontend/README.md).

## Stack

| Camada                | Tecnologia                                                  |
| --------------------- | ----------------------------------------------------------- |
| Linguagem (back-end)  | Java 25                                                     |
| Framework (back-end)  | Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, Actuator) |
| Banco e migrações     | PostgreSQL 17 (Docker) e Flyway                             |
| Documentação da API   | springdoc-openapi (Swagger UI)                              |
| Testes (back-end)     | JUnit 5, Mockito, Cucumber (BDD), H2                        |
| Build (back-end)      | Gradle                                                      |
| Framework (front-end) | Angular 20 (componentes standalone e signals)               |
| Componentes e ícones  | NG-ZORRO e Lucide                                           |
| Testes (front-end)    | Jest e Playwright                                           |
| Lint (front-end)      | ESLint (`angular-eslint`) e Prettier                        |
