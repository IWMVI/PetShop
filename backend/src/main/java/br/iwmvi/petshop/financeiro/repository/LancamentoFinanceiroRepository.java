package br.iwmvi.petshop.financeiro.repository;

import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.financeiro.model.CategoriaLancamento;
import br.iwmvi.petshop.financeiro.model.LancamentoFinanceiro;
import br.iwmvi.petshop.financeiro.model.TipoLancamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LancamentoFinanceiroRepository extends SoftDeleteRepository<LancamentoFinanceiro, Long> {

    Optional<LancamentoFinanceiro> findByPagamentoIdAndDeletedAtIsNull(Long pagamentoId);

    @Query("""
            SELECT l FROM LancamentoFinanceiro l
            WHERE l.deletedAt IS NULL
              AND l.data BETWEEN :inicio AND :fim
              AND (:tipo IS NULL OR l.tipo = :tipo)
              AND (:categoria IS NULL OR l.categoria = :categoria)
            """)
    Page<LancamentoFinanceiro> buscarExtrato(@Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim,
                                             @Param("tipo") TipoLancamento tipo,
                                             @Param("categoria") CategoriaLancamento categoria,
                                             Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFinanceiro l
            WHERE l.deletedAt IS NULL
              AND l.tipo = :tipo
              AND l.data BETWEEN :inicio AND :fim
              AND (:categoria IS NULL OR l.categoria = :categoria)
            """)
    BigDecimal somarPorTipo(@Param("tipo") TipoLancamento tipo,
                            @Param("inicio") LocalDateTime inicio,
                            @Param("fim") LocalDateTime fim,
                            @Param("categoria") CategoriaLancamento categoria);
}
