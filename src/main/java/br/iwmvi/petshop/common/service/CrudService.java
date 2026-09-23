package br.iwmvi.petshop.common.service;

import br.iwmvi.petshop.common.entity.SoftDeleteEntity;
import br.iwmvi.petshop.common.mapper.ResponseMapper;
import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.exception.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço CRUD base para entidades usando padrão soft delete.
 *
 * Fornece operações padrão Create, Read, Update, Delete para entidades.
 * Todos os métodos trabalham automaticamente com o padrão soft delete através do repositório.
 *
 * Subclasses devem implementar métodos abstratos para fornecer comportamento específico da entidade:
 * - {@link #mapToEntity(Object)} - Converter request para entity
 * - {@link #updateEntity(SoftDeleteEntity, Object)} - Atualizar campos da entity
 * - {@link #getEntityName()} - Nome da entidade para mensagens de erro
 *
 * @param <T> O tipo de entidade (deve estender SoftDeleteEntity)
 * @param <ID> O tipo de ID da entidade
 * @param <REQ> O tipo de DTO request
 * @param <RES> O tipo de DTO response
 */
@Transactional
public abstract class CrudService<T extends SoftDeleteEntity, ID, REQ, RES> {

    /**
     * Obtém a instância do repositório para este serviço.
     *
     * @return a instância do SoftDeleteRepository
     */
    protected abstract SoftDeleteRepository<T, ID> getRepository();

    /**
     * Obtém o mapper de response para converter entidades em DTOs de response.
     *
     * @return a instância do ResponseMapper
     */
    protected abstract ResponseMapper<T, RES> getMapper();

    /**
     * Converte um DTO request para uma entidade.
     *
     * @param request o DTO request
     * @return a entidade
     */
    protected abstract T mapToEntity(REQ request);

    /**
     * Atualiza uma entidade existente com valores de um DTO request.
     *
     * @param entity a entidade para atualizar
     * @param request o DTO request contendo novos valores
     */
    protected abstract void updateEntity(T entity, REQ request);

    /**
     * Cria uma nova entidade a partir de um DTO request.
     *
     * @param request o DTO request
     * @return o DTO response
     */
    public RES create(REQ request) {
        T entity = mapToEntity(request);
        validateBeforeSave(entity);
        T saved = getRepository().save(entity);
        return getMapper().toResponse(saved);
    }

    /**
     * Encontra uma entidade ativa pelo ID.
     *
     * @param id o ID da entidade
     * @return o DTO response
     * @throws EntityNotFoundException se entidade não for encontrada ou estiver deletada
     */
    @Transactional(readOnly = true)
    public RES findById(ID id) {
        T entity = findByIdOrThrow(id);
        return getMapper().toResponse(entity);
    }

    /**
     * Encontra todas as entidades ativas.
     *
     * @return uma lista de DTOs response
     */
    @Transactional(readOnly = true)
    public List<RES> findAll() {
        List<T> entities = getRepository().findAllActive();
        return getMapper().toResponseList(entities);
    }

    /**
     * Atualiza uma entidade existente.
     *
     * @param id o ID da entidade
     * @param request o DTO request com novos valores
     * @return o DTO response atualizado
     * @throws EntityNotFoundException se entidade não for encontrada ou estiver deletada
     */
    public RES update(ID id, REQ request) {
        T entity = findByIdOrThrow(id);
        validateUpdate(entity, request);
        updateEntity(entity, request);
        T updated = getRepository().save(entity);
        return getMapper().toResponse(updated);
    }

    /**
     * Deleta (soft delete) uma entidade pelo ID.
     *
     * @param id o ID da entidade
     * @throws EntityNotFoundException se entidade não for encontrada ou estiver deletada
     */
    public void delete(ID id) {
        findByIdOrThrow(id);
        getRepository().softDelete(id);
    }

    /**
     * Encontra uma entidade pelo ID ou lança uma exceção se não encontrada.
     *
     * @param id o ID da entidade
     * @return a entidade
     * @throws EntityNotFoundException se entidade não for encontrada ou estiver deletada
     */
    protected T findByIdOrThrow(ID id) {
        return getRepository().findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityName() + " com ID " + id + " não encontrado"));
    }

    /**
     * Valida a atualização de uma entidade antes de aplicar os novos valores.
     *
     * É invocada com a entidade persistida (estado anterior) e o DTO request, antes de
     * {@link #updateEntity} mutar a entidade. Por padrão delega a
     * {@link #validateBeforeSave}, mas pode ser sobrescrita para validar somente
     * o delta entre o estado persistido e a requisição (ex.: não relançar regra de
     * unicidade quando o valor não mudou).
     *
     * @param entity a entidade persistida (estado anterior)
     * @param request o DTO request com os novos valores
     */
    protected void validateUpdate(T entity, REQ request) {
        validateBeforeSave(entity);
    }

    /**
     * Valida uma entidade antes de salvar. Sobrescreva em subclasses para adicionar validação customizada.
     *
     * @param entity a entidade para validar
     */
    protected void validateBeforeSave(T entity) {
        // Sobrescreva em subclasses se necessário
    }

    /**
     * Obtém o nome da entidade para mensagens de erro.
     *
     * @return o nome da entidade
     */
    protected abstract String getEntityName();
}
