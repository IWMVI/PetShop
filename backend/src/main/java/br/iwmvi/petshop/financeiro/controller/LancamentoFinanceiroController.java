package br.iwmvi.petshop.financeiro.controller;

import br.iwmvi.petshop.common.dto.PaginaResponse;
import br.iwmvi.petshop.financeiro.dto.request.LancamentoFinanceiroRequest;
import br.iwmvi.petshop.financeiro.dto.response.ExtratoResponse;
import br.iwmvi.petshop.financeiro.dto.response.LancamentoFinanceiroResponse;
import br.iwmvi.petshop.financeiro.model.CategoriaLancamento;
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
}
