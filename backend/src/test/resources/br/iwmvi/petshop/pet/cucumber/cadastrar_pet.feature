# language: pt

Funcionalidade: Cadastro de pet
  Como usuário do PetShop quero cadastrar pets para gerenciar os animais de estimação dos tutores.

  Cenário: Cadastrar pet com dados válidos
    Dado que existe um tutor cadastrado
    Quando cadastrar um pet com os seguintes dados:
      | nome    | Fluffy    |
      | especie | Gato      |
      | raca    | Persa     |
      | idade   | 2         |
      | peso    | 5.50      |
    Então o cadastro do pet deve retornar o status 201
    E o pet cadastrado deve possuir um identificador

  Cenário: Não cadastrar pet com nome inválido
    Dado que existe um tutor cadastrado
    Quando tentar cadastrar um pet com nome vazio
    Então o cadastro do pet deve retornar o status 400

  Cenário: Não cadastrar pet quando tutor não existe
    Quando tentar cadastrar um pet para um tutor inexistente
    Então o cadastro do pet deve retornar o status 404

  Cenário: Não cadastrar pet com peso inválido
    Dado que existe um tutor cadastrado
    Quando tentar cadastrar um pet com peso "0.00"
    Então o cadastro do pet deve retornar o status 400

  Cenário: Não cadastrar pet com especie inválida
    Dado que existe um tutor cadastrado
    Quando tentar cadastrar um pet com especie vazia
    Então o cadastro do pet deve retornar o status 400
