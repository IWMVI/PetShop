package br.iwmvi.petshop.historico.cucumber;

import br.iwmvi.petshop.tutor.CpfTestData;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class HistoricoPetSteps {

    private static final String URL_HISTORICO = "/pets/{petId}/historico";
    private static final String URL_EVENTO = "/pets/{petId}/historico/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult resultado;

    private Long tutorId;

    private Long petId;

    private Long funcionarioId;

    private Long eventoId;

    @Dado("que existe um pet cadastrado")
    public void queExisteUmPetCadastrado() throws Exception {
        tutorId = obterId(enviarPost("/tutores", criarTutorRequest()));

        Map<String, Object> petRequest = new LinkedHashMap<>();
        petRequest.put("nome", "Fluffy");
        petRequest.put("especie", "Gato");

        petId = obterId(mockMvc.perform(post("/tutores/{tutorId}/pets", tutorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petRequest))
        ).andReturn());

        funcionarioId = obterId(enviarPost("/funcionarios", criarFuncionarioRequest()));
    }

    @Dado("que existe um pet cadastrado com um evento no histórico")
    public void queExisteUmPetCadastradoComUmEvento() throws Exception {
        queExisteUmPetCadastrado();
        registrarEvento(petId, criarEventoRequest("VACINACAO", "Vacina V10", ontem()));
        eventoId = obterId(resultado);
    }

    @Dado("o pet foi excluído")
    public void oPetFoiExcluido() throws Exception {
        mockMvc.perform(delete("/tutores/{tutorId}/pets/{petId}", tutorId, petId)).andReturn();
    }

    @Dado("o funcionário foi desligado")
    public void oFuncionarioFoiDesligado() throws Exception {
        mockMvc.perform(delete("/funcionarios/{id}", funcionarioId)).andReturn();
    }

    @Quando("registrar um evento do tipo {string} com a descrição {string}")
    public void registrarUmEvento(String tipo, String descricao) throws Exception {
        registrarEvento(petId, criarEventoRequest(tipo, descricao, ontem()));
    }

    @Quando("registrar um evento sem funcionário")
    public void registrarUmEventoSemFuncionario() throws Exception {
        var request = criarEventoRequest("OUTRO", "Vacina aplicada em outra clínica", ontem());
        request.put("funcionarioId", null);
        registrarEvento(petId, request);
    }

    @Quando("tentar registrar um evento com data no futuro")
    public void tentarRegistrarUmEventoComDataNoFuturo() throws Exception {
        var amanha = LocalDateTime.now().plusDays(1).toString();
        registrarEvento(petId, criarEventoRequest("CONSULTA", "Consulta agendada", amanha));
    }

    @Quando("tentar registrar um evento sem tipo")
    public void tentarRegistrarUmEventoSemTipo() throws Exception {
        registrarEvento(petId, criarEventoRequest(null, "Consulta de rotina", ontem()));
    }

    @Quando("tentar registrar um evento para um pet inexistente")
    public void tentarRegistrarUmEventoParaUmPetInexistente() throws Exception {
        registrarEvento(999_999L, criarEventoRequest("CONSULTA", "Consulta de rotina", ontem()));
    }

    @Quando("tentar registrar um evento com um funcionário inexistente")
    public void tentarRegistrarUmEventoComFuncionarioInexistente() throws Exception {
        var request = criarEventoRequest("CONSULTA", "Consulta de rotina", ontem());
        request.put("funcionarioId", 999_999L);
        registrarEvento(petId, request);
    }

    @Quando("listar o histórico do pet")
    public void listarOHistoricoDoPet() throws Exception {
        resultado = mockMvc.perform(get(URL_HISTORICO, petId)).andReturn();
    }

    @Quando("consultar o evento registrado")
    public void consultarOEventoRegistrado() throws Exception {
        resultado = mockMvc.perform(get(URL_EVENTO, petId, eventoId)).andReturn();
    }

    @Quando("tentar consultar um evento inexistente")
    public void tentarConsultarUmEventoInexistente() throws Exception {
        resultado = mockMvc.perform(get(URL_EVENTO, petId, 999_999L)).andReturn();
    }

    @Quando("tentar alterar o evento registrado")
    public void tentarAlterarOEventoRegistrado() throws Exception {
        resultado = mockMvc.perform(put(URL_EVENTO, petId, eventoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        criarEventoRequest("OUTRO", "Descrição alterada", ontem())))
        ).andReturn();
    }

    @Quando("tentar excluir o evento registrado")
    public void tentarExcluirOEventoRegistrado() throws Exception {
        resultado = mockMvc.perform(delete(URL_EVENTO, petId, eventoId)).andReturn();
    }

    @Entao("a resposta deve retornar o status {int}")
    public void respostaDeveRetornarStatus(int statusEsperado) {
        assertThat(resultado.getResponse().getStatus())
                .as("Status HTTP retornado")
                .isEqualTo(statusEsperado);
    }

    @Entao("o evento retornado deve possuir um identificador")
    public void eventoRetornadoDevePossuirIdentificador() throws Exception {
        assertThat(lerResposta().hasNonNull("id"))
                .as("Resposta deve possuir o campo id")
                .isTrue();
    }

    @Entao("o evento retornado deve informar o funcionário {string}")
    public void eventoRetornadoDeveInformarOFuncionario(String nome) throws Exception {
        assertThat(lerResposta().get("funcionarioNome").asString())
                .as("Nome do funcionário que executou o evento")
                .isEqualTo(nome);
    }

    @Entao("o evento retornado não deve possuir funcionário")
    public void eventoRetornadoNaoDevePossuirFuncionario() throws Exception {
        assertThat(lerResposta().hasNonNull("funcionarioId"))
                .as("Evento não deve estar vinculado a funcionário")
                .isFalse();
    }

    @Entao("o histórico deve conter {int} evento(s)")
    public void historicoDeveConterEventos(int quantidade) throws Exception {
        JsonNode response = lerResposta();

        assertThat(response.isArray()).as("Resposta deve ser uma lista").isTrue();
        assertThat(response.size()).as("Quantidade de eventos").isEqualTo(quantidade);
    }

    private void registrarEvento(Long petId, Map<String, Object> request) throws Exception {
        resultado = mockMvc.perform(post(URL_HISTORICO, petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn();
    }

    private MvcResult enviarPost(String url, Map<String, Object> request) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn();
    }

    private Map<String, Object> criarEventoRequest(String tipo, String descricao, String dataEvento) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("tipoEvento", tipo);
        request.put("descricao", descricao);
        request.put("dataEvento", dataEvento);
        request.put("funcionarioId", funcionarioId);
        return request;
    }

    private Map<String, Object> criarFuncionarioRequest() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("nome", "Dra. Ana");
        request.put("cpf", CpfTestData.gerar());
        request.put("cargo", "VETERINARIO");
        request.put("telefone", "11988887777");
        return request;
    }

    private Map<String, Object> criarTutorRequest() {
        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("cep", "01001-001");
        endereco.put("logradouro", "Praça da Sé");
        endereco.put("numero", "1");
        endereco.put("complemento", null);
        endereco.put("bairro", "Sé");
        endereco.put("cidade", "São Paulo");
        endereco.put("estado", "SP");

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("nome", "Wallace");
        request.put("cpf", CpfTestData.gerar());
        // e-mail é único no banco e o H2 é compartilhado entre os cenários
        request.put("email", "tutor-" + UUID.randomUUID() + "@test.com");
        request.put("telefone", "11999999999");
        request.put("endereco", endereco);
        return request;
    }

    private String ontem() {
        return LocalDateTime.now().minusDays(1).toString();
    }

    private JsonNode lerResposta() throws Exception {
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }

    private Long obterId(MvcResult resultado) throws Exception {
        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("id").asLong();
    }
}
