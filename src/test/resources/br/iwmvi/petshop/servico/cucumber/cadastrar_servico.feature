# language: pt

Funcionalidade: Cadastro de serviço
  Como gestor do PetShop quero cadastrar serviços para oferecê-los aos tutores.

  Cenário: Cadastrar serviço com dados válidos
    Quando cadastrar um serviço com os seguintes dados:
      | nome                     | Banho e Tosa                 |
      | descricao                | Banho completo com tosa      |
      | preco                    | 150.00                       |
      | tempoEstimadoMinutos     | 60                           |
    Então o cadastro do serviço deve retornar o status 201
    E o serviço cadastrado deve possuir um identificador

  Cenário: Não cadastrar serviço com nome inválido
    Quando tentar cadastrar um serviço com nome vazio
    Então o cadastro do serviço deve retornar o status 400

  Cenário: Não cadastrar serviço com preço inválido
    Quando tentar cadastrar um serviço com preço "0.00"
    Então o cadastro do serviço deve retornar o status 400

  Cenário: Não cadastrar serviço com preço acima do limite
    Quando tentar cadastrar um serviço com preço "10000.00"
    Então o cadastro do serviço deve retornar o status 400

  Cenário: Cadastrar serviço sem tempo estimado
    Quando cadastrar um serviço com os seguintes dados:
      | nome                     | Vacinação                    |
      | descricao                | Aplicação de vacina          |
      | preco                    | 80.00                        |
      | tempoEstimadoMinutos     |                              |
    Então o cadastro do serviço deve retornar o status 201
