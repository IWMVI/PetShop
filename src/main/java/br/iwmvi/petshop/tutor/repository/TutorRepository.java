package br.iwmvi.petshop.tutor.repository;

import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.tutor.model.Tutor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TutorRepository extends SoftDeleteRepository<Tutor, Long> {

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    /** Busca pelo CPF incluindo tutores excluídos logicamente. */
    Optional<Tutor> findByCpf(String cpf);

    // ---- Expurgo definitivo de tutores excluídos logicamente ----
    // Nem todas as FKs do esquema têm ON DELETE CASCADE (agendamento_servicos e pagamentos não têm,
    // e o endereço é referenciado pelo tutor, não o contrário). Por isso a remoção é explícita,
    // dos dependentes para o tutor, e deve rodar numa única transação.

    @Query("SELECT t.id FROM Tutor t WHERE t.deletedAt IS NOT NULL AND t.deletedAt < :limite ORDER BY t.id")
    List<Long> findIdsExcluidosAntesDe(@Param("limite") LocalDateTime limite, Pageable pageable);

    @Query(value = "SELECT endereco_id FROM tutores WHERE id IN (:ids)", nativeQuery = true)
    List<Long> findEnderecoIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(value = """
            DELETE FROM agendamento_servicos WHERE agendamento_id IN (
                SELECT a.id FROM agendamentos a JOIN pets p ON p.id = a.pet_id WHERE p.tutor_id IN (:ids))
            """, nativeQuery = true)
    void expurgarServicosDosAgendamentos(@Param("ids") List<Long> tutorIds);

    @Modifying
    @Query(value = """
            DELETE FROM pagamentos WHERE agendamento_id IN (
                SELECT a.id FROM agendamentos a JOIN pets p ON p.id = a.pet_id WHERE p.tutor_id IN (:ids))
            """, nativeQuery = true)
    void expurgarPagamentos(@Param("ids") List<Long> tutorIds);

    @Modifying
    @Query(value = "DELETE FROM agendamentos WHERE pet_id IN (SELECT id FROM pets WHERE tutor_id IN (:ids))",
            nativeQuery = true)
    void expurgarAgendamentos(@Param("ids") List<Long> tutorIds);

    @Modifying
    @Query(value = "DELETE FROM historico_pets WHERE pet_id IN (SELECT id FROM pets WHERE tutor_id IN (:ids))",
            nativeQuery = true)
    void expurgarHistoricoDosPets(@Param("ids") List<Long> tutorIds);

    @Modifying
    @Query(value = "DELETE FROM pets WHERE tutor_id IN (:ids)", nativeQuery = true)
    void expurgarPets(@Param("ids") List<Long> tutorIds);

    @Modifying
    @Query(value = "DELETE FROM tutores WHERE id IN (:ids)", nativeQuery = true)
    void expurgarTutores(@Param("ids") List<Long> tutorIds);

    @Modifying
    @Query(value = "DELETE FROM enderecos WHERE id IN (:ids)", nativeQuery = true)
    void expurgarEnderecos(@Param("ids") List<Long> enderecoIds);
}
