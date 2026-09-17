package br.iwmvi.petshop.tutor.controller;

import br.iwmvi.petshop.common.controller.CrudController;
import br.iwmvi.petshop.common.service.CrudService;
import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.tutor.dto.response.TutorResponse;
import br.iwmvi.petshop.tutor.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para operações CRUD de Tutor.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/tutores")
public class TutorController extends CrudController<Long, TutorRequest, TutorResponse> {

    private final TutorService tutorService;

    @Override
    protected CrudService<?, Long, TutorRequest, TutorResponse> getService() {
        return tutorService;
    }

    @PostMapping("/{id}/restaurar")
    public TutorResponse restaurar(@PathVariable Long id) {
        return tutorService.restaurar(id);
    }
}
