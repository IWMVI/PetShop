package br.iwmvi.petshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/**
 * Habilita as tarefas agendadas ({@code @Scheduled}) e expõe o relógio da aplicação,
 * substituível nos testes para controlar "agora".
 */
@Configuration
@EnableScheduling
public class AgendamentoTarefasConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
