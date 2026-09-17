package br.iwmvi.petshop.agendamento.repository;

import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends SoftDeleteRepository<Agendamento, Long> {

    @Query("""
            SELECT DISTINCT a
            FROM Agendamento a
            LEFT JOIN FETCH a.agendamentoServicos agendamentoServico
            LEFT JOIN FETCH agendamentoServico.servico
            WHERE a.pet.id = :petId
              AND a.deletedAt IS NULL
            ORDER BY a.dataHora
            """)
    List<Agendamento> findByPetIdAndDeletedAtIsNull(@Param("petId") Long petId);

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
