package br.iwmvi.petshop.agendamento.cucumber;

import br.iwmvi.petshop.tutor.CpfTestData;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class AgendamentoSteps {

    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult resultado;
    private Long petId;
    private Long agendamentoId;
    private final List<Long> servicoIdsCriados = new ArrayList<>();

    @Dado("que existe um pet disponível para agendamento")
    public void queExisteUmPetDisponivelParaAgendamento() throws Exception {
        Long tutorId = criarTutor();
        petId = criarPet(tutorId);
    }

    @Dado("que existem serviços disponíveis para agendamento")
    public void queExistemServicosDisponiveisParaAgendamento() throws Exception {
        servicoIdsCriados.clear();
        servicoIdsCriados.add(criarServico("Banho", "50.00"));
        servicoIdsCriados.add(criarServico("Tosa", "30.00"));
    }

    @Dado("que existe um agendamento ativo para o pet")
    public void queExisteUmAgendamentoAtivoParaOPet() throws Exception {
        queExisteUmPetDisponivelParaAgendamento();
        queExistemServicosDisponiveisParaAgendamento();

        Map<String, Object> request = criarAgendamentoRequest(
                LocalDateTime.now().plusDays(1),
                "Primeiro agendamento",
                List.of(servicoIdsCriados.getFirst())
        );

        resultado = mockMvc.perform(post("/pets/{petId}/agendamentos", petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();

        agendamentoId = obterId(resultado);
    }

    @Quando("agendar serviços para o pet com os dados:")
    public void agendarServicosParaOPetComOsDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        List<Long> servicoIds = dados.get("servicoIds").equals("1,2")
                ? List.of(servicoIdsCriados.get(0), servicoIdsCriados.get(1))
                : List.of(servicoIdsCriados.getFirst());
        Map<String, Object> request = criarAgendamentoRequest(
                LocalDateTime.parse(dados.get("dataHora"), FORMATADOR),
                dados.get("observacoes"),
                servicoIds
        );

        resultado = mockMvc.perform(post("/pets/{petId}/agendamentos", petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();

        if (resultado.getResponse().getStatus() == 201) {
            agendamentoId = obterId(resultado);
        }
    }

    @Quando("tentar agendar para um pet inexistente")
    public void tentarAgendarParaUmPetInexistente() throws Exception {
        Map<String, Object> request = criarAgendamentoRequest(
                LocalDateTime.now().plusDays(1),
                "Teste",
                List.of(servicoIdsCriados.getFirst())
        );

        resultado = mockMvc.perform(post("/pets/{petId}/agendamentos", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Quando("tentar agendar com serviço inválido")
    public void tentarAgendarComServicoInvalido() throws Exception {
        Map<String, Object> request = criarAgendamentoRequest(LocalDateTime.now().plusDays(1), "Teste", List.of(999L));

        resultado = mockMvc.perform(post("/pets/{petId}/agendamentos", petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Quando("listar os agendamentos do pet")
    public void listarOsAgendamentosDoPet() throws Exception {
        resultado = mockMvc.perform(get("/pets/{petId}/agendamentos", petId)).andReturn();
    }

    @Quando("buscar o agendamento ativo do pet")
    public void buscarOAgendamentoAtivoDoPet() throws Exception {
        resultado = mockMvc.perform(get("/pets/{petId}/agendamentos/{id}", petId, agendamentoId)).andReturn();
    }

    @Quando("reagendar o agendamento com os dados:")
    public void reagendarOAgendamentoComOsDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        Map<String, Object> request = criarAgendamentoRequest(
                LocalDateTime.parse(dados.get("dataHora"), FORMATADOR),
                dados.get("observacoes"),
                List.of(servicoIdsCriados.get(1))
        );

        resultado = mockMvc.perform(put("/pets/{petId}/agendamentos/{id}", petId, agendamentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Quando("cancelar o agendamento do pet")
    public void cancelarOAgendamentoDoPet() throws Exception {
        resultado = mockMvc.perform(delete("/pets/{petId}/agendamentos/{id}", petId, agendamentoId)).andReturn();
    }

    @Quando("buscar um agendamento inexistente do pet")
    public void buscarUmAgendamentoInexistenteDoPet() throws Exception {
        resultado = mockMvc.perform(get("/pets/{petId}/agendamentos/{id}", petId, 999L)).andReturn();
    }

    @Quando("tentar reagendar com data no passado")
    public void tentarReagendarComDataNoPassado() throws Exception {
        Map<String, Object> request = criarAgendamentoRequest(
                LocalDateTime.now().minusDays(1),
                "Inválido",
                List.of(servicoIdsCriados.getFirst())
        );

        resultado = mockMvc.perform(put("/pets/{petId}/agendamentos/{id}", petId, agendamentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Entao("o retorno do agendamento deve ser o status {int}")
    public void oRetornoDoAgendamentoDeveSerOStatus(int statusEsperado) {
        assertThat(resultado).isNotNull();
        assertThat(resultado.getResponse().getStatus()).isEqualTo(statusEsperado);
    }

    @Entao("o agendamento criado deve ter identificador")
    public void oAgendamentoCriadoDeveTerIdentificador() throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());

        assertThat(response.hasNonNull("id")).isTrue();
        assertThat(response.get("id").asLong()).isPositive();
    }

    @Entao("a lista de agendamentos deve conter ao menos um item")
    public void aListaDeAgendamentosDeveConterAoMenosUmItem() throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(response.isArray()).isTrue();
        assertThat(response.size()).isPositive();
    }

    @Entao("a lista de agendamentos deve estar vazia")
    public void aListaDeAgendamentosDeveEstarVazia() throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(response.isArray()).isTrue();
        assertThat(response.size()).isZero();
    }

    @Entao("o agendamento retornado deve possuir status {string}")
    public void oAgendamentoRetornadoDevePossuirStatus(String statusEsperado) throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(response.get("status").asText()).isEqualTo(statusEsperado);
    }

    @Entao("o agendamento retornado deve possuir os serviços atualizados")
    public void oAgendamentoRetornadoDevePossuirOsServicosAtualizados() throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());

        assertThat(response.get("servicos").isArray()).isTrue();
        assertThat(response.get("servicos").size()).isEqualTo(1);
        assertThat(response.get("servicos").get(0).get("servicoId").asLong()).isEqualTo(servicoIdsCriados.get(1));
    }

    private Long criarTutor() throws Exception {
        Map<String, Object> tutor = new LinkedHashMap<>();
        tutor.put("nome", "Wallace");
        tutor.put("cpf", CpfTestData.gerar());
        tutor.put("email", "wallace+agendamento" + System.nanoTime() + "@test.com");
        tutor.put("telefone", "11999999999");

        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("cep", "01001-001");
        endereco.put("logradouro", "Praça da Sé");
        endereco.put("numero", "1");
        endereco.put("complemento", null);
        endereco.put("bairro", "Sé");
        endereco.put("cidade", "São Paulo");
        endereco.put("estado", "SP");
        tutor.put("endereco", endereco);

        MvcResult tutorResult = mockMvc.perform(post("/tutores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tutor))).andReturn();

        return obterId(tutorResult);
    }

    private Long criarPet(Long tutorId) throws Exception {
        Map<String, Object> pet = new LinkedHashMap<>();
        pet.put("nome", "Rex");
        pet.put("especie", "Cachorro");
        pet.put("raca", "SRD");
        pet.put("idade", 4);
        pet.put("peso", new BigDecimal("12.50"));

        MvcResult petResult = mockMvc.perform(post("/tutores/{tutorId}/pets", tutorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pet))).andReturn();

        return obterId(petResult);
    }

    private Long criarServico(String nome, String preco) throws Exception {
        Map<String, Object> servico = new LinkedHashMap<>();
        servico.put("nome", nome);
        servico.put("descricao", nome + " completo");
        servico.put("preco", new BigDecimal(preco));
        servico.put("tempoEstimadoMinutos", 60);

        MvcResult servicoResult = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(servico))).andReturn();

        return obterId(servicoResult);
    }

    private Map<String, Object> criarAgendamentoRequest(LocalDateTime dataHora, String observacoes, List<Long> servicoIds) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("dataHora", dataHora.withNano(0).format(FORMATADOR));
        request.put("observacoes", observacoes);
        request.put("servicoIds", servicoIds);
        return request;
    }

    private Long obterId(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }
}
