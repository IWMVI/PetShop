package br.iwmvi.petshop.tutor.controller;

import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.tutor.dto.response.TutorResponse;
import br.iwmvi.petshop.tutor.service.TutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}
