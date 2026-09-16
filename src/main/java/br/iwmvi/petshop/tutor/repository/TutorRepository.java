package br.iwmvi.petshop.tutor.repository;

import br.iwmvi.petshop.tutor.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    boolean existsByEmail(String email);

    List<Tutor> findAllByDeletedAtIsNull();

    Optional<Tutor> findByIdAndDeletedAtIsNull(Long id);
}
