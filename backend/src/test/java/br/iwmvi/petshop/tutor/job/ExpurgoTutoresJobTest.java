package br.iwmvi.petshop.tutor.job;

import br.iwmvi.petshop.tutor.service.ExpurgoTutoresService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExpurgoTutoresJobTest {

    @Test
    @DisplayName("PCE - Deve delegar a execução agendada ao serviço de expurgo.")
    void deveDelegarAoServico() {
        var service = mock(ExpurgoTutoresService.class);

        new ExpurgoTutoresJob(service).executar();

        verify(service).expurgar();
    }

    @Test
    @DisplayName("PCE - Deve rodar diariamente por padrão, com horário configurável.")
    void deveEstarAgendadoDiariamente() throws NoSuchMethodException {
        var agendamento = ExpurgoTutoresJob.class.getMethod("executar").getAnnotation(Scheduled.class);

        assertThat(agendamento.cron()).isEqualTo("${petshop.expurgo-tutores.cron:0 0 3 * * *}");
    }
}
