package br.iwmvi.petshop.common.mapper;

import java.util.List;

/**
 * Interface para conversão de entidades em DTOs de response.
 *
 * @param <T> O tipo de entidade
 * @param <RES> O tipo de DTO response
 */
public interface ResponseMapper<T, RES> {

    /**
     * Converte uma entidade em um DTO de response.
     *
     * @param entity a entidade a converter
     * @return o DTO response
     */
    RES toResponse(T entity);

    /**
     * Converte uma lista de entidades em uma lista de DTOs de response.
     *
     * @param entities as entidades a converter
     * @return a lista de DTOs response
     */
    default List<RES> toResponseList(List<T> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
