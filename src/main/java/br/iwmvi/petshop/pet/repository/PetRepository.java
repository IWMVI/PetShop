package br.iwmvi.petshop.pet.repository;

import br.iwmvi.petshop.pet.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findAllByDeletedAtIsNull();

    Optional<Pet> findByIdAndDeletedAtIsNull(Long id);

    List<Pet> findByTutorIdAndDeletedAtIsNull(Long tutorId);

    Optional<Pet> findByIdAndTutorIdAndDeletedAtIsNull(Long id, Long tutorId);

    void deleteByIdAndTutorId(Long id, Long tutorId);
}
