package br.iwmvi.petshop.pagamento.cucumber;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class PagamentoSteps {

    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult resultado;
    private Long agendamentoId;
    private Long pagamentoId;

    @Dado("que existe um agendamento ativo disponível para pagamento")
    public void queExisteUmAgendamentoAtivoDisponivelParaPagamento() throws Exception {
        Long tutorId = criarTutor();
        Long petId = criarPet(tutorId);
        Long servicoId = criarServico("Banho", "50.00");
        agendamentoId = criarAgendamento(petId, servicoId);
    }

    @Dado("que existe um pagamento registrado para o agendamento")
    public void queExisteUmPagamentoRegistradoParaOAgendamento() throws Exception {
        queExisteUmAgendamentoAtivoDisponivelParaPagamento();

        Map<String, Object> request = criarPagamentoRequest(new BigDecimal("150.00"), "PIX");

        resultado = mockMvc.perform(post("/agendamentos/{agendamentoId}/pagamentos", agendamentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();

        pagamentoId = obterId(resultado);
    }

    @Quando("registrar um pagamento para o agendamento com os dados:")
    public void registrarUmPagamentoParaOAgendamentoComOsDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        Map<String, Object> request = criarPagamentoRequest(
                new BigDecimal(dados.get("valor")),
                dados.get("metodoPagamento")
        );

        resultado = mockMvc.perform(post("/agendamentos/{agendamentoId}/pagamentos", agendamentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();

        if (resultado.getResponse().getStatus() == 201) {
            pagamentoId = obterId(resultado);
        }
    }

    @Quando("tentar registrar pagamento para um agendamento inexistente")
    public void tentarRegistrarPagamentoParaUmAgendamentoInexistente() throws Exception {
        Map<String, Object> request = criarPagamentoRequest(new BigDecimal("150.00"), "PIX");

        resultado = mockMvc.perform(post("/agendamentos/{agendamentoId}/pagamentos", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Quando("tentar registrar pagamento com valor inválido")
    public void tentarRegistrarPagamentoComValorInvalido() throws Exception {
        Map<String, Object> request = criarPagamentoRequest(BigDecimal.ZERO, "PIX");

        resultado = mockMvc.perform(post("/agendamentos/{agendamentoId}/pagamentos", agendamentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Quando("listar os pagamentos do agendamento")
    public void listarOsPagamentosDoAgendamento() throws Exception {
        resultado = mockMvc.perform(get("/agendamentos/{agendamentoId}/pagamentos", agendamentoId)).andReturn();
    }

    @Quando("buscar o pagamento registrado")
    public void buscarOPagamentoRegistrado() throws Exception {
        resultado = mockMvc.perform(get("/pagamentos/{id}", pagamentoId)).andReturn();
    }

    @Quando("buscar um pagamento inexistente")
    public void buscarUmPagamentoInexistente() throws Exception {
        resultado = mockMvc.perform(get("/pagamentos/{id}", 999L)).andReturn();
    }

    @Quando("atualizar o status do pagamento para PAGO com data de pagamento")
    public void atualizarOStatusDoPagamentoParaPagoComDataDePagamento() throws Exception {
        Map<String, Object> request = criarAtualizarStatusRequest("PAGO", LocalDateTime.now());

        resultado = mockMvc.perform(put("/pagamentos/{id}", pagamentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Quando("tentar atualizar o status do pagamento para PAGO sem data de pagamento")
    public void tentarAtualizarOStatusDoPagamentoParaPagoSemDataDePagamento() throws Exception {
        Map<String, Object> request = criarAtualizarStatusRequest("PAGO", null);

        resultado = mockMvc.perform(put("/pagamentos/{id}", pagamentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Quando("cancelar o pagamento registrado")
    public void cancelarOPagamentoRegistrado() throws Exception {
        resultado = mockMvc.perform(delete("/pagamentos/{id}", pagamentoId)).andReturn();
    }

    @Quando("tentar cancelar um pagamento inexistente")
    public void tentarCancelarUmPagamentoInexistente() throws Exception {
        resultado = mockMvc.perform(delete("/pagamentos/{id}", 999L)).andReturn();
    }

    @Entao("o retorno do pagamento deve ser o status {int}")
    public void oRetornoDoPagamentoDeveSerOStatus(int statusEsperado) {
        assertThat(resultado).isNotNull();
        assertThat(resultado.getResponse().getStatus()).isEqualTo(statusEsperado);
    }

    @Entao("o pagamento criado deve ter identificador")
    public void oPagamentoCriadoDeveTerIdentificador() throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());

        assertThat(response.hasNonNull("id")).isTrue();
        assertThat(response.get("id").asLong()).isPositive();
    }

    @Entao("a lista de pagamentos deve conter ao menos um item")
    public void aListaDePagamentosDeveConterAoMenosUmItem() throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(response.isArray()).isTrue();
        assertThat(response.size()).isPositive();
    }

    @Entao("a lista de pagamentos deve estar vazia")
    public void aListaDePagamentosDeveEstarVazia() throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(response.isArray()).isTrue();
        assertThat(response.size()).isZero();
    }

    @Entao("o pagamento retornado deve possuir status {string}")
    public void oPagamentoRetornadoDevePossuirStatus(String statusEsperado) throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(response.get("status").asText()).isEqualTo(statusEsperado);
    }

    private Long criarTutor() throws Exception {
        Map<String, Object> tutor = new LinkedHashMap<>();
        tutor.put("nome", "Wallace");
        tutor.put("cpf", CpfTestData.gerar());
        tutor.put("email", "wallace+pagamento" + System.nanoTime() + "@test.com");
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

    private Long criarAgendamento(Long petId, Long servicoId) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("dataHora", LocalDateTime.now().plusDays(1).withNano(0).format(FORMATADOR));
        request.put("observacoes", "Agendamento para pagamento");
        request.put("servicoIds", List.of(servicoId));

        MvcResult agendamentoResult = mockMvc.perform(post("/pets/{petId}/agendamentos", petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();

        return obterId(agendamentoResult);
    }

    private Map<String, Object> criarPagamentoRequest(BigDecimal valor, String metodoPagamento) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("valor", valor);
        request.put("metodoPagamento", metodoPagamento);
        return request;
    }

    private Map<String, Object> criarAtualizarStatusRequest(String status, LocalDateTime dataPagamento) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("status", status);
        request.put("dataPagamento", dataPagamento == null ? null : dataPagamento.withNano(0).format(FORMATADOR));
        return request;
    }

    private Long obterId(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }
}
