package br.iwmvi.petshop.servico;

import br.iwmvi.petshop.servico.dto.request.ServicoRequest;
import br.iwmvi.petshop.servico.model.Servico;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

public class ServicoTestData {

    public static ServicoRequest criarServicoRequest() {
        return new ServicoRequest(
                "Banho e Tosa",
                "Banho completo com tosa",
                new BigDecimal("150.00"),
                60
        );
    }

    public static Servico criarServico() {
        var servico = new Servico(
                "Banho e Tosa",
                "Banho completo com tosa",
                new BigDecimal("150.00"),
                60
        );

        ReflectionTestUtils.setField(servico, "id", 1L);

        return servico;
    }
}
