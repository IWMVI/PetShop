package br.iwmvi.petshop.pagamento.validator;

import br.iwmvi.petshop.common.validator.CompositeValidator;
import br.iwmvi.petshop.common.validator.EntityValidator;
import br.iwmvi.petshop.pagamento.model.Pagamento;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Monta o validador composto (Composite) de pagamentos a partir das
 * estratégias individuais ({@link EntityValidator}).
 */
@Configuration
public class PagamentoValidatorConfig {

    @Bean
    public EntityValidator<Pagamento> pagamentoValidator() {
        return new CompositeValidator<>(List.of(
                new ValorPositivoValidador(),
                new DataPagamentoCoerenteValidador()
        ));
    }
}
