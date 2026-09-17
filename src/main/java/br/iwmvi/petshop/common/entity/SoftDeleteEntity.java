package br.iwmvi.petshop.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade base para implementação do padrão soft delete.
 *
 * Soft delete é uma estratégia onde registros não são fisicamente deletados do banco de dados,
 * mas marcados como deletados através de um timestamp na coluna {@code deleted_at}.
 *
 * Todas as entidades que herdam desta classe devem ser filtradas por {@code deletedAt IS NULL}
 * em queries para retornar apenas registros ativos.
 *
 * @see br.iwmvi.petshop.common.repository.SoftDeleteRepository
 */
@MappedSuperclass
@Getter
@Setter
public abstract class SoftDeleteEntity {

    /** Timestamp de quando o registro foi deletado (null se ativo) */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Marca este registro como deletado, definindo deletedAt com a data/hora atual.
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Verifica se este registro está ativo (não foi deletado).
     *
     * @return true se o registro está ativo, false caso contrário
     */
    public boolean isActive() {
        return deletedAt == null;
    }
}
