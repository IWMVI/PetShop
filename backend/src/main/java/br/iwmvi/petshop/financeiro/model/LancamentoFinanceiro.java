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

    @Column(name = "data")
    private LocalDateTime dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLancamento status;

    @Column(name = "data_vencimento")
    private LocalDateTime dataVencimento;

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
                                 BigDecimal valor, LocalDateTime dataPagamento) {
        this.tipo = tipo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.status = StatusLancamento.PAGO;
        this.dataVencimento = null;
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

    /** Cria uma conta a pagar/receber pendente, com vencimento futuro e ainda sem pagamento. */
    public static LancamentoFinanceiro novaConta(TipoLancamento tipo, CategoriaLancamento categoria, String descricao,
                                                  BigDecimal valor, LocalDateTime dataVencimento) {
        LancamentoFinanceiro conta = new LancamentoFinanceiro();
        conta.tipo = tipo;
        conta.categoria = categoria;
        conta.descricao = descricao;
        conta.valor = valor;
        conta.status = StatusLancamento.PENDENTE;
        conta.dataPagamento = null;
        conta.dataVencimento = dataVencimento;
        return conta;
    }

    /** Marca uma conta pendente como paga/recebida, preservando a data de vencimento original. */
    public void marcarComoPaga(LocalDateTime dataPagamento) {
        this.status = StatusLancamento.PAGO;
        this.dataPagamento = dataPagamento;
    }

    /** Cancela o lançamento (realizado ou pendente): status + soft delete, mesmo padrão de Agendamento/Pagamento. */
    public void cancelar() {
        this.status = StatusLancamento.CANCELADO;
        delete();
    }

    /** Edita um lançamento já realizado (Entrada/Saída do Extrato). Não mexe em status nem vencimento. */
    public void atualizarLancamento(TipoLancamento tipo, CategoriaLancamento categoria, String descricao,
                                     BigDecimal valor, LocalDateTime dataPagamento) {
        this.tipo = tipo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
    }

    /** Edita uma conta a pagar/receber ainda pendente. Não mexe em status nem data de pagamento. */
    public void atualizarConta(TipoLancamento tipo, CategoriaLancamento categoria, String descricao,
                                BigDecimal valor, LocalDateTime dataVencimento) {
        this.tipo = tipo;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
    }
}
