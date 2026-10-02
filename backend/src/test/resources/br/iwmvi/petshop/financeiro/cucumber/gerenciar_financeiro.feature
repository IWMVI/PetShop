# language: pt

Funcionalidade: Gerenciamento financeiro
  Como gestor do PetShop quero registrar entradas e saídas e consultar o extrato.

  Cenário: Registrar uma entrada manual
    Quando registrar uma entrada com os dados:
      | categoria | VENDA_PRODUTO |
      | descricao | Venda de ração |
      | valor     | 80.00          |
    Então o retorno do financeiro deve ser o status 201
    E o lançamento criado deve ter identificador

  Cenário: Registrar uma saída manual
    Quando registrar uma saída com os dados:
      | categoria | FORNECEDOR          |
      | descricao | Compra de insumos   |
      | valor     | 200.00              |
    Então o retorno do financeiro deve ser o status 201

  Cenário: Não registrar entrada com categoria de saída
    Quando registrar uma entrada com os dados:
      | categoria | ALUGUEL |
      | descricao | Inválido |
      | valor     | 10.00    |
    Então o retorno do financeiro deve ser o status 400

  Cenário: Buscar lançamento por id
    Dado que existe um lançamento manual registrado
    Quando buscar o lançamento registrado
    Então o retorno do financeiro deve ser o status 200

  Cenário: Não buscar lançamento inexistente
    Quando buscar um lançamento financeiro inexistente
    Então o retorno do financeiro deve ser o status 404

  Cenário: Atualizar lançamento manual
    Dado que existe um lançamento manual registrado
    Quando atualizar o lançamento registrado com os dados:
      | categoria | OUTRA_RECEITA      |
      | descricao | Receita atualizada |
      | valor     | 99.90              |
    Então o retorno do financeiro deve ser o status 200

  Cenário: Cancelar lançamento manual
    Dado que existe um lançamento manual registrado
    Quando cancelar o lançamento registrado
    Então o retorno do financeiro deve ser o status 204

  Cenário: Consultar extrato do período com totais
    Dado que existe um lançamento manual registrado
    Quando consultar o extrato financeiro
    Então o retorno do financeiro deve ser o status 200
    E o extrato deve conter ao menos um lançamento

  Cenário: Pagamento marcado como pago aparece automaticamente no extrato
    Dado que existe um pagamento de agendamento marcado como pago
    Quando consultar o extrato financeiro
    Então o retorno do financeiro deve ser o status 200
    E o extrato deve conter um lançamento vinculado ao pagamento

  Cenário: Não atualizar lançamento gerado automaticamente por pagamento
    Dado que existe um pagamento de agendamento marcado como pago
    Quando tentar atualizar o lançamento gerado pelo pagamento
    Então o retorno do financeiro deve ser o status 400

  Cenário: Não cancelar lançamento gerado automaticamente por pagamento
    Dado que existe um pagamento de agendamento marcado como pago
    Quando tentar cancelar o lançamento gerado pelo pagamento
    Então o retorno do financeiro deve ser o status 400
