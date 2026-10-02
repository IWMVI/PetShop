package br.iwmvi.petshop.financeiro.controller;

import br.iwmvi.petshop.common.dto.PaginaResponse;
import br.iwmvi.petshop.financeiro.dto.request.ContaFinanceiraRequest;
import br.iwmvi.petshop.financeiro.dto.request.LancamentoFinanceiroRequest;
import br.iwmvi.petshop.financeiro.dto.request.MarcarComoPagaRequest;
import br.iwmvi.petshop.financeiro.dto.response.ExtratoResponse;
import br.iwmvi.petshop.financeiro.dto.response.LancamentoFinanceiroResponse;
import br.iwmvi.petshop.financeiro.dto.response.SaldoContasResponse;
import br.iwmvi.petshop.financeiro.model.CategoriaLancamento;
import br.iwmvi.petshop.financeiro.model.StatusLancamento;
import br.iwmvi.petshop.financeiro.model.TipoLancamento;
import br.iwmvi.petshop.financeiro.service.LancamentoFinanceiroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/financeiro")
public class LancamentoFinanceiroController {

    private final LancamentoFinanceiroService service;

    @PostMapping("/lancamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoFinanceiroResponse registrar(@Valid @RequestBody LancamentoFinanceiroRequest request) {
        return service.registrar(request);
    }

    @GetMapping("/lancamentos/{id}")
    public LancamentoFinanceiroResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/lancamentos/{id}")
    public LancamentoFinanceiroResponse atualizar(@PathVariable Long id,
                                                  @Valid @RequestBody LancamentoFinanceiroRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/lancamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id) {
        service.cancelar(id);
    }

    @GetMapping("/extrato")
    public ExtratoResponse extrato(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) CategoriaLancamento categoria,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + PaginaResponse.TAMANHO_PADRAO) int tamanho) {
        return service.extrato(inicio, fim, tipo, categoria, pagina, tamanho);
    }

    @PostMapping("/contas")
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoFinanceiroResponse registrarConta(@Valid @RequestBody ContaFinanceiraRequest request) {
        return service.registrarConta(request);
    }

    @GetMapping("/contas")
    public PaginaResponse<LancamentoFinanceiroResponse> listarContas(
            @RequestParam TipoLancamento tipo,
            @RequestParam(required = false) StatusLancamento status,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + PaginaResponse.TAMANHO_PADRAO) int tamanho) {
        return service.listarContas(tipo, status, pagina, tamanho);
    }

    @GetMapping("/contas/saldo")
    public SaldoContasResponse saldoContas(@RequestParam TipoLancamento tipo) {
        return service.saldoContas(tipo);
    }

    @PutMapping("/contas/{id}")
    public LancamentoFinanceiroResponse atualizarConta(@PathVariable Long id,
                                                        @Valid @RequestBody ContaFinanceiraRequest request) {
        return service.atualizarConta(id, request);
    }

    @PutMapping("/contas/{id}/pagar")
    public LancamentoFinanceiroResponse marcarComoPaga(@PathVariable Long id,
                                                        @Valid @RequestBody MarcarComoPagaRequest request) {
        return service.marcarComoPaga(id, request);
    }
}
