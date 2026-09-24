package br.iwmvi.petshop.tutor.repository;

import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.tutor.model.Tutor;

import java.util.Optional;

public interface TutorRepository extends SoftDeleteRepository<Tutor, Long> {

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    /** Busca pelo CPF incluindo tutores excluídos logicamente. */
    Optional<Tutor> findByCpf(String cpf);

}
