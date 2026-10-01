package br.iwmvi.petshop.tutor.job;

import br.iwmvi.petshop.tutor.service.ExpurgoTutoresService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara o expurgo de tutores excluídos. Fica separado do serviço para que a chamada
 * passe pelo proxy do Spring e o {@code @Transactional} de {@link ExpurgoTutoresService#expurgar()} valha.
 */
@Component
@RequiredArgsConstructor
public class ExpurgoTutoresJob {

    private final ExpurgoTutoresService expurgoTutoresService;

    /** Roda diariamente (padrão: 03:00); o horário é ajustável por {@code petshop.expurgo-tutores.cron}. */
    @Scheduled(cron = "${petshop.expurgo-tutores.cron:0 0 3 * * *}")
    public void executar() {
        expurgoTutoresService.expurgar();
    }
}
