package br.iwmvi.petshop.agendamento.controller;

import br.iwmvi.petshop.agendamento.dto.request.AgendamentoRequest;
import br.iwmvi.petshop.agendamento.dto.response.AgendamentoResponse;
import br.iwmvi.petshop.agendamento.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<AgendamentoResponse> listar(@PathVariable Long petId) {
        return agendamentoService.listarPorPet(petId);
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
