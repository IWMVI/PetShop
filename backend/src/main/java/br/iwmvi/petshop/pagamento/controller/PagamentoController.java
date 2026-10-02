package br.iwmvi.petshop.pagamento.controller;

import br.iwmvi.petshop.pagamento.dto.request.AtualizarStatusPagamentoRequest;
import br.iwmvi.petshop.pagamento.dto.request.PagamentoRequest;
import br.iwmvi.petshop.pagamento.dto.response.PagamentoResponse;
import br.iwmvi.petshop.pagamento.service.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping("/agendamentos/{agendamentoId}/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public PagamentoResponse registrar(@PathVariable Long agendamentoId,
                                       @Valid @RequestBody PagamentoRequest request) {
        return pagamentoService.registrar(agendamentoId, request);
    }

    @GetMapping("/agendamentos/{agendamentoId}/pagamentos")
    public List<PagamentoResponse> listar(@PathVariable Long agendamentoId) {
        return pagamentoService.listarPorAgendamento(agendamentoId);
    }

    @GetMapping("/pagamentos/{id}")
    public PagamentoResponse buscarPorId(@PathVariable Long id) {
        return pagamentoService.buscarPorId(id);
    }

    @PutMapping("/pagamentos/{id}")
    public PagamentoResponse atualizarStatus(@PathVariable Long id,
                                             @Valid @RequestBody AtualizarStatusPagamentoRequest request) {
        return pagamentoService.atualizarStatus(id, request);
    }

    @DeleteMapping("/pagamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id) {
        pagamentoService.cancelar(id);
    }
}
