package br.iwmvi.petshop.funcionario.repository;

import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.funcionario.model.Funcionario;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncionarioRepository extends SoftDeleteRepository<Funcionario, Long> {

    boolean existsByCpf(String cpf);
}
