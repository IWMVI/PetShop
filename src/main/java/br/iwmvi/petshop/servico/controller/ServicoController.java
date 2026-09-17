package br.iwmvi.petshop.servico.controller;

import br.iwmvi.petshop.servico.dto.request.ServicoRequest;
import br.iwmvi.petshop.servico.dto.response.ServicoResponse;
import br.iwmvi.petshop.servico.service.ServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResponse cadastrar(@Valid @RequestBody ServicoRequest request) {
        return servicoService.cadastrar(request);
    }

    @GetMapping
    public List<ServicoResponse> listar() {
        return servicoService.listar();
    }

    @GetMapping("/{id}")
    public ServicoResponse buscarPorId(@PathVariable Long id) {
        return servicoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ServicoResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody ServicoRequest request) {
        return servicoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        servicoService.deletar(id);
    }
}
