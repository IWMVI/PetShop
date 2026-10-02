package br.iwmvi.petshop.financeiro.model;

import br.iwmvi.petshop.common.entity.SoftDeleteEntity;
import br.iwmvi.petshop.pagamento.model.Pagamento;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "lancamentos_financeiros")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LancamentoFinanceiro extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoLancamento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaLancamento categoria;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime data;

    /**
     * Id do pagamento que originou este lançamento, quando gerado
     * automaticamente a partir de um {@link Pagamento} marcado como PAGO.
     * Nulo para lançamentos manuais. Não é mapeado como {@code @ManyToOne}
     * porque o lançamento já guarda os dados relevantes (valor, data) e não
     * precisa navegar até o pagamento.
     */
    @Column(name = "pagamento_id")
    private Long pagamentoId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public LancamentoFinanceiro(TipoLancamento tipo, CategoriaLancamento categoria, String descricao,
                                 BigDecimal valor, LocalDateTime data) {
        this.tipo = tipo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
    }

    /** Entrada gerada automaticamente quando um pagamento de agendamento é marcado como PAGO. */
    public static LancamentoFinanceiro deEntradaPagamento(Pagamento pagamento) {
        LancamentoFinanceiro lancamento = new LancamentoFinanceiro(
                TipoLancamento.ENTRADA,
                CategoriaLancamento.PAGAMENTO_SERVICO,
                "Pagamento do agendamento #" + pagamento.getAgendamento().getId(),
                pagamento.getValor(),
                pagamento.getDataPagamento()
        );
        lancamento.pagamentoId = pagamento.getId();
        return lancamento;
    }

    public void atualizar(TipoLancamento tipo, CategoriaLancamento categoria, String descricao,
                          BigDecimal valor, LocalDateTime data) {
        this.tipo = tipo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
    }
}
