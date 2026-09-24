package br.iwmvi.petshop.tutor.service;

import br.iwmvi.petshop.common.mapper.ResponseMapper;
import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.common.service.CrudService;
import br.iwmvi.petshop.exception.CpfJaCadastradoException;
import br.iwmvi.petshop.exception.EmailJaCadastradoException;
import br.iwmvi.petshop.exception.TutorNotFoundException;
import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.tutor.dto.response.TutorResponse;
import br.iwmvi.petshop.tutor.mapper.TutorMapper;
import br.iwmvi.petshop.tutor.model.Tutor;
import br.iwmvi.petshop.tutor.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TutorService extends CrudService<Tutor, Long, TutorRequest, TutorResponse> {

    private final TutorRepository repository;
    private final TutorMapper mapper;

    @Override
    protected SoftDeleteRepository<Tutor, Long> getRepository() {
        return repository;
    }

    @Override
    protected ResponseMapper<Tutor, TutorResponse> getMapper() {
        return mapper;
    }

    @Override
    protected Tutor mapToEntity(TutorRequest request) {
        return mapper.toEntity(request);
    }

    @Override
    protected void updateEntity(Tutor entity, TutorRequest request) {
        mapper.atualizarEntidade(entity, request);
    }

    @Override
    protected void validateBeforeSave(Tutor entity) {
        String email = entity.getEmail().toLowerCase();
        entity.setEmail(email);

        boolean emailExists = repository.existsByEmail(email);
        if (emailExists) {
            throw new EmailJaCadastradoException("Email ja cadastrado: " + email);
        }

        if (repository.existsByCpf(entity.getCpf())) {
            throw new CpfJaCadastradoException();
        }
    }

    @Override
    protected void validateUpdate(Tutor entity, TutorRequest request) {
        String novoEmail = request.email().toLowerCase();
        if (!entity.getEmail().equalsIgnoreCase(novoEmail) && repository.existsByEmail(novoEmail)) {
            throw new EmailJaCadastradoException("Email ja cadastrado: " + novoEmail);
        }

        String novoCpf = request.cpf().replaceAll("\\D", "");
        if (!novoCpf.equals(entity.getCpf()) && repository.existsByCpf(novoCpf)) {
            throw new CpfJaCadastradoException();
        }
    }

    @Override
    protected String getEntityName() {
        return "Tutor";
    }

    // Métodos legados para compatibilidade
    public TutorResponse cadastrar(TutorRequest request) {
        return create(request);
    }

    public TutorResponse buscarPorId(Long id) {
        return findById(id);
    }

    public TutorResponse atualizar(Long id, TutorRequest request) {
        return update(id, request);
    }

    public void excluir(Long id) {
        delete(id);
    }

    public TutorResponse restaurar(Long id) {
        Tutor tutor = repository.findById(id)
                .orElseThrow(() -> new TutorNotFoundException(id));
        tutor.setDeletedAt(null);
        repository.save(tutor);
        return mapper.toResponse(tutor);
    }
}
