package br.iwmvi.petshop.common.validator;

/**
 * Interface para validação de entidades usando o padrão Strategy.
 *
 * Implemente esta interface para criar validadores para entidades específicas.
 * Validadores podem ser compostos para executar lógica de validação complexa.
 *
 * @param <T> O tipo de entidade a validar
 */
public interface EntityValidator<T> {

    /**
     * Valida a entidade fornecida.
     *
     * @param entity a entidade a validar
     * @throws ValidationException se a validação falhar
     */
    void validate(T entity) throws ValidationException;
}
