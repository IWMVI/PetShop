package br.iwmvi.petshop.funcionario.service;

import br.iwmvi.petshop.common.mapper.ResponseMapper;
import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.common.service.CrudService;
import br.iwmvi.petshop.exception.CpfJaCadastradoException;
import br.iwmvi.petshop.funcionario.dto.request.FuncionarioRequest;
import br.iwmvi.petshop.funcionario.dto.response.FuncionarioResponse;
import br.iwmvi.petshop.funcionario.mapper.FuncionarioMapper;
import br.iwmvi.petshop.funcionario.model.Funcionario;
import br.iwmvi.petshop.funcionario.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FuncionarioService extends CrudService<Funcionario, Long, FuncionarioRequest, FuncionarioResponse> {

    private final FuncionarioRepository repository;
    private final FuncionarioMapper mapper;

    @Override
    protected SoftDeleteRepository<Funcionario, Long> getRepository() {
        return repository;
    }

    @Override
    protected ResponseMapper<Funcionario, FuncionarioResponse> getMapper() {
        return mapper;
    }

    @Override
    protected Funcionario mapToEntity(FuncionarioRequest request) {
        return mapper.toEntity(request);
    }

    @Override
    protected void updateEntity(Funcionario entity, FuncionarioRequest request) {
        mapper.atualizarEntidade(entity, request);
    }

    @Override
    protected void validateBeforeSave(Funcionario entity) {
        if (repository.existsByCpf(entity.getCpf())) {
            throw new CpfJaCadastradoException();
        }
    }

    /** Só valida o CPF quando ele muda, para não acusar o próprio funcionário como duplicado. */
    @Override
    protected void validateUpdate(Funcionario entity, FuncionarioRequest request) {
        String novoCpf = request.cpf().replaceAll("\\D", "");
        if (!novoCpf.equals(entity.getCpf()) && repository.existsByCpf(novoCpf)) {
            throw new CpfJaCadastradoException();
        }
    }

    @Override
    protected Sort getOrdenacaoPadrao() {
        return Sort.by("nome").and(Sort.by("id"));
    }

    /** Busca pelo nome do funcionário. */
    @Override
    protected Specification<Funcionario> buscaPor(String termo) {
        String padrao = "%" + termo.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nome")), padrao);
    }

    @Override
    protected String getEntityName() {
        return "Funcionário";
    }
}
