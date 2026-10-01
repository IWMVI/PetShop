package br.iwmvi.petshop.common.controller;

import br.iwmvi.petshop.common.dto.PaginaResponse;
import br.iwmvi.petshop.common.service.CrudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST base para operações CRUD padrão.
 *
 * Fornece endpoints REST para operações Create, Read, Update, Delete:
 * - POST - Criar nova entidade
 * - GET - Listar todas as entidades ativas
 * - GET /{id} - Obter entidade por ID
 * - PUT /{id} - Atualizar entidade
 * - DELETE /{id} - Deletar entidade
 *
 * Subclasses devem sobrescrever {@link #getService()} para fornecer seu serviço específico.
 *
 * @param <ID> O tipo de ID da entidade
 * @param <REQ> O tipo de DTO request
 * @param <RES> O tipo de DTO response
 */
public abstract class CrudController<ID, REQ, RES> {

    /**
     * Obtém o serviço CRUD para este controller.
     *
     * @return a instância do CrudService
     */
    protected abstract CrudService<?, ID, REQ, RES> getService();

    /**
     * Cria uma nova entidade.
     * @param request o DTO request
     * @return o DTO response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RES create(@Valid @RequestBody REQ request) {
        return getService().create(request);
    }

    /**
     * Lista as entidades ativas de forma paginada.
     * @param busca termo de busca opcional
     * @param pagina índice da página (começa em 0)
     * @param tamanho itens por página (padrão 10)
     * @return a página de DTOs response
     */
    @GetMapping
    public PaginaResponse<RES> listAll(@RequestParam(required = false) String busca,
                                       @RequestParam(defaultValue = "0") int pagina,
                                       @RequestParam(defaultValue = "" + PaginaResponse.TAMANHO_PADRAO) int tamanho) {
        return getService().findPage(busca, pagina, tamanho);
    }

    /**
     * Obtém uma entidade pelo ID.
     * @param id o ID da entidade
     * @return o DTO response
     */
    @GetMapping("/{id}")
    public RES findById(@PathVariable ID id) {
        return getService().findById(id);
    }

    /**
     * Atualiza uma entidade existente.
     * @param id o ID da entidade
     * @param request o DTO request com novos valores
     * @return o DTO response atualizado
     */
    @PutMapping("/{id}")
    public RES update(@PathVariable ID id, @Valid @RequestBody REQ request) {
        return getService().update(id, request);
    }

    /**
     * Deleta uma entidade pelo ID.
     * @param id o ID da entidade
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable ID id) {
        getService().delete(id);
    }
}
