# language: pt

Funcionalidade: Gerenciamento de pets
  Como usuário do PetShop quero gerenciar os pets dos tutores, podendo listar, buscar, atualizar e excluir.

  Cenário: Listar pets de um tutor
    Dado que existe um tutor cadastrado com um pet
    Quando listar os pets do tutor
    Então a resposta deve retornar o status 200
    E a resposta deve conter uma lista de pets

  Cenário: Listar pets quando nenhum pet está cadastrado
    Dado que existe um tutor cadastrado
    Quando listar os pets do tutor
    Então a resposta deve retornar o status 200
    E a resposta deve conter uma lista vazia de pets

  Cenário: Buscar um pet por id
    Dado que existe um tutor cadastrado com um pet
    Quando buscar o pet cadastrado
    Então a resposta deve retornar o status 200
    E o pet retornado deve possuir um identificador

  Cenário: Não buscar pet quando o id não existe
    Dado que existe um tutor cadastrado
    Quando tentar buscar um pet inexistente
    Então a resposta deve retornar o status 404

  Cenário: Atualizar um pet
    Dado que existe um tutor cadastrado com um pet
    Quando atualizar o pet cadastrado com os seguintes dados:
      | nome    | Fluffy Atualizado |
      | especie | Gato              |
      | raca    | Siamês            |
      | idade   | 3                 |
      | peso    | 6.50              |
    Então a resposta deve retornar o status 200
    E o pet retornado deve possuir os dados atualizados

  Cenário: Não atualizar pet quando o id não existe
    Dado que existe um tutor cadastrado
    Quando tentar atualizar um pet inexistente
    Então a resposta deve retornar o status 404

  Cenário: Excluir um pet
    Dado que existe um tutor cadastrado com um pet
    Quando excluir o pet cadastrado
    Então a resposta deve retornar o status 204

  Cenário: Não excluir pet quando o id não existe
    Dado que existe um tutor cadastrado
    Quando tentar excluir um pet inexistente
    Então a resposta deve retornar o status 404

  Cenário: Não listar pet excluído
    Dado que existe um tutor cadastrado com um pet
    Quando excluir o pet cadastrado
    E listar os pets do tutor
    Então a resposta deve conter uma lista vazia de pets
