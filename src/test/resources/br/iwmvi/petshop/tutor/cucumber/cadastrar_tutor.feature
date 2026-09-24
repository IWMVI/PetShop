# language: pt

Funcionalidade: Cadastro de tutor
  Como usuário do PetShop quero cadastrar tutores para associar pet's aos seus responsáveis.

  Cenário: Cadastrar tutor com dados válidos
    Quando cadastrar um tutor com os seguintes dados:
      | nome       | Wallace          |
      | email      | wallace@test.com |
      | telefone   | 11111111111      |
      | cep        | 01001-001        |
      | logradouro | Praça da Sé      |
      | numero     | 1                |
      | bairro     | Sé               |
      | cidade     | São Paulo        |
      | estado     | SP               |
    Então o cadastro do tutor deve retornar o status 201
    E o tutor cadastrado deve possuir um identificador

  Cenário: Não cadastrar tutor com e-mail inválido
    Quando tentar cadastrar um tutor com o e-mail "invalido"
    Então o cadastro do tutor deve retornar o status 400

  Cenário: Não cadastrar tutor com e-mail já cadastrado
    Dado que existe um tutor cadastrado com o e-mail "duplicado@test.com"
    Quando tentar cadastrar um tutor com o e-mail "duplicado@test.com"
    Então o cadastro do tutor deve retornar o status 409

  Cenário: Não cadastrar tutor com CEP inválido
    Quando tentar cadastrar um tutor com o CEP "123"
    Então o cadastro do tutor deve retornar o status 400

  Cenário: Não cadastrar tutor com CPF inválido
    Quando tentar cadastrar um tutor com o CPF "123.456.789-00"
    Então o cadastro do tutor deve retornar o status 400

  Cenário: Não cadastrar tutor com CPF já cadastrado
    Dado que existe um tutor cadastrado com o CPF "529.982.247-25"
    Quando tentar cadastrar um tutor com o CPF "52998224725"
    Então o cadastro do tutor deve retornar o status 409
