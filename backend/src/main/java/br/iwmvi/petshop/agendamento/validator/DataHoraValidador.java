package br.iwmvi.petshop.agendamento.validator;

import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.common.validator.EntityValidator;
import br.iwmvi.petshop.exception.AgendamentoValidationException;
import br.iwmvi.petshop.exception.ValidationException;

import java.time.LocalDateTime;

/**
 * Strategy de validação: a data/hora do agendamento é obrigatória e não pode
 * estar no passado.
 */
public class DataHoraValidador implements EntityValidator<Agendamento> {

    @Override
    public void validate(Agendamento agendamento) throws ValidationException {
        LocalDateTime dataHora = agendamento.getDataHora();
        if (dataHora == null) {
            throw new AgendamentoValidationException("A data/hora do agendamento é obrigatória.");
        }
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new AgendamentoValidationException("Data/hora do agendamento não pode ser no passado.");
        }
    }
}