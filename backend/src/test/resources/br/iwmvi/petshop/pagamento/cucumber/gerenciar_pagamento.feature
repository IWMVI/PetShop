# language: pt

Funcionalidade: Gerenciamento de pagamentos
  Como gestor do PetShop quero gerenciar pagamentos de agendamentos.

  Cenário: Registrar pagamento para um agendamento
    Dado que existe um agendamento ativo disponível para pagamento
    Quando registrar um pagamento para o agendamento com os dados:
      | valor           | 150.00 |
      | metodoPagamento | PIX    |
    Então o retorno do pagamento deve ser o status 201
    E o pagamento criado deve ter identificador

  Cenário: Não registrar pagamento para agendamento inexistente
    Quando tentar registrar pagamento para um agendamento inexistente
    Então o retorno do pagamento deve ser o status 404

  Cenário: Não registrar pagamento com valor inválido
    Dado que existe um agendamento ativo disponível para pagamento
    Quando tentar registrar pagamento com valor inválido
    Então o retorno do pagamento deve ser o status 400

  Cenário: Listar pagamentos de um agendamento
    Dado que existe um pagamento registrado para o agendamento
    Quando listar os pagamentos do agendamento
    Então o retorno do pagamento deve ser o status 200
    E a lista de pagamentos deve conter ao menos um item

  Cenário: Listar pagamentos vazios
    Dado que existe um agendamento ativo disponível para pagamento
    Quando listar os pagamentos do agendamento
    Então o retorno do pagamento deve ser o status 200
    E a lista de pagamentos deve estar vazia

  Cenário: Buscar pagamento por id
    Dado que existe um pagamento registrado para o agendamento
    Quando buscar o pagamento registrado
    Então o retorno do pagamento deve ser o status 200
    E o pagamento retornado deve possuir status "PENDENTE"

  Cenário: Não buscar pagamento inexistente
    Quando buscar um pagamento inexistente
    Então o retorno do pagamento deve ser o status 404

  Cenário: Atualizar status do pagamento para PAGO
    Dado que existe um pagamento registrado para o agendamento
    Quando atualizar o status do pagamento para PAGO com data de pagamento
    Então o retorno do pagamento deve ser o status 200
    E o pagamento retornado deve possuir status "PAGO"

  Cenário: Não atualizar status para PAGO sem data de pagamento
    Dado que existe um pagamento registrado para o agendamento
    Quando tentar atualizar o status do pagamento para PAGO sem data de pagamento
    Então o retorno do pagamento deve ser o status 400

  Cenário: Cancelar pagamento
    Dado que existe um pagamento registrado para o agendamento
    Quando cancelar o pagamento registrado
    Então o retorno do pagamento deve ser o status 204

  Cenário: Não cancelar pagamento inexistente
    Quando tentar cancelar um pagamento inexistente
    Então o retorno do pagamento deve ser o status 404
