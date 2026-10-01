package br.iwmvi.petshop.agendamento.validator;

import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.common.validator.EntityValidator;
import br.iwmvi.petshop.exception.AgendamentoValidationException;
import br.iwmvi.petshop.exception.ValidationException;

/**
 * Strategy de validação: um agendamento deve possuir pelo menos um serviço.
 */
public class ServicosObrigatoriosValidador implements EntityValidator<Agendamento> {

    @Override
    public void validate(Agendamento agendamento) throws ValidationException {
        if (agendamento.getAgendamentoServicos() == null || agendamento.getAgendamentoServicos().isEmpty()) {
            throw new AgendamentoValidationException("Pelo menos um serviço deve ser informado.");
        }
    }
}