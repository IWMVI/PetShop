package br.iwmvi.petshop.pet.service;

import br.iwmvi.petshop.exception.PetNotFoundException;
import br.iwmvi.petshop.exception.TutorNotFoundException;
import br.iwmvi.petshop.pet.dto.request.PetRequest;
import br.iwmvi.petshop.pet.dto.response.PetResponse;
import br.iwmvi.petshop.pet.mapper.PetMapper;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.pet.repository.PetRepository;
import br.iwmvi.petshop.tutor.model.Tutor;
import br.iwmvi.petshop.tutor.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository repository;
    private final TutorRepository tutorRepository;

    public PetResponse cadastrar(PetRequest request, Long tutorId) {
        Tutor tutor = tutorRepository.findByIdAndDeletedAtIsNull(tutorId).orElseThrow(() -> new TutorNotFoundException(tutorId));
        Pet pet = PetMapper.toEntity(request, tutor);

        Pet petSalvo = repository.save(pet);

        return PetMapper.toResponse(petSalvo);
    }

    public List<PetResponse> listarPorTutor(Long tutorId) {
        tutorRepository.findByIdAndDeletedAtIsNull(tutorId).orElseThrow(() -> new TutorNotFoundException(tutorId));

        List<PetResponse> responses = new ArrayList<>();
        for (Pet pet : repository.findByTutorIdAndDeletedAtIsNull(tutorId)) {
            responses.add(PetMapper.toResponse(pet));
        }

        return responses;
    }

    public PetResponse buscarPorId(Long petId, Long tutorId) {
        Pet pet = repository.findByIdAndTutorIdAndDeletedAtIsNull(petId, tutorId).orElseThrow(() -> new PetNotFoundException(petId));

        return PetMapper.toResponse(pet);
    }

    public PetResponse atualizar(Long tutorId, Long petId, PetRequest request) {
        Pet pet = repository.findByIdAndTutorIdAndDeletedAtIsNull(petId, tutorId).orElseThrow(() ->
                new PetNotFoundException(petId));

        pet.atualizar(request.nome(), request.especie(), request.peso(), request.raca(), request.idade());

        Pet petAtualizado = repository.save(pet);

        return PetMapper.toResponse(petAtualizado);
    }

    public void deletar(Long tutorId, Long petId) {
        Pet pet = repository.findByIdAndTutorIdAndDeletedAtIsNull(petId, tutorId).orElseThrow(() ->
                new PetNotFoundException(petId));

        pet.deletar();
        repository.save(pet);
    }
}
