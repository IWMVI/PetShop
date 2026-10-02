package br.iwmvi.petshop.financeiro.cucumber;

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

public class FinanceiroSteps {

    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult resultado;
    private Long lancamentoId;
    private Long pagamentoId;

    @Dado("que existe um lançamento manual registrado")
    public void queExisteUmLancamentoManualRegistrado() throws Exception {
        Map<String, Object> request = criarLancamentoRequest("ENTRADA", "VENDA_PRODUTO", "Venda avulsa", new BigDecimal("50.00"));

        resultado = mockMvc.perform(post("/financeiro/lancamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();

        lancamentoId = obterId(resultado);
    }

    @Dado("que existe um pagamento de agendamento marcado como pago")
    public void queExisteUmPagamentoDeAgendamentoMarcadoComoPago() throws Exception {
        Long tutorId = criarTutor();
        Long petId = criarPet(tutorId);
        Long servicoId = criarServico("Banho", "50.00");
        Long agendamentoId = criarAgendamento(petId, servicoId);

        Map<String, Object> pagamentoRequest = new LinkedHashMap<>();
        pagamentoRequest.put("valor", new BigDecimal("50.00"));
        pagamentoRequest.put("metodoPagamento", "PIX");

        MvcResult pagamentoResult = mockMvc.perform(post("/agendamentos/{agendamentoId}/pagamentos", agendamentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagamentoRequest))).andReturn();
        pagamentoId = obterId(pagamentoResult);

        Map<String, Object> statusRequest = new LinkedHashMap<>();
        statusRequest.put("status", "PAGO");
        statusRequest.put("dataPagamento", LocalDateTime.now().withNano(0).format(FORMATADOR));

        mockMvc.perform(put("/pagamentos/{id}", pagamentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest))).andReturn();
    }

