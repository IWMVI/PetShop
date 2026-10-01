package br.iwmvi.petshop.servico.repository;

import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.servico.model.Servico;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends SoftDeleteRepository<Servico, Long> {

    List<Servico> findByIdInAndDeletedAtIsNull(List<Long> ids);
}
