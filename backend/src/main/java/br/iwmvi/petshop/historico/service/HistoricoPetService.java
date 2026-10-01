package br.iwmvi.petshop.historico.service;

import br.iwmvi.petshop.exception.EntityNotFoundException;
import br.iwmvi.petshop.exception.HistoricoPetNotFoundException;
import br.iwmvi.petshop.exception.PetNotFoundException;
import br.iwmvi.petshop.funcionario.model.Funcionario;
import br.iwmvi.petshop.funcionario.repository.FuncionarioRepository;
import br.iwmvi.petshop.historico.dto.request.HistoricoPetRequest;
import br.iwmvi.petshop.historico.dto.response.HistoricoPetResponse;
import br.iwmvi.petshop.historico.mapper.HistoricoPetMapper;
import br.iwmvi.petshop.historico.repository.HistoricoPetRepository;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.pet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoPetService {

    private final HistoricoPetRepository repository;
    private final PetRepository petRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final HistoricoPetMapper mapper;

    @Transactional
    public HistoricoPetResponse registrar(Long petId, HistoricoPetRequest request) {
        Pet pet = buscarPetAtivo(petId);
        Funcionario funcionario = buscarFuncionarioAtivo(request.funcionarioId());
        var historico = repository.save(mapper.toEntity(request, pet, funcionario));
        return mapper.toResponse(historico);
    }

    @Transactional(readOnly = true)
    public List<HistoricoPetResponse> listarPorPet(Long petId) {
        buscarPetAtivo(petId);
        return mapper.toResponseList(repository.findByPetIdOrderByDataEventoDesc(petId));
    }

    @Transactional(readOnly = true)
    public HistoricoPetResponse buscarPorId(Long petId, Long id) {
        buscarPetAtivo(petId);
        return repository.findByIdAndPetId(id, petId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new HistoricoPetNotFoundException(id));
    }

    private Pet buscarPetAtivo(Long petId) {
        return petRepository.findActiveById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
    }

    /**
     * Ao registrar, o funcionário precisa estar ativo. Eventos antigos continuam exibindo
     * funcionários desligados, pois a exclusão lógica mantém o registro no banco.
     */
    private Funcionario buscarFuncionarioAtivo(Long funcionarioId) {
        if (funcionarioId == null) {
            return null;
        }
        return funcionarioRepository.findActiveById(funcionarioId)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário", funcionarioId));
    }
}
