package br.iwmvi.petshop.common.validator;

import br.iwmvi.petshop.exception.ValidationException;

import java.util.List;

/**
 * Implementação do padrão Composite para validação.
 *
 * Agrega múltiplos validadores (Strategy) em uma única validação encadeada,
 * permitindo que regras independentes sejam compostas sem acoplamento.
 *
 * @param <T> O tipo de entidade validada
 */
public class CompositeValidator<T> implements EntityValidator<T> {

    private final List<EntityValidator<T>> validators;

    public CompositeValidator(List<EntityValidator<T>> validators) {
        this.validators = List.copyOf(validators);
    }

    @Override
    public void validate(T entity) throws ValidationException {
        for (EntityValidator<T> validator : validators) {
            validator.validate(entity);
        }
    }
}