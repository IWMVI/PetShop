# language: pt

Funcionalidade: Histórico de eventos do pet
  Como usuário do PetShop quero registrar os eventos da vida do pet,
  mantendo um histórico que não pode ser alterado nem excluído.

  Cenário: Registrar um evento no histórico do pet
    Dado que existe um pet cadastrado
    Quando registrar um evento do tipo "VACINACAO" com a descrição "Vacina V10"
    Então a resposta deve retornar o status 201
    E o evento retornado deve possuir um identificador
    E o evento retornado deve informar o funcionário "Dra. Ana"

  Cenário: Registrar um evento sem funcionário
    Dado que existe um pet cadastrado
    Quando registrar um evento sem funcionário
    Então a resposta deve retornar o status 201
    E o evento retornado não deve possuir funcionário

  Cenário: Listar o histórico do pet
    Dado que existe um pet cadastrado com um evento no histórico
    Quando listar o histórico do pet
    Então a resposta deve retornar o status 200
    E o histórico deve conter 1 evento

  Cenário: Consultar um evento específico
    Dado que existe um pet cadastrado com um evento no histórico
    Quando consultar o evento registrado
    Então a resposta deve retornar o status 200
    E o evento retornado deve possuir um identificador

  Cenário: Manter o funcionário no histórico após o desligamento
    Dado que existe um pet cadastrado com um evento no histórico
    E o funcionário foi desligado
    Quando consultar o evento registrado
    Então a resposta deve retornar o status 200
    E o evento retornado deve informar o funcionário "Dra. Ana"

  Cenário: Não consultar evento inexistente
    Dado que existe um pet cadastrado
    Quando tentar consultar um evento inexistente
    Então a resposta deve retornar o status 404

  Cenário: Não registrar evento com data no futuro
    Dado que existe um pet cadastrado
    Quando tentar registrar um evento com data no futuro
    Então a resposta deve retornar o status 400

  Cenário: Não registrar evento com descrição vazia
    Dado que existe um pet cadastrado
    Quando registrar um evento do tipo "CONSULTA" com a descrição ""
    Então a resposta deve retornar o status 400

  Cenário: Não registrar evento sem tipo
    Dado que existe um pet cadastrado
    Quando tentar registrar um evento sem tipo
    Então a resposta deve retornar o status 400

  Cenário: Não registrar evento para pet inexistente
    Quando tentar registrar um evento para um pet inexistente
    Então a resposta deve retornar o status 404

  Cenário: Não registrar evento para pet excluído
    Dado que existe um pet cadastrado
    E o pet foi excluído
    Quando registrar um evento do tipo "CONSULTA" com a descrição "Retorno"
    Então a resposta deve retornar o status 404

  Cenário: Não registrar evento com funcionário inexistente
    Dado que existe um pet cadastrado
    Quando tentar registrar um evento com um funcionário inexistente
    Então a resposta deve retornar o status 404

  Cenário: Não registrar evento com funcionário desligado
    Dado que existe um pet cadastrado
    E o funcionário foi desligado
    Quando registrar um evento do tipo "CONSULTA" com a descrição "Retorno"
    Então a resposta deve retornar o status 404

  Cenário: Não permitir alterar um evento registrado
    Dado que existe um pet cadastrado com um evento no histórico
    Quando tentar alterar o evento registrado
    Então a resposta deve retornar o status 405

  Cenário: Não permitir excluir um evento registrado
    Dado que existe um pet cadastrado com um evento no histórico
    Quando tentar excluir o evento registrado
    Então a resposta deve retornar o status 405
