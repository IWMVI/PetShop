package br.iwmvi.petshop.pet.service;

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

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository repository;
    private final TutorRepository tutorRepository;

    public PetResponse cadastrar(PetRequest request, Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId).orElseThrow(() -> new TutorNotFoundException(tutorId));
        Pet pet = PetMapper.toEntity(request, tutor);

        Pet petSalvo = repository.save(pet);

        return PetMapper.toResponse(petSalvo);
    }
}
