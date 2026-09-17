package br.iwmvi.petshop.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Interface base de repositório para entidades usando padrão soft delete.
 *
 * Fornece operações comuns para entidades soft-deleted, filtrando registros deletados
 * automaticamente. Repositórios estendendo esta interface obtêm métodos de query que retornam
 * apenas registros ativos (não deletados).
 *
 * @param <T> O tipo de entidade
 * @param <ID> O tipo de ID da entidade
 * @see SoftDeleteRepositoryImpl
 * @see br.iwmvi.petshop.common.entity.SoftDeleteEntity
 */
@NoRepositoryBean
public interface SoftDeleteRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Encontra uma entidade ativa pelo seu ID.
     *
     * @param id o ID da entidade
     * @return um Optional contendo a entidade se encontrada e ativa, vazio caso contrario
     */
    Optional<T> findActiveById(ID id);

    /**
     * Encontra todas as entidades ativas.
     *
     * @return uma lista de todas as entidades ativas
     */
    List<T> findAllActive();

    /**
     * Soft delete de uma entidade por ID (define timestamp deletedAt em vez de deletar fisicamente).
     *
     * @param id o ID da entidade
     */
    void softDelete(ID id);

    /**
     * Soft delete de multiplas entidades por seus IDs.
     *
     * @param ids os IDs das entidades para soft delete
     */
    void softDeleteAll(Iterable<ID> ids);
}
