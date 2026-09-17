# language: pt

Funcionalidade: Gerenciamento de agendamentos
  Como gestor do PetShop quero gerenciar agendamentos de serviços por pet.

  Cenário: Agendar serviços para um pet
    Dado que existe um pet disponível para agendamento
    E que existem serviços disponíveis para agendamento
    Quando agendar serviços para o pet com os dados:
      | dataHora    | 2099-12-31T10:00:00 |
      | observacoes | Levar com coleira   |
      | servicoIds  | 1,2                 |
    Então o retorno do agendamento deve ser o status 201
    E o agendamento criado deve ter identificador

  Cenário: Não agendar para pet inexistente
    Dado que existe um pet disponível para agendamento
    E que existem serviços disponíveis para agendamento
    Quando tentar agendar para um pet inexistente
    Então o retorno do agendamento deve ser o status 404

  Cenário: Não agendar com serviço inválido
    Dado que existe um pet disponível para agendamento
    E que existem serviços disponíveis para agendamento
    Quando tentar agendar com serviço inválido
    Então o retorno do agendamento deve ser o status 400

  Cenário: Listar agendamentos do pet
    Dado que existe um agendamento ativo para o pet
    Quando listar os agendamentos do pet
    Então o retorno do agendamento deve ser o status 200
    E a lista de agendamentos deve conter ao menos um item

  Cenário: Listar agendamentos vazios
    Dado que existe um pet disponível para agendamento
    Quando listar os agendamentos do pet
    Então o retorno do agendamento deve ser o status 200
    E a lista de agendamentos deve estar vazia

  Cenário: Buscar agendamento por id
    Dado que existe um agendamento ativo para o pet
    Quando buscar o agendamento ativo do pet
    Então o retorno do agendamento deve ser o status 200
    E o agendamento retornado deve possuir status "AGENDADO"

  Cenário: Não buscar agendamento inexistente
    Dado que existe um pet disponível para agendamento
    Quando buscar um agendamento inexistente do pet
    Então o retorno do agendamento deve ser o status 404

  Cenário: Reagendar mudando data e serviços
    Dado que existe um agendamento ativo para o pet
    E que existem serviços disponíveis para agendamento
    Quando reagendar o agendamento com os dados:
      | dataHora    | 2099-12-31T15:00:00      |
      | observacoes | Reagendado para a tarde  |
    Então o retorno do agendamento deve ser o status 200
    E o agendamento retornado deve possuir os serviços atualizados

  Cenário: Não reagendar com data no passado
    Dado que existe um agendamento ativo para o pet
    Quando tentar reagendar com data no passado
    Então o retorno do agendamento deve ser o status 400

  Cenário: Cancelar agendamento
    Dado que existe um agendamento ativo para o pet
    Quando cancelar o agendamento do pet
    Então o retorno do agendamento deve ser o status 204
