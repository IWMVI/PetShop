package br.iwmvi.petshop.servico.cucumber;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class ServicoSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult resultado;

    private Long servicoId;

    @Dado("que existe um serviço cadastrado")
    public void queExisteUmServicoCadastrado() throws Exception {
        Map<String, Object> servicoRequest = criarServicoRequest(
                "Banho e Tosa",
                "Banho completo com tosa",
                "150.00",
                "60"
        );

        resultado = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(servicoRequest))
        ).andReturn();

        servicoId = obterId(resultado);
    }

    @Quando("cadastrar um serviço com os seguintes dados:")
    public void cadastrarUmServicoComOsSeguintesDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        Map<String, Object> servicoRequest = criarServicoRequest(
                dados.get("nome"),
                dados.get("descricao"),
                dados.get("preco"),
                dados.get("tempoEstimadoMinutos")
        );

        resultado = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(servicoRequest))
        ).andReturn();
    }

    @Quando("tentar cadastrar um serviço com nome vazio")
    public void tentarCadastrarUmServicoComNomeVazio() throws Exception {
        Map<String, Object> servicoRequest = criarServicoRequest(
                "",
                "Descrição",
                "150.00",
                "60"
        );

        resultado = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(servicoRequest))
        ).andReturn();
    }

    @Quando("tentar cadastrar um serviço com preço {string}")
    public void tentarCadastrarUmServicoComPreco(String preco) throws Exception {
        Map<String, Object> servicoRequest = criarServicoRequest(
                "Banho e Tosa",
                "Descrição",
                preco,
                "60"
        );

        resultado = mockMvc.perform(post("/servicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(servicoRequest))
        ).andReturn();
    }

    @Quando("listar os serviços")
    public void listarOsServicos() throws Exception {
        resultado = mockMvc.perform(get("/servicos")).andReturn();
    }

    @Quando("buscar o serviço cadastrado")
    public void buscarOServicoCadastrado() throws Exception {
        resultado = mockMvc.perform(get("/servicos/{id}", servicoId)).andReturn();
    }

    @Quando("tentar buscar um serviço inexistente")
    public void tentarBuscarUmServicoInexistente() throws Exception {
        resultado = mockMvc.perform(get("/servicos/{id}", 999L)).andReturn();
    }

    @Quando("atualizar o serviço cadastrado com os seguintes dados:")
    public void atualizarOServicoCadastradoComOsSeguintesDados(DataTable dataTable) throws Exception {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        Map<String, Object> servicoRequest = criarServicoRequest(
                dados.get("nome"),
                dados.get("descricao"),
                dados.get("preco"),
                dados.get("tempoEstimadoMinutos")
        );

        resultado = mockMvc.perform(put("/servicos/{id}", servicoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(servicoRequest))
        ).andReturn();
    }

    @Quando("tentar atualizar um serviço inexistente")
    public void tentarAtualizarUmServicoInexistente() throws Exception {
        Map<String, Object> servicoRequest = criarServicoRequest(
                "Banho e Tosa",
                "Descrição",
                "150.00",
                "60"
        );

        resultado = mockMvc.perform(put("/servicos/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(servicoRequest))
        ).andReturn();
    }

    @Quando("excluir o serviço cadastrado")
    public void excluirOServicoCadastrado() throws Exception {
        resultado = mockMvc.perform(delete("/servicos/{id}", servicoId)).andReturn();
    }

    @Quando("tentar excluir um serviço inexistente")
    public void tentarExcluirUmServicoInexistente() throws Exception {
        resultado = mockMvc.perform(delete("/servicos/{id}", 999L)).andReturn();
    }

    @Entao("o cadastro do serviço deve retornar o status {int}")
    public void cadastroDoServicoDeveRetornarStatus(int statusEsperado) {
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

    @Entao("o serviço cadastrado deve possuir um identificador")
    public void servicoCadastradoDevePossuirIdentificador() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.hasNonNull("id"))
                .as("Resposta deve possuir o campo id")
                .isTrue();

        assertThat(response.get("id").asLong())
                .as("Identificador do serviço deve ser positivo")
                .isPositive();
    }

    @Entao("a resposta deve conter uma lista de serviços")
    public void respostaDeveConterUmaListaDeServicos() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.isArray())
                .as("Resposta deve ser uma lista")
                .isTrue();

        assertThat(response.size())
                .as("Lista deve conter ao menos um serviço")
                .isPositive();
    }

    @Entao("a resposta deve conter uma lista vazia de serviços")
    public void respostaDeveConterUmaListaVaziaDeServicos() throws Exception {
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

    @Entao("o serviço retornado deve possuir um identificador")
    public void servicoRetornadoDevePossuirIdentificador() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.hasNonNull("id"))
                .as("Resposta deve possuir o campo id")
                .isTrue();
    }

    @Entao("o serviço retornado deve possuir os dados atualizados")
    public void servicoRetornadoDevePossuirOsDadosAtualizados() throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        assertThat(response.hasNonNull("nome"))
                .as("Resposta deve possuir o campo nome")
                .isTrue();

        assertThat(response.get("nome").asString())
                .as("Nome do serviço deve ser atualizado")
                .isEqualTo("Tosa Premium");

        assertThat(response.get("preco").asText())
                .as("Preço do serviço deve ser atualizado")
                .isEqualTo("200.00");

        assertThat(response.get("tempoEstimadoMinutos").asInt())
                .as("Tempo do serviço deve ser atualizado")
                .isEqualTo(90);
    }

    private Map<String, Object> criarServicoRequest(String nome, String descricao, String preco, String tempoEstimado) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("nome", nome);
        request.put("descricao", descricao);
        request.put("preco", preco != null && !preco.isEmpty() ? new BigDecimal(preco) : null);
        request.put("tempoEstimadoMinutos", tempoEstimado != null && !tempoEstimado.isEmpty() ? Integer.parseInt(tempoEstimado) : null);

        return request;
    }

    private Long obterId(MvcResult resultado) throws Exception {
        JsonNode response = objectMapper.readTree(
                resultado.getResponse().getContentAsString()
        );

        return response.get("id").asLong();
    }
}
