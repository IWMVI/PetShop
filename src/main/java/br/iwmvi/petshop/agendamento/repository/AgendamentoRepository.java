package br.iwmvi.petshop.agendamento.repository;

import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends SoftDeleteRepository<Agendamento, Long> {

    /**
     * Página de ids dos agendamentos ativos do pet. A paginação é feita só sobre os ids
     * para não combinar LIMIT com JOIN FETCH de coleção (que o Hibernate faria em memória).
     */
    @Query(value = """
            SELECT a.id
            FROM Agendamento a
            WHERE a.pet.id = :petId
              AND a.deletedAt IS NULL
            """,
            countQuery = """
            SELECT COUNT(a)
            FROM Agendamento a
            WHERE a.pet.id = :petId
              AND a.deletedAt IS NULL
            """)
    Page<Long> findIdsByPetId(@Param("petId") Long petId, Pageable pageable);

    /** Carrega os agendamentos (com serviços) de uma página de ids. */
    @Query("""
            SELECT DISTINCT a
            FROM Agendamento a
            LEFT JOIN FETCH a.agendamentoServicos agendamentoServico
            LEFT JOIN FETCH agendamentoServico.servico
            WHERE a.id IN :ids
            """)
    List<Agendamento> findAllComServicosByIdIn(@Param("ids") List<Long> ids);

    @Query("""
            SELECT DISTINCT a
            FROM Agendamento a
            LEFT JOIN FETCH a.agendamentoServicos agendamentoServico
            LEFT JOIN FETCH agendamentoServico.servico
            WHERE a.id = :id
              AND a.pet.id = :petId
              AND a.deletedAt IS NULL
            """)
    Optional<Agendamento> findByIdAndPetIdAndDeletedAtIsNull(@Param("id") Long id,
                                                              @Param("petId") Long petId);
}
