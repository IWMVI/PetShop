package br.iwmvi.petshop.tutor.service;

import br.iwmvi.petshop.tutor.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Exclui definitivamente os tutores que estão excluídos logicamente há mais tempo que o
 * período de retenção (30 dias por padrão), junto com tudo que depende deles: pets,
 * histórico dos pets, agendamentos (e seus serviços e pagamentos) e o endereço.
 *
 * <p>Enquanto estiver dentro do período, o tutor pode ser recuperado pelo CPF
 * ({@code POST /tutores/{id}/restaurar}). Depois do expurgo, não há recuperação.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpurgoTutoresService {

    /** Quantidade de tutores removidos por lote, para limitar o tamanho das cláusulas IN. */
    static final int TAMANHO_LOTE = 500;

    private final TutorRepository repository;
    private final Clock clock;

    @Value("${petshop.expurgo-tutores.dias-retencao:30}")
    private int diasRetencao;

    /**
     * Remove definitivamente os tutores excluídos antes do limite de retenção.
     *
     * @return quantidade de tutores removidos
     */
    @Transactional
    public int expurgar() {
        LocalDateTime limite = LocalDateTime.now(clock).minusDays(diasRetencao);
        int total = 0;
        List<Long> lote;
        // Sempre a primeira página: cada lote removido deixa de aparecer na consulta seguinte.
        while (!(lote = repository.findIdsExcluidosAntesDe(limite, PageRequest.of(0, TAMANHO_LOTE))).isEmpty()) {
            expurgarLote(lote);
            total += lote.size();
        }
        if (total > 0) {
            log.info("Expurgo de tutores: {} tutor(es) excluído(s) há mais de {} dias removido(s) definitivamente.",
                    total, diasRetencao);
        }
        return total;
    }

    private void expurgarLote(List<Long> tutorIds) {
        List<Long> enderecoIds = repository.findEnderecoIds(tutorIds);

        repository.expurgarServicosDosAgendamentos(tutorIds);
        repository.expurgarPagamentos(tutorIds);
        repository.expurgarAgendamentos(tutorIds);
        repository.expurgarHistoricoDosPets(tutorIds);
        repository.expurgarPets(tutorIds);
        repository.expurgarTutores(tutorIds);
        if (!enderecoIds.isEmpty()) {
            repository.expurgarEnderecos(enderecoIds);
        }
    }
}
