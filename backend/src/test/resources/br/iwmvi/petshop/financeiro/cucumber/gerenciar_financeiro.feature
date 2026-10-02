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

  Cenário: Registrar uma conta a pagar
    Quando registrar uma conta a pagar com os dados:
      | categoria      | FORNECEDOR          |
      | descricao      | Conta de fornecedor |
      | valor          | 300.00              |
      | diasVencimento | 10                  |
    Então o retorno do financeiro deve ser o status 201
    E a conta criada deve ter identificador e status PENDENTE

  Cenário: Registrar uma conta a receber
    Quando registrar uma conta a receber com os dados:
      | categoria      | OUTRA_RECEITA         |
      | descricao      | Conta a receber       |
      | valor          | 400.00                |
      | diasVencimento | 10                    |
    Então o retorno do financeiro deve ser o status 201
    E a conta criada deve ter identificador e status PENDENTE

  Cenário: Marcar conta como paga e ela aparece no extrato depois
    Dado que existe uma conta a pagar pendente registrada
    Quando marcar a conta registrada como paga
    Então o retorno do financeiro deve ser o status 200
    E a conta deve estar com status PAGO
    E o extrato deve conter a conta paga

  Cenário: Consultar saldo de contas a pagar
    Dado que existe uma conta a pagar pendente registrada
    Quando consultar o saldo de contas a pagar
    Então o retorno do financeiro deve ser o status 200
    E o saldo de contas deve ter total pendente maior que zero

  Cenário: Consultar saldo de contas a receber
    Dado que existe uma conta a receber pendente registrada
    Quando consultar o saldo de contas a receber
    Então o retorno do financeiro deve ser o status 200
    E o saldo de contas deve ter total pendente maior que zero

  Cenário: Não marcar como paga uma conta que já foi paga
    Dado que existe uma conta a pagar pendente registrada
    Quando marcar a conta registrada como paga
    E tentar marcar a conta registrada como paga novamente
    Então o retorno do financeiro deve ser o status 400

  Cenário: Cancelar uma conta pendente
    Dado que existe uma conta a receber pendente registrada
    Quando cancelar a conta registrada
    Então o retorno do financeiro deve ser o status 204
