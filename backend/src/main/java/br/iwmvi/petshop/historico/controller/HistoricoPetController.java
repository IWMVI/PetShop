package br.iwmvi.petshop.historico.controller;

import br.iwmvi.petshop.historico.dto.request.HistoricoPetRequest;
import br.iwmvi.petshop.historico.dto.response.HistoricoPetResponse;
import br.iwmvi.petshop.historico.service.HistoricoPetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Histórico de eventos do pet. O registro é imutável: não há endpoints de alteração ou exclusão.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pets/{petId}/historico")
public class HistoricoPetController {

    private final HistoricoPetService historicoPetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HistoricoPetResponse registrar(@PathVariable Long petId,
                                          @Valid @RequestBody HistoricoPetRequest request) {
        return historicoPetService.registrar(petId, request);
    }

    @GetMapping
    public List<HistoricoPetResponse> listar(@PathVariable Long petId) {
        return historicoPetService.listarPorPet(petId);
    }

    @GetMapping("/{id}")
    public HistoricoPetResponse buscarPorId(@PathVariable Long petId,
                                            @PathVariable Long id) {
        return historicoPetService.buscarPorId(petId, id);
    }
}
