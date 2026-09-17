package br.iwmvi.petshop.pet.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class PetSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult resultado;

    private Long tutorId;

    private Long petId;

    @Dado("que existe um tutor cadastrado")
    public void queExisteUmTutorCadastrado() throws Exception {
        Map<String, Object> tutorRequest = criarTutorRequest(
                "Wallace",
                "wallace@test.com",
                "11999999999",
                "01001-001",
                "Praça da Sé",
                "1",
                "Sé",
                "São Paulo",
                "SP"
        );

        resultado = mockMvc.perform(post("/tutores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tutorRequest))
        ).andReturn();

        tutorId = obterId(resultado);
    }

    @Dado("que existe um tutor cadastrado com um pet")
    public void queExisteUmTutorCadastradoComUmPet() throws Exception {
        queExisteUmTutorCadastrado();

        Map<String, Object> petRequest = criarPetRequest(
                "Fluffy",
                "Gato",
                "Persa",
                "2",
                "5.50"
        );

        resultado = mockMvc.perform(post("/tutores/{tutorId}/pets", tutorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petRequest))
        ).andReturn();

        petId = obterId(resultado);
    }

    @Quando("cadastrar um pet com os seguintes dados:")
    public void cadastrarUmPetComOsSeguintesDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        Map<String, Object> petRequest = criarPetRequest(
                dados.get("nome"),
                dados.get("especie"),
                dados.get("raca"),
                dados.get("idade"),
                dados.get("peso")
        );

        resultado = mockMvc.perform(post("/tutores/{tutorId}/pets", tutorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petRequest))
        ).andReturn();
    }

    @Quando("tentar cadastrar um pet com nome vazio")
    public void tentarCadastrarUmPetComNomeVazio() throws Exception {
        Map<String, Object> petRequest = criarPetRequest(
                "",
                "Gato",
                "Persa",
                "2",
                "5.50"
        );

        resultado = mockMvc.perform(post("/tutores/{tutorId}/pets", tutorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petRequest))
        ).andReturn();
    }

    @Quando("tentar cadastrar um pet para um tutor inexistente")
    public void tentarCadastrarUmPetParaUmTutorInexistente() throws Exception {
        Map<String, Object> petRequest = criarPetRequest(
                "Fluffy",
                "Gato",
                "Persa",
                "2",
                "5.50"
        );

        resultado = mockMvc.perform(post("/tutores/{tutorId}/pets", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petRequest))
        ).andReturn();
    }

    @Quando("tentar cadastrar um pet com peso {string}")
    public void tentarCadastrarUmPetComPeso(String peso) throws Exception {
        Map<String, Object> petRequest = criarPetRequest(
                "Fluffy",
                "Gato",
                "Persa",
                "2",
                peso
        );

        resultado = mockMvc.perform(post("/tutores/{tutorId}/pets", tutorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petRequest))
        ).andReturn();
    }

    @Quando("tentar cadastrar um pet com especie vazia")
    public void tentarCadastrarUmPetComEspecieVazia() throws Exception {
        Map<String, Object> petRequest = criarPetRequest(
                "Fluffy",
                "",
                "Persa",
                "2",
                "5.50"
        );

        resultado = mockMvc.perform(post("/tutores/{tutorId}/pets", tutorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petRequest))
        ).andReturn();
    }

    @Quando("listar os pets do tutor")
    public void listarOsPetsDoTutor() throws Exception {
        resultado = mockMvc.perform(get("/tutores/{tutorId}/pets", tutorId)).andReturn();
    }

    @Quando("buscar o pet cadastrado")
    public void buscarOPetCadastrado() throws Exception {
        resultado = mockMvc.perform(get("/tutores/{tutorId}/pets/{petId}", tutorId, petId)).andReturn();
    }

    @Quando("tentar buscar um pet inexistente")
    public void tentarBuscarUmPetInexistente() throws Exception {
        resultado = mockMvc.perform(get("/tutores/{tutorId}/pets/{petId}", tutorId, 999L)).andReturn();
    }

    @Quando("atualizar o pet cadastrado com os seguintes dados:")
    public void atualizarOPetCadastradoComOsSeguintesDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        Map<String, Object> petRequest = criarPetRequest(
                dados.get("nome"),
                dados.get("especie"),
                dados.get("raca"),
                dados.get("idade"),
                dados.get("peso")
        );

        resultado = mockMvc.perform(put("/tutores/{tutorId}/pets/{petId}", tutorId, petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petRequest))
        ).andReturn();
    }

    @Quando("tentar atualizar um pet inexistente")
    public void tentarAtualizarUmPetInexistente() throws Exception {
        Map<String, Object> petRequest = criarPetRequest(
                "Fluffy",
                "Gato",
                "Persa",
                "2",
                "5.50"
        );

        resultado = mockMvc.perform(put("/tutores/{tutorId}/pets/{petId}", tutorId, 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petRequest))
        ).andReturn();
    }

    @Quando("excluir o pet cadastrado")
    public void excluirOPetCadastrado() throws Exception {
        resultado = mockMvc.perform(delete("/tutores/{tutorId}/pets/{petId}", tutorId, petId)).andReturn();
    }

    @Quando("tentar excluir um pet inexistente")
    public void tentarExcluirUmPetInexistente() throws Exception {
        resultado = mockMvc.perform(delete("/tutores/{tutorId}/pets/{petId}", tutorId, 999L)).andReturn();
    }

    @Entao("o cadastro do pet deve retornar o status {int}")
    public void cadastroDoPetDeveRetornarStatus(int statusEsperado) {
        assertThat(resultado)
                .as("Resultado da requisição deve existir")
                .isNotNull();

        assertThat(resultado.getResponse().getStatus())
                .as("Status HTTP retornado pelo cadastro")
                .isEqualTo(statusEsperado);
    }

    @Entao("a resposta deve retornar o status {int}")
    public void respostaDeveRetornarStatus(int statusEsperado) {
        assertThat(resultado)
                .as("Resultado da requisição deve existir")
                .isNotNull();

        assertThat(resultado.getResponse().getStatus())
                .as("Status HTTP retornado")
                .isEqualTo(statusEsperado);
    }

    @Entao("o pet cadastrado deve possuir um identificador")
    public void petCadastradoDevePossuirIdentificador() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.hasNonNull("id"))
                .as("Resposta deve possuir o campo id")
                .isTrue();

        assertThat(response.get("id").asLong())
                .as("Identificador do pet deve ser positivo")
                .isPositive();
    }

    @Entao("a resposta deve conter uma lista de pets")
    public void respostaDeveConterUmaListaDePets() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.isArray())
                .as("Resposta deve ser uma lista")
                .isTrue();

        assertThat(response.size())
                .as("Lista deve conter ao menos um pet")
                .isPositive();
    }

    @Entao("a resposta deve conter uma lista vazia de pets")
    public void respostaDeveConterUmaListaVaziaDePets() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.isArray())
                .as("Resposta deve ser uma lista")
                .isTrue();

        assertThat(response.size())
                .as("Lista deve estar vazia")
                .isZero();
    }

    @Entao("o pet retornado deve possuir um identificador")
    public void petRetornadoDevePossuirIdentificador() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.hasNonNull("id"))
                .as("Resposta deve possuir o campo id")
                .isTrue();
    }

    @Entao("o pet retornado deve possuir os dados atualizados")
    public void petRetornadoDevePossuirOsDadosAtualizados() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.hasNonNull("nome"))
                .as("Resposta deve possuir o campo nome")
                .isTrue();

        assertThat(response.get("nome").asString())
                .as("Nome do pet deve ser atualizado")
                .isEqualTo("Fluffy Atualizado");

        assertThat(response.get("raca").asString())
                .as("Raça do pet deve ser atualizada")
                .isEqualTo("Siamês");

        assertThat(response.get("idade").asInt())
                .as("Idade do pet deve ser atualizada")
                .isEqualTo(3);
    }

    private Map<String, Object> criarTutorRequest(String nome, String email, String telefone,
                                                  String cep, String logradouro, String numero,
                                                  String bairro, String cidade, String estado) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("nome", nome);
        request.put("email", email);
        request.put("telefone", telefone);

        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("cep", cep);
        endereco.put("logradouro", logradouro);
        endereco.put("numero", numero);
        endereco.put("complemento", null);
        endereco.put("bairro", bairro);
        endereco.put("cidade", cidade);
        endereco.put("estado", estado);

        request.put("endereco", endereco);

        return request;
    }

    private Map<String, Object> criarPetRequest(String nome, String especie, String raca,
                                                String idade, String peso) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("nome", nome);
        request.put("especie", especie);
        request.put("raca", raca);
        request.put("idade", idade != null && !idade.isEmpty() ? Integer.parseInt(idade) : null);
        request.put("peso", peso != null && !peso.isEmpty() ? new BigDecimal(peso) : null);

        return request;
    }

    private Long obterId(MvcResult resultado) throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        return response.get("id").asLong();
    }
}
