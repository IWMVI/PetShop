package br.iwmvi.petshop.pet.repository;

import br.iwmvi.petshop.pet.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
}
