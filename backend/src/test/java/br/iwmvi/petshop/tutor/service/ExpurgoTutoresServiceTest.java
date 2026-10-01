package br.iwmvi.petshop.tutor.service;

import br.iwmvi.petshop.tutor.repository.TutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpurgoTutoresServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 23, 12, 0);

    @Mock
    private TutorRepository repository;

    private ExpurgoTutoresService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA.atZone(ZONA).toInstant(), ZONA);
        service = new ExpurgoTutoresService(repository, clock);
        ReflectionTestUtils.setField(service, "diasRetencao", 30);
    }

    @Test
    @DisplayName("PCE - Deve considerar apenas tutores excluídos há mais de 30 dias.")
    void deveUsarLimiteDe30Dias() {
        when(repository.findIdsExcluidosAntesDe(any(), any())).thenReturn(List.of());

        service.expurgar();

        var limite = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).findIdsExcluidosAntesDe(limite.capture(), any(Pageable.class));
        assertThat(limite.getValue()).isEqualTo(LocalDateTime.of(2026, 8, 24, 12, 0));
    }

    @Test
    @DisplayName("PCE - Não deve remover nada quando não houver tutores expirados.")
    void naoDeveRemoverNada_quandoNaoHouverExpirados() {
        when(repository.findIdsExcluidosAntesDe(any(), any())).thenReturn(List.of());

        assertThat(service.expurgar()).isZero();

        verify(repository, never()).expurgarTutores(any());
        verify(repository, never()).expurgarEnderecos(any());
    }

    @Test
    @DisplayName("PCE - Deve remover dependentes antes do tutor e o endereço por último.")
    void deveRemoverNaOrdemDasChavesEstrangeiras() {
        var ids = List.of(1L, 2L);
        when(repository.findIdsExcluidosAntesDe(any(), any())).thenReturn(ids, List.of());
        when(repository.findEnderecoIds(ids)).thenReturn(List.of(10L, 20L));

        assertThat(service.expurgar()).isEqualTo(2);

        InOrder ordem = inOrder(repository);
        ordem.verify(repository).findEnderecoIds(ids);
        ordem.verify(repository).expurgarServicosDosAgendamentos(ids);
        ordem.verify(repository).expurgarPagamentos(ids);
        ordem.verify(repository).expurgarAgendamentos(ids);
        ordem.verify(repository).expurgarHistoricoDosPets(ids);
        ordem.verify(repository).expurgarPets(ids);
        ordem.verify(repository).expurgarTutores(ids);
        ordem.verify(repository).expurgarEnderecos(List.of(10L, 20L));
    }

    @Test
    @DisplayName("AVL - Deve processar em lotes até não restar tutor expirado.")
    void deveProcessarEmLotes() {
        var lote1 = LongStream.rangeClosed(1, ExpurgoTutoresService.TAMANHO_LOTE).boxed().toList();
        var lote2 = List.of(9_999L);
        when(repository.findIdsExcluidosAntesDe(any(), any())).thenReturn(lote1, lote2, List.of());
        when(repository.findEnderecoIds(any())).thenReturn(List.of());

        assertThat(service.expurgar()).isEqualTo(ExpurgoTutoresService.TAMANHO_LOTE + 1);

        verify(repository).expurgarTutores(lote1);
        verify(repository).expurgarTutores(lote2);
        verify(repository, never()).expurgarEnderecos(any());
    }

    @Test
    @DisplayName("AVL - Deve respeitar o período de retenção configurado.")
    void deveRespeitarRetencaoConfigurada() {
        ReflectionTestUtils.setField(service, "diasRetencao", 7);
        when(repository.findIdsExcluidosAntesDe(any(), any())).thenReturn(List.of());

        service.expurgar();

        verify(repository).findIdsExcluidosAntesDe(eq(AGORA.minusDays(7)), any(Pageable.class));
    }
}
