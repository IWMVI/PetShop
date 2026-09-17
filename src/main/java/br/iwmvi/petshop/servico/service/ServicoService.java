package br.iwmvi.petshop.servico.service;

import br.iwmvi.petshop.exception.ServicoNotFoundException;
import br.iwmvi.petshop.servico.dto.request.ServicoRequest;
import br.iwmvi.petshop.servico.dto.response.ServicoResponse;
import br.iwmvi.petshop.servico.mapper.ServicoMapper;
import br.iwmvi.petshop.servico.model.Servico;
import br.iwmvi.petshop.servico.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository repository;

    public ServicoResponse cadastrar(ServicoRequest request) {
        Servico servico = ServicoMapper.toEntity(request);
        Servico servicoSalvo = repository.save(servico);
        return ServicoMapper.toResponse(servicoSalvo);
    }

    public List<ServicoResponse> listar() {
        List<ServicoResponse> responses = new ArrayList<>();
        for (Servico servico : repository.findAllByDeletedAtIsNull()) {
            responses.add(ServicoMapper.toResponse(servico));
        }
        return responses;
    }

    public ServicoResponse buscarPorId(Long id) {
        Servico servico = repository.findByIdAndDeletedAtIsNull(id).orElseThrow(() ->
                new ServicoNotFoundException(id));
        return ServicoMapper.toResponse(servico);
    }

    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        Servico servico = repository.findByIdAndDeletedAtIsNull(id).orElseThrow(() ->
                new ServicoNotFoundException(id));

        servico.atualizar(request.nome(), request.descricao(), request.preco(), request.tempoEstimadoMinutos());

        Servico servicoAtualizado = repository.save(servico);

        return ServicoMapper.toResponse(servicoAtualizado);
    }

    public void deletar(Long id) {
        Servico servico = repository.findByIdAndDeletedAtIsNull(id).orElseThrow(() ->
                new ServicoNotFoundException(id));

        servico.deletar();
        repository.save(servico);
    }
}
