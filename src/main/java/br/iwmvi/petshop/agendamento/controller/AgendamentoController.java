package br.iwmvi.petshop.agendamento.controller;

import br.iwmvi.petshop.agendamento.dto.request.AgendamentoRequest;
import br.iwmvi.petshop.agendamento.dto.response.AgendamentoResponse;
import br.iwmvi.petshop.agendamento.service.AgendamentoService;
import br.iwmvi.petshop.common.dto.PaginaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/pets/{petId}/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResponse agendar(@PathVariable Long petId,
                                       @Valid @RequestBody AgendamentoRequest request) {
        return agendamentoService.cadastrar(petId, request);
    }

    @GetMapping
    public PaginaResponse<AgendamentoResponse> listar(@PathVariable Long petId,
                                                      @RequestParam(defaultValue = "0") int pagina,
                                                      @RequestParam(defaultValue = "" + PaginaResponse.TAMANHO_PADRAO) int tamanho) {
        return agendamentoService.listarPorPet(petId, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public AgendamentoResponse buscarPorId(@PathVariable Long petId,
                                           @PathVariable Long id) {
        return agendamentoService.buscarPorId(petId, id);
    }

    @PutMapping("/{id}")
    public AgendamentoResponse reagendar(@PathVariable Long petId,
                                         @PathVariable Long id,
                                         @Valid @RequestBody AgendamentoRequest request) {
        return agendamentoService.atualizar(petId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long petId,
                         @PathVariable Long id) {
        agendamentoService.cancelar(petId, id);
    }
}
