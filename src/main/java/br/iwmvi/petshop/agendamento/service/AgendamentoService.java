package br.iwmvi.petshop.agendamento.service;

import br.iwmvi.petshop.agendamento.dto.request.AgendamentoRequest;
import br.iwmvi.petshop.agendamento.dto.response.AgendamentoResponse;
import br.iwmvi.petshop.agendamento.mapper.AgendamentoMapper;
import br.iwmvi.petshop.agendamento.model.Agendamento;
import br.iwmvi.petshop.agendamento.repository.AgendamentoRepository;
import br.iwmvi.petshop.exception.AgendamentoNotFoundException;
import br.iwmvi.petshop.exception.AgendamentoValidationException;
import br.iwmvi.petshop.exception.PetNotFoundException;
import br.iwmvi.petshop.pet.model.Pet;
import br.iwmvi.petshop.pet.repository.PetRepository;
import br.iwmvi.petshop.servico.model.Servico;
import br.iwmvi.petshop.servico.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PetRepository petRepository;
    private final ServicoRepository servicoRepository;

    public AgendamentoResponse cadastrar(Long petId, AgendamentoRequest request) {
        validarDataHora(request.dataHora());

        Pet pet = buscarPetAtivo(petId);
        List<Servico> servicos = buscarServicosAtivos(request.servicoIds());

        Agendamento agendamentoSalvo = agendamentoRepository.save(AgendamentoMapper.toEntity(request, pet, servicos));

        return AgendamentoMapper.toResponse(agendamentoSalvo);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listarPorPet(Long petId) {
        buscarPetAtivo(petId);

        return agendamentoRepository.findByPetIdAndDeletedAtIsNull(petId)
                .stream()
                .map(AgendamentoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse buscarPorId(Long petId, Long agendamentoId) {
        buscarPetAtivo(petId);

        Agendamento agendamento = buscarAgendamentoAtivo(petId, agendamentoId);

        return AgendamentoMapper.toResponse(agendamento);
    }

    public AgendamentoResponse atualizar(Long petId, Long agendamentoId, AgendamentoRequest request) {
        validarDataHora(request.dataHora());
        buscarPetAtivo(petId);

        Agendamento agendamento = buscarAgendamentoAtivo(petId, agendamentoId);
        List<Servico> servicos = buscarServicosAtivos(request.servicoIds());

        BigDecimal valorTotal = servicos.stream()
                .map(Servico::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        agendamento.atualizar(request.dataHora(), request.observacoes(), valorTotal);
        agendamento.definirServicos(servicos);

        return AgendamentoMapper.toResponse(agendamentoRepository.save(agendamento));
    }

    public void cancelar(Long petId, Long agendamentoId) {
        buscarPetAtivo(petId);

        Agendamento agendamento = buscarAgendamentoAtivo(petId, agendamentoId);
        agendamento.cancelar();
        agendamentoRepository.save(agendamento);
    }

    private Pet buscarPetAtivo(Long petId) {
        return petRepository.findByIdAndDeletedAtIsNull(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
    }

    private Agendamento buscarAgendamentoAtivo(Long petId, Long agendamentoId) {
        return agendamentoRepository.findByIdAndPetIdAndDeletedAtIsNull(agendamentoId, petId)
                .orElseThrow(() -> new AgendamentoNotFoundException(agendamentoId));
    }

    private void validarDataHora(LocalDateTime dataHora) {
        if (dataHora != null && dataHora.isBefore(LocalDateTime.now())) {
            throw new AgendamentoValidationException("Data/hora do agendamento não pode ser no passado.");
        }
    }

    private List<Servico> buscarServicosAtivos(List<Long> servicoIds) {
        if (servicoIds == null || servicoIds.isEmpty()) {
            throw new AgendamentoValidationException("Pelo menos um serviço deve ser informado.");
        }

        LinkedHashSet<Long> idsUnicos = new LinkedHashSet<>(servicoIds);
        List<Servico> servicos = servicoRepository.findByIdInAndDeletedAtIsNull(idsUnicos.stream().toList());

        Map<Long, Servico> servicosPorId = servicos.stream()
                .collect(Collectors.toMap(Servico::getId, Function.identity()));

        if (servicosPorId.size() != idsUnicos.size()) {
            throw new AgendamentoValidationException("Um ou mais serviços informados não existem.");
        }

        return idsUnicos.stream().map(servicosPorId::get).toList();
    }
}
