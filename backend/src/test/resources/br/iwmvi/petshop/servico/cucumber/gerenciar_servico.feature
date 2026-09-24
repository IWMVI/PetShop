# language: pt

Funcionalidade: Gerenciamento de serviços
  Como gestor do PetShop quero gerenciar os serviços, podendo listar, buscar, atualizar e excluir.

  Cenário: Listar serviços cadastrados
    Dado que existe um serviço cadastrado
    Quando listar os serviços
    Então a resposta deve retornar o status 200
    E a resposta deve conter uma lista de serviços

  Cenário: Listar serviços quando nenhum está cadastrado
    Quando listar os serviços
    Então a resposta deve retornar o status 200
    E a resposta deve conter uma lista vazia de serviços

  Cenário: Buscar um serviço por id
    Dado que existe um serviço cadastrado
    Quando buscar o serviço cadastrado
    Então a resposta deve retornar o status 200
    E o serviço retornado deve possuir um identificador

  Cenário: Não buscar serviço quando o id não existe
    Quando tentar buscar um serviço inexistente
    Então a resposta deve retornar o status 404

  Cenário: Atualizar um serviço
    Dado que existe um serviço cadastrado
    Quando atualizar o serviço cadastrado com os seguintes dados:
      | nome                     | Tosa Premium                 |
      | descricao                | Tosa com produtos premium    |
      | preco                    | 200.00                       |
      | tempoEstimadoMinutos     | 90                           |
    Então a resposta deve retornar o status 200
    E o serviço retornado deve possuir os dados atualizados

  Cenário: Não atualizar serviço quando o id não existe
    Quando tentar atualizar um serviço inexistente
    Então a resposta deve retornar o status 404

  Cenário: Excluir um serviço
    Dado que existe um serviço cadastrado
    Quando excluir o serviço cadastrado
    Então a resposta deve retornar o status 204

  Cenário: Não excluir serviço quando o id não existe
    Quando tentar excluir um serviço inexistente
    Então a resposta deve retornar o status 404

  Cenário: Não listar serviço excluído
    Dado que existe um serviço cadastrado
    Quando excluir o serviço cadastrado
    E listar os serviços
    Então a resposta deve conter uma lista vazia de serviços
