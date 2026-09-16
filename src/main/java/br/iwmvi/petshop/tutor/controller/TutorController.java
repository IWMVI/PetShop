package br.iwmvi.petshop.tutor.controller;

import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.tutor.dto.response.TutorResponse;
import br.iwmvi.petshop.tutor.service.TutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tutores")
public class TutorController {

    private final TutorService tutorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TutorResponse cadastrar(@Valid @RequestBody TutorRequest request) {
        return tutorService.cadastrar(request);
    }

    @GetMapping
    public List<TutorResponse> listar() {
        return tutorService.listar();
    }

    @GetMapping("/{id}")
    public TutorResponse buscarPorId(@PathVariable Long id) {
        return tutorService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public TutorResponse atualizar(@PathVariable Long id,
                                   @Valid @RequestBody TutorRequest request) {
        return tutorService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        tutorService.excluir(id);
    }

    @PostMapping("/{id}/restaurar")
    public TutorResponse restaurar(@PathVariable Long id) {
        return tutorService.restaurar(id);
    }
}
