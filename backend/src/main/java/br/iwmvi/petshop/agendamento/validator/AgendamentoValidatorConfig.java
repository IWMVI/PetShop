package br.iwmvi.petshop.agendamento.validator;

import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.common.validator.CompositeValidator;
import br.iwmvi.petshop.common.validator.EntityValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Monta o validador composto (Composite) de agendamentos a partir das
 * estratégias individuais ({@link EntityValidator}).
 */
@Configuration
public class AgendamentoValidatorConfig {

    @Bean
    public EntityValidator<Agendamento> agendamentoValidator() {
        return new CompositeValidator<>(List.of(
                new DataHoraValidador(),
                new ServicosObrigatoriosValidador()
        ));
    }
}