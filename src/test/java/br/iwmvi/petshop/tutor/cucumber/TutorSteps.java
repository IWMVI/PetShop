package br.iwmvi.petshop.tutor.cucumber;

import br.iwmvi.petshop.endereco.EnderecoTestData;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TutorSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult resultado;

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

    @Entao("o cadastro do tutor deve retornar o status {int}")
    public void cadastroDoTutorDeveRetornarStatus(int statusEsperado) {
        assertThat(resultado)
                .as("Resultado da requisição deve existir")
                .isNotNull();

        assertThat(resultado.getResponse().getStatus())
                .as("Status HTTP retornado pelo cadastro")
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

    private MvcResult realizarCadastro(Map<String, Object> request) throws Exception {
        return mockMvc.perform(
                        post("/tutores")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andReturn();
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