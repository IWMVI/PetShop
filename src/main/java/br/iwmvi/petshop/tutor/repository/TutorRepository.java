package br.iwmvi.petshop.tutor.repository;

import br.iwmvi.petshop.tutor.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    boolean existsByEmail(String email);
}
