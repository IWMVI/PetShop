# PetShop

API de gerenciamento de um petshop, construída com **Spring Boot** e **Java 25**.
Permite o cadastro de tutores e de seus pets, com validações, persistência
via JPA/Hibernate e migrações versionadas com Flyway.

## Stack

| Camada        | Tecnologia                                      |
|---------------|-------------------------------------------------|
| Linguagem     | Java 25                                         |
| Framework     | Spring Boot 4.1.1                               |
| Persistência  | Spring Data JPA (Hibernate)                      |
| Banco         | PostgreSQL 17 (docker-compose)                  |
| Migrações     | Flyway                                          |
| Validação     | Jakarta Validation (Hibernate Validator)        |
| Documentação  | springdoc-openapi (Swagger UI)                  |
| Testes        | JUnit 5, Mockito, Cucumber (BDD), H2            |
| Build         | Gradle                                          |
| Outros        | Lombok, Spring Boot Actuator, DevTools          |

## Estrutura do projeto
Obs: Será atualizado conforme o projeto evoluir.

```
src/main/java/br/iwmvi/petshop
├── config/          # DatabaseInitializer (cria o banco automaticamente)
├── exception/       # Exceções de domínio (EmailJaCadastradoException, TutorNotFoundException)
├── tutor/           # Entidade de tutor e endpoints
│   ├── controller/  # TutorController
│   ├── dto/         # TutorRequest / TutorResponse
│   ├── mapper/      # TutorMapper
│   ├── model/       # Tutor
│   ├── repository/  # TutorRepository
│   └── service/     # TutorService
├── endereco/        # Entidade Endereco (tabela própria)
│   ├── dto/
│   ├── mapper/
│   └── model/
└── pet/             # Entidade Pet vinculada ao Tutor
    ├── controller/  # PetController
    ├── dto/
    ├── mapper/
    ├── model/
    ├── repository/
    └── service/
```

## Requisitos

- JDK 25
- Docker (para o PostgreSQL local)
- Gradle (via `./gradlew`, sem instalação)

## Como rodar

1. Suba o banco de dados:

   ```bash
   docker compose up -d
   ```

   Isso inicia o PostgreSQL na porta `5432` com o banco `petshop`
   (usuário/senha `postgres`/`postgres`).

2. Inicie a aplicação:

   ```bash
   ./gradlew bootRun
   ```

   A aplicação sobe em `http://localhost:8080`.

O perfil ativo por padrão é `dev`. O `DatabaseInitializer` cria o banco
`petshop` automaticamente caso ele não exista, e o Flyway aplica as
migrações na inicialização.

## Perfis

| Perfil | Descrição                                                            |
|--------|----------------------------------------------------------------------|
| `dev`  | Padrão. Aponta para `localhost:5432`, Flyway habilitado, SQL visível |
| `prod` | Conexão obrigatória por `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`       |
| `test` | Usado pela suíte de testes (H2 em modo PostgreSQL, Flyway)           |

Variáveis de ambiente configuráveis no perfil `dev`:

| Variável      | Padrão     | Descrição                     |
|---------------|------------|-------------------------------|
| `DB_HOST`     | `localhost`| Host do PostgreSQL            |
| `DB_PORT`     | `5432`     | Porta do PostgreSQL           |
| `DB_NAME`     | `petshop`  | Nome do banco de dados        |
| `DB_USERNAME` | `postgres` | Usuário do banco              |
| `DB_PASSWORD` | `postgres` | Senha do banco                |

### Exemplo — cadastrar tutor

```bash
curl -X POST http://localhost:8080/tutores \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Wallace",
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

### Exemplo — cadastrar pet

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

## Documentação (Swagger)

Com a aplicação rodando:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testes

A suíte inclui testes unitários (JUnit 5 + Mockito) e BDD (Cucumber),
executados com H2 em modo PostgreSQL no perfil `test`.

```bash
./gradlew test
```