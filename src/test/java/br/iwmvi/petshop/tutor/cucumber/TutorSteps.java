package br.iwmvi.petshop.tutor.cucumber;

import br.iwmvi.petshop.endereco.EnderecoTestData;
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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class TutorSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult resultado;

    private Long tutorId;

    @Dado("que existe um tutor cadastrado com o e-mail {string}")
    public void queExisteUmTutorCadastradoComEmail(String email) throws Exception {
        Map<String, Object> request = criarTutorRequestValido();

        request.put("email", email);

        resultado = realizarCadastro(request);
        tutorId = obterId(resultado);
    }

    @Quando("cadastrar um tutor com os seguintes dados:")
    public void cadastrarTutorComOsSeguintesDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        Map<String, Object> request = criarTutorRequest(
                dados.get("nome"),
                dados.get("email"),
                dados.get("telefone"),
                dados.get("cep"),
                dados.get("logradouro"),
                dados.get("numero"),
                dados.get("bairro"),
                dados.get("cidade"),
                dados.get("estado")
        );

        resultado = realizarCadastro(request);
    }

    @Quando("tentar cadastrar um tutor com o e-mail {string}")
    public void tentarCadastrarTutorComEmail(String email) throws Exception {
        Map<String, Object> request = criarTutorRequestValido();

        request.put("email", email);

        resultado = realizarCadastro(request);
    }

    @Quando("tentar cadastrar um tutor com o CEP {string}")
    @SuppressWarnings("unchecked")
    public void tentarCadastrarTutorComCep(String cep) throws Exception {
        Map<String, Object> request = criarTutorRequestValido();

        Map<String, Object> endereco =
                (Map<String, Object>) request.get("endereco");

        endereco.put("cep", cep);

        resultado = realizarCadastro(request);
    }

    @Quando("listar os tutores")
    public void listarTutores() throws Exception {
        resultado = mockMvc.perform(get("/tutores")).andReturn();
    }

    @Quando("buscar o tutor cadastrado")
    public void buscarTutorCadastrado() throws Exception {
        resultado = buscarTutor(tutorId);
    }

    @Quando("tentar buscar o tutor de id {long}")
    public void tentarBuscarTutorDeId(long id) throws Exception {
        resultado = buscarTutor(id);
    }

    @Quando("tentar atualizar o tutor cadastrado com os seguintes dados:")
    public void tentarAtualizarTutorCadastradoComOsSeguintesDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        Map<String, Object> request = criarTutorRequest(
                dados.get("nome"),
                dados.get("email"),
                dados.get("telefone"),
                dados.get("cep"),
                dados.get("logradouro"),
                dados.get("numero"),
                dados.get("bairro"),
                dados.get("cidade"),
                dados.get("estado")
        );

        resultado = realizarAtualizacao(tutorId, request);
    }

    @Quando("tentar atualizar o tutor cadastrado com o e-mail {string}")
    public void tentarAtualizarTutorCadastradoComEmail(String email) throws Exception {
        Map<String, Object> request = criarTutorRequestValido();

        request.put("email", email);

        resultado = realizarAtualizacao(tutorId, request);
    }

    @Quando("tentar atualizar o tutor de id {long} com os seguintes dados:")
    public void tentarAtualizarTutorDeIdComOsSeguintesDados(long id, DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        Map<String, Object> request = criarTutorRequest(
                dados.get("nome"),
                dados.get("email"),
                dados.get("telefone"),
                dados.get("cep"),
                dados.get("logradouro"),
                dados.get("numero"),
                dados.get("bairro"),
                dados.get("cidade"),
                dados.get("estado")
        );

        resultado = realizarAtualizacao(id, request);
    }

    @Quando("tentar excluir o tutor cadastrado")
    public void tentarExcluirTutorCadastrado() throws Exception {
        resultado = excluirTutor(tutorId);
    }

    @Quando("tentar excluir o tutor de id {long}")
    public void tentarExcluirTutorDeId(long id) throws Exception {
        resultado = excluirTutor(id);
    }

    @Entao("o cadastro do tutor deve retornar o status {int}")
    public void cadastroDoTutorDeveRetornarStatus(int statusEsperado) {
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

    @Entao("o tutor cadastrado deve possuir um identificador")
    public void tutorCadastradoDevePossuirIdentificador() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.hasNonNull("id"))
                .as("Resposta deve possuir o campo id")
                .isTrue();

        assertThat(response.get("id").asLong())
                .as("Identificador do tutor deve ser positivo")
                .isPositive();
    }

    @Entao("a resposta deve conter uma lista de tutores")
    public void respostaDeveConterListaDeTutores() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.isArray())
                .as("Resposta deve ser uma lista")
                .isTrue();

        assertThat(response.size())
                .as("Lista deve conter ao menos um tutor")
                .isPositive();
    }

    @Entao("o tutor retornado deve possuir um identificador")
    public void tutorRetornadoDevePossuirIdentificador() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.hasNonNull("id"))
                .as("Resposta deve possuir o campo id")
                .isTrue();
    }

    @Entao("o tutor retornado deve possuir o e-mail {string}")
    public void tutorRetornadoDevePossuirEmail(String email) throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.hasNonNull("email"))
                .as("Resposta deve possuir o campo email")
                .isTrue();

        assertThat(response.get("email").asString())
                .as("E-mail do tutor retornado")
                .isEqualTo(email);
    }

    @Entao("o tutor retornado deve possuir os dados atualizados")
    public void tutorRetornadoDevePossuirDadosAtualizados() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.get("nome").asString()).isEqualTo("Wallace Atualizado");
        assertThat(response.get("email").asString()).isEqualTo("atualizado@test.com");
        assertThat(response.get("telefone").asString()).isEqualTo("11911112222");
        assertThat(response.get("endereco").get("cep").asString()).isEqualTo("01001010");
        assertThat(response.get("endereco").get("logradouro").asString()).isEqualTo("Avenida Paulista");
        assertThat(response.get("endereco").get("estado").asString()).isEqualTo("SP");
    }

    @Entao("o tutor excluído não deve mais ser encontrado")
    public void tutorExcluidoNaoDeveMaisSerEncontrado() throws Exception {
        resultado = buscarTutor(tutorId);

        assertThat(resultado.getResponse().getStatus())
                .as("Tutor excluído deve retornar 404")
                .isEqualTo(404);
    }

    private MvcResult realizarCadastro(Map<String, Object> request) throws Exception {
        return mockMvc.perform(
                        post("/tutores")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andReturn();
    }

    private MvcResult realizarAtualizacao(Long id, Map<String, Object> request) throws Exception {
        return mockMvc.perform(
                        put("/tutores/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andReturn();
    }

    private MvcResult buscarTutor(long id) throws Exception {
        return mockMvc.perform(get("/tutores/{id}", id)).andReturn();
    }

    private MvcResult excluirTutor(long id) throws Exception {
        return mockMvc.perform(delete("/tutores/{id}", id)).andReturn();
    }

    private long obterId(MvcResult resultadoCadastro) throws Exception {
        JsonNode response = objectMapper.readTree(
                resultadoCadastro.getResponse().getContentAsString()
        );

        return response.get("id").asLong();
    }

    private Map<String, Object> criarTutorRequestValido() {
        return criarTutorRequest(
                "João da Silva",
                "joao@email.com",
                "11999999999",
                "01001-001",
                "Praça da Sé",
                "100",
                "Sé",
                "São Paulo",
                "SP"
        );
    }

    private Map<String, Object> criarTutorRequest(
            String nome,
            String email,
            String telefone,
            String cep,
            String logradouro,
            String numero,
            String bairro,
            String cidade,
            String estado
    ) {
        Map<String, Object> tutor = new LinkedHashMap<>();

        tutor.put("nome", nome);
        tutor.put("email", email);
        tutor.put("telefone", telefone);
        tutor.put("endereco", EnderecoTestData.criarEnderecoJson(
                cep, logradouro, numero, null, bairro, cidade, estado
        ));

        return tutor;
    }
}