package br.iwmvi.petshop.servico.service;

import br.iwmvi.petshop.common.mapper.ResponseMapper;
import br.iwmvi.petshop.common.repository.SoftDeleteRepository;
import br.iwmvi.petshop.common.service.CrudService;
import br.iwmvi.petshop.servico.dto.request.ServicoRequest;
import br.iwmvi.petshop.servico.dto.response.ServicoResponse;
import br.iwmvi.petshop.servico.mapper.ServicoMapper;
import br.iwmvi.petshop.servico.model.Servico;
import br.iwmvi.petshop.servico.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicoService extends CrudService<Servico, Long, ServicoRequest, ServicoResponse> {

    private final ServicoRepository repository;
    private final ServicoMapper mapper;

    @Override
    protected SoftDeleteRepository<Servico, Long> getRepository() {
        return repository;
    }

    @Override
    protected ResponseMapper<Servico, ServicoResponse> getMapper() {
        return mapper;
    }

    @Override
    protected Servico mapToEntity(ServicoRequest request) {
        return mapper.toEntity(request);
    }

    @Override
    protected void updateEntity(Servico entity, ServicoRequest request) {
        entity.atualizar(
                request.nome(),
                request.descricao(),
                request.preco(),
                request.tempoEstimadoMinutos()
        );
    }

    @Override
    protected String getEntityName() {
        return "Serviço";
    }

    // Métodos legados para compatibilidade com controllers existentes
    public ServicoResponse cadastrar(ServicoRequest request) {
        return create(request);
    }

    public ServicoResponse buscarPorId(Long id) {
        return findById(id);
    }

    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        return update(id, request);
    }

    public void deletar(Long id) {
        delete(id);
    }
}
