package br.iwmvi.petshop.pet.controller;

import br.iwmvi.petshop.pet.dto.request.PetRequest;
import br.iwmvi.petshop.pet.dto.response.PetResponse;
import br.iwmvi.petshop.pet.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tutores/{tutorId}/pets")
public class PetController {

    private final PetService petService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse cadastrar(@PathVariable Long tutorId,
                                 @Valid @RequestBody PetRequest request) {
        return petService.cadastrar(request, tutorId);
    }

    @GetMapping
    public java.util.List<PetResponse> listar(@PathVariable Long tutorId) {
        return petService.listarPorTutor(tutorId);
    }

    @GetMapping("/{petId}")
    public PetResponse buscarPorId(@PathVariable Long tutorId,
                                   @PathVariable Long petId) {
        return petService.buscarPorId(petId, tutorId);
    }

    @PutMapping("/{petId}")
    public PetResponse atualizar(@PathVariable Long tutorId,
                                 @PathVariable Long petId,
                                 @Valid @RequestBody PetRequest request) {
        return petService.atualizar(tutorId, petId, request);
    }

    @DeleteMapping("/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long tutorId,
                        @PathVariable Long petId) {
        petService.deletar(tutorId, petId);
    }

}
