package br.iwmvi.petshop.financeiro.validator;

import br.iwmvi.petshop.common.validator.CompositeValidator;
import br.iwmvi.petshop.common.validator.EntityValidator;
import br.iwmvi.petshop.financeiro.model.LancamentoFinanceiro;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Monta o validador composto (Composite) de lançamentos financeiros a
 * partir das estratégias individuais ({@link EntityValidator}).
 */
@Configuration
public class LancamentoFinanceiroValidatorConfig {

    @Bean
    public EntityValidator<LancamentoFinanceiro> lancamentoFinanceiroValidator() {
        return new CompositeValidator<>(List.of(
                new CategoriaCompativelComTipoValidador(),
                new StatusCoerenteComDatasValidador()
        ));
    }
}
