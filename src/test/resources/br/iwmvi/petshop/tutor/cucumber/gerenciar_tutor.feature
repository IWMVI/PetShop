# language: pt

Funcionalidade: Gerenciamento de tutores
  Como usuário do PetShop
  Quero listar, buscar, atualizar e excluir tutores
  Para administrar os responsáveis pelos pets

  Cenário: Listar tutores cadastrados
    Dado que existe um tutor cadastrado com o e-mail "listar@test.com"
    Quando listar os tutores
    Então a resposta deve retornar o status 200
    E a resposta deve conter uma lista de tutores

  Cenário: Buscar tutor por identificador existente
    Dado que existe um tutor cadastrado com o e-mail "buscar@test.com"
    Quando buscar o tutor cadastrado
    Então a resposta deve retornar o status 200
    E o tutor retornado deve possuir um identificador
    E o tutor retornado deve possuir o e-mail "buscar@test.com"

  Cenário: Buscar tutor por identificador inexistente
    Quando tentar buscar o tutor de id 9999
    Então a resposta deve retornar o status 404

  Cenário: Atualizar tutor existente
    Dado que existe um tutor cadastrado com o e-mail "atualizar@test.com"
    Quando tentar atualizar o tutor cadastrado com os seguintes dados:
      | nome       | Wallace Atualizado |
      | email      | atualizado@test.com |
      | telefone   | 11911112222       |
      | cep        | 01001-010           |
      | logradouro | Avenida Paulista    |
      | numero     | 1000                |
      | bairro     | Bela Vista          |
      | cidade     | São Paulo           |
      | estado     | sp                  |
    Então a resposta deve retornar o status 200
    E o tutor retornado deve possuir os dados atualizados

  Cenário: Atualizar tutor por identificador inexistente
    Quando tentar atualizar o tutor de id 9999 com os seguintes dados:
      | nome       | Tutor Inexistente |
      | email      | inexistente@test.com |
      | telefone   | 11111111111       |
      | cep        | 01001-001         |
      | logradouro | Praça da Sé       |
      | numero     | 1                 |
      | bairro     | Sé                |
      | cidade     | São Paulo         |
      | estado     | SP                |
    Então a resposta deve retornar o status 404

  Cenário: Atualizar tutor com e-mail já cadastrado
    Dado que existe um tutor cadastrado com o e-mail "um@email.com"
    Dado que existe um tutor cadastrado com o e-mail "dois@email.com"
    Quando tentar atualizar o tutor cadastrado com o e-mail "um@email.com"
    Então a resposta deve retornar o status 409

  Cenário: Atualizar tutor com payload inválido
    Dado que existe um tutor cadastrado com o e-mail "valido@test.com"
    Quando tentar atualizar o tutor cadastrado com o e-mail "invalido"
    Então a resposta deve retornar o status 400

  Cenário: Excluir tutor existente
    Dado que existe um tutor cadastrado com o e-mail "excluir@test.com"
    Quando tentar excluir o tutor cadastrado
    Então a resposta deve retornar o status 204
    E o tutor excluído não deve mais ser encontrado

  Cenário: Excluir tutor por identificador inexistente
    Quando tentar excluir o tutor de id 9999
    Então a resposta deve retornar o status 404

  Cenário: Listar tutores não deve incluir excluídos
    Dado que existe um tutor cadastrado com o e-mail "oculto@test.com"
    Quando tentar excluir o tutor cadastrado
    E listar os tutores
    Então a resposta deve retornar o status 200
    E a lista não deve conter o tutor excluído

  Cenário: Restaurar tutor excluído
    Dado que existe um tutor cadastrado com o e-mail "restaurar@test.com"
    Quando tentar excluir o tutor cadastrado
    E tentar restaurar o tutor cadastrado
    Então a resposta deve retornar o status 200
    E o tutor restaurado deve possuir o e-mail "restaurar@test.com"
    E o tutor cadastrado deve ser encontrado novamente

  Cenário: Restaurar tutor por identificador inexistente
    Quando tentar restaurar o tutor de id 9999
    Então a resposta deve retornar o status 404