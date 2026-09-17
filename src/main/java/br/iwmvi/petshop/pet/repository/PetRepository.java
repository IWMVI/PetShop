package br.iwmvi.petshop.pet.repository;

import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.pet.model.Pet;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends SoftDeleteRepository<Pet, Long> {

    List<Pet> findByTutorIdAndDeletedAtIsNull(Long tutorId);

    Optional<Pet> findByIdAndTutorIdAndDeletedAtIsNull(Long id, Long tutorId);
}
