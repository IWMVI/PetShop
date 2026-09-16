package br.iwmvi.petshop.endereco;

import br.iwmvi.petshop.endereco.dto.request.EnderecoRequest;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EnderecoTestData {

    private EnderecoTestData() {
    }

    public static EnderecoRequest criarEnderecoRequest() {
        return criarEnderecoRequest(
                "01001-001",
                "Praça da Sé",
                "1",
                "Lado Ímpar",
                "Sé",
                "São Paulo",
                "SP"
        );
    }

    public static EnderecoRequest criarEnderecoRequest(
            String cep, String logradouro, String numero, String complemento,
            String bairro, String cidade, String estado) {
        return new EnderecoRequest(
                cep,
                logradouro,
                numero,
                complemento,
                bairro,
                cidade,
                estado
        );
    }

    public static Map<String, Object> criarEnderecoJson(
            String cep, String logradouro, String numero, String complemento,
            String bairro, String cidade, String estado) {
        Map<String, Object> endereco = new LinkedHashMap<>();

        endereco.put("cep", cep);
        endereco.put("logradouro", logradouro);
        endereco.put("numero", numero);
        endereco.put("complemento", complemento);
        endereco.put("bairro", bairro);
        endereco.put("cidade", cidade);
        endereco.put("estado", estado);

        return endereco;
    }
}