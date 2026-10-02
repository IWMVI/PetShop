package br.iwmvi.petshop.pagamento.repository;

import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.pagamento.model.Pagamento;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoRepository extends SoftDeleteRepository<Pagamento, Long> {

    List<Pagamento> findByAgendamentoIdAndDeletedAtIsNull(Long agendamentoId);
}
