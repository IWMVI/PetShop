package br.iwmvi.petshop.historico.repository;

import br.iwmvi.petshop.historico.model.HistoricoPet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;
import java.util.Optional;

/**
 * Repositório do histórico. Declara apenas inserção e leitura: por ser imutável,
 * o histórico não expõe os métodos de alteração/exclusão que viriam do JpaRepository.
 */
@RepositoryDefinition(domainClass = HistoricoPet.class, idClass = Long.class)
public interface HistoricoPetRepository {

    HistoricoPet save(HistoricoPet historico);

    @EntityGraph(attributePaths = "funcionario")
    List<HistoricoPet> findByPetIdOrderByDataEventoDesc(Long petId);

    @EntityGraph(attributePaths = "funcionario")
    Optional<HistoricoPet> findByIdAndPetId(Long id, Long petId);
}
