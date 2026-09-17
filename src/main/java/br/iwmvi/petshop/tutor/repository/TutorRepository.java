package br.iwmvi.petshop.tutor.repository;

import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.tutor.model.Tutor;

public interface TutorRepository extends SoftDeleteRepository<Tutor, Long> {

    boolean existsByEmail(String email);
}