    @Quando("registrar uma entrada com os dados:")
    public void registrarUmaEntradaComOsDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);
        Map<String, Object> request = criarLancamentoRequest("ENTRADA", dados.get("categoria"), dados.get("descricao"), new BigDecimal(dados.get("valor")));

        resultado = mockMvc.perform(post("/financeiro/lancamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();

        if (resultado.getResponse().getStatus() == 201) {
            lancamentoId = obterId(resultado);
        }
    }

    @Quando("registrar uma saída com os dados:")
    public void registrarUmaSaidaComOsDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);
        Map<String, Object> request = criarLancamentoRequest("SAIDA", dados.get("categoria"), dados.get("descricao"), new BigDecimal(dados.get("valor")));

        resultado = mockMvc.perform(post("/financeiro/lancamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Quando("buscar o lançamento registrado")
    public void buscarOLancamentoRegistrado() throws Exception {
        resultado = mockMvc.perform(get("/financeiro/lancamentos/{id}", lancamentoId)).andReturn();
    }

    @Quando("buscar um lançamento financeiro inexistente")
    public void buscarUmLancamentoFinanceiroInexistente() throws Exception {
        resultado = mockMvc.perform(get("/financeiro/lancamentos/{id}", 999999L)).andReturn();
    }

    @Quando("atualizar o lançamento registrado com os dados:")
    public void atualizarOLancamentoRegistradoComOsDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);
        Map<String, Object> request = criarLancamentoRequest("ENTRADA", dados.get("categoria"), dados.get("descricao"), new BigDecimal(dados.get("valor")));

        resultado = mockMvc.perform(put("/financeiro/lancamentos/{id}", lancamentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Quando("cancelar o lançamento registrado")
    public void cancelarOLancamentoRegistrado() throws Exception {
        resultado = mockMvc.perform(delete("/financeiro/lancamentos/{id}", lancamentoId)).andReturn();
    }

    @Quando("tentar atualizar o lançamento gerado pelo pagamento")
    public void tentarAtualizarOLancamentoGeradoPeloPagamento() throws Exception {
        Long id = obterIdDoLancamentoDoPagamento();
        Map<String, Object> request = criarLancamentoRequest("ENTRADA", "VENDA_PRODUTO", "Tentativa de edição", new BigDecimal("1.00"));

        resultado = mockMvc.perform(put("/financeiro/lancamentos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Quando("tentar cancelar o lançamento gerado pelo pagamento")
    public void tentarCancelarOLancamentoGeradoPeloPagamento() throws Exception {
        Long id = obterIdDoLancamentoDoPagamento();
        resultado = mockMvc.perform(delete("/financeiro/lancamentos/{id}", id)).andReturn();
    }

    @Quando("consultar o extrato financeiro")
    public void consultarOExtratoFinanceiro() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now().plusDays(1);

        resultado = mockMvc.perform(get("/financeiro/extrato")
                .param("inicio", inicio.format(FORMATADOR))
                .param("fim", fim.format(FORMATADOR))).andReturn();
    }

    @Entao("o retorno do financeiro deve ser o status {int}")
    public void oRetornoDoFinanceiroDeveSerOStatus(int statusEsperado) {
        assertThat(resultado).isNotNull();
        assertThat(resultado.getResponse().getStatus()).isEqualTo(statusEsperado);
    }

    @Entao("o lançamento criado deve ter identificador")
    public void oLancamentoCriadoDeveTerIdentificador() throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());

        assertThat(response.hasNonNull("id")).isTrue();
        assertThat(response.get("id").asLong()).isPositive();
    }

    @Entao("o extrato deve conter ao menos um lançamento")
    public void oExtratoDeveConterAoMenosUmLancamento() throws Exception {
        JsonNode response = objectMapper.readTree(resultado.getResponse().getContentAsString());
        JsonNode itens = response.get("lancamentos").get("itens");

        assertThat(itens.isArray()).isTrue();
        assertThat(itens.size()).isPositive();
    }

    @Entao("o extrato deve conter um lançamento vinculado ao pagamento")
    public void oExtratoDeveConterUmLancamentoVinculadoAoPagamento() throws Exception {
        assertThat(obterIdDoLancamentoDoPagamento()).isNotNull();
    }

    private Long obterIdDoLancamentoDoPagamento() throws Exception {
        MvcResult extratoResult = mockMvc.perform(get("/financeiro/extrato")
                .param("inicio", LocalDateTime.now().minusDays(1).format(FORMATADOR))
                .param("fim", LocalDateTime.now().plusDays(1).format(FORMATADOR))
                .param("tamanho", "50")).andReturn();

        JsonNode itens = objectMapper.readTree(extratoResult.getResponse().getContentAsString())
                .get("lancamentos").get("itens");

        for (JsonNode item : itens) {
            if (item.hasNonNull("pagamentoId") && item.get("pagamentoId").asLong() == pagamentoId) {
                return item.get("id").asLong();
            }
        }
        throw new AssertionError("Nenhum lançamento vinculado ao pagamento " + pagamentoId + " foi encontrado no extrato.");
    }

    private Long criarTutor() throws Exception {
        Map<String, Object> tutor = new LinkedHashMap<>();
        tutor.put("nome", "Wallace");
        tutor.put("cpf", CpfTestData.gerar());
        tutor.put("email", "wallace+financeiro" + System.nanoTime() + "@test.com");
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
        request.put("observacoes", "Agendamento para financeiro");
        request.put("servicoIds", List.of(servicoId));

        MvcResult agendamentoResult = mockMvc.perform(post("/pets/{petId}/agendamentos", petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();

        return obterId(agendamentoResult);
    }

    private Map<String, Object> criarLancamentoRequest(String tipo, String categoria, String descricao, BigDecimal valor) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("tipo", tipo);
        request.put("categoria", categoria);
        request.put("descricao", descricao);
        request.put("valor", valor);
        request.put("data", LocalDateTime.now().withNano(0).format(FORMATADOR));
        return request;
    }

    private Long obterId(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }
}
