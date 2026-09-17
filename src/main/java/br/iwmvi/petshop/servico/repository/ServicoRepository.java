package br.iwmvi.petshop.servico.repository;

import br.iwmvi.petshop.servico.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findAllByDeletedAtIsNull();

    Optional<Servico> findByIdAndDeletedAtIsNull(Long id);

    List<Servico> findByIdInAndDeletedAtIsNull(List<Long> ids);
}
