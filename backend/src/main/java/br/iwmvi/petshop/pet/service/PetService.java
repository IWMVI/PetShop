package br.iwmvi.petshop.pet.service;

import br.iwmvi.petshop.common.mapper.ResponseMapper;
import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.common.service.CrudService;
import br.iwmvi.petshop.exception.PetNotFoundException;
import br.iwmvi.petshop.exception.TutorNotFoundException;
import br.iwmvi.petshop.pet.dto.request.PetRequest;
import br.iwmvi.petshop.pet.dto.response.PetResponse;
import br.iwmvi.petshop.pet.mapper.PetMapper;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.pet.repository.PetRepository;
import br.iwmvi.petshop.tutor.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService extends CrudService<Pet, Long, PetRequest, PetResponse> {

    private final PetRepository repository;
    private final TutorRepository tutorRepository;
    private final PetMapper mapper;

    @Override
    protected SoftDeleteRepository<Pet, Long> getRepository() {
        return repository;
    }

    @Override
    protected ResponseMapper<Pet, PetResponse> getMapper() {
        return mapper;
    }

    @Override
    protected Pet mapToEntity(PetRequest request) {
        Long tutorId = extractTutorIdFromContext();
        var tutor = tutorRepository.findActiveById(tutorId)
                .orElseThrow(() -> new TutorNotFoundException(tutorId));
        return mapper.toEntity(request, tutor);
    }

    @Override
    protected void updateEntity(Pet entity, PetRequest request) {
        entity.atualizar(
                request.nome(),
                request.especie(),
                request.peso(),
                request.raca(),
                request.idade()
        );
    }

    @Override
    protected String getEntityName() {
        return "Pet";
    }

    // Métodos de contexto e compatibilidade
    private static final ThreadLocal<Long> tutorIdContext = new ThreadLocal<>();

    public void setTutorContext(Long tutorId) {
        tutorIdContext.set(tutorId);
    }

    private Long extractTutorIdFromContext() {
        Long tutorId = tutorIdContext.get();
        tutorIdContext.remove();
        return tutorId;
    }

    // Métodos legados para compatibilidade
    public PetResponse cadastrar(PetRequest request, Long tutorId) {
        setTutorContext(tutorId);
        return create(request);
    }

    @Transactional(readOnly = true)
    public List<PetResponse> listarPorTutor(Long tutorId) {
        tutorRepository.findActiveById(tutorId)
                .orElseThrow(() -> new TutorNotFoundException(tutorId));
        return repository.findByTutorIdAndDeletedAtIsNull(tutorId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PetResponse buscarPorId(Long petId, Long tutorId) {
        return repository.findByIdAndTutorIdAndDeletedAtIsNull(petId, tutorId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new PetNotFoundException(petId));
    }

    public PetResponse atualizar(Long tutorId, Long petId, PetRequest request) {
        Pet pet = repository.findByIdAndTutorIdAndDeletedAtIsNull(petId, tutorId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        updateEntity(pet, request);
        Pet updated = repository.save(pet);
        return mapper.toResponse(updated);
    }

    public void deletar(Long tutorId, Long petId) {
        repository.findByIdAndTutorIdAndDeletedAtIsNull(petId, tutorId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        repository.softDelete(petId);
    }
}
