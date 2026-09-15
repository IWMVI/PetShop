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

}
