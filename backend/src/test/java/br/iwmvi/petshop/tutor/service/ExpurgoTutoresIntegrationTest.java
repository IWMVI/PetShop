package br.iwmvi.petshop.tutor.service;

import br.iwmvi.petshop.tutor.CpfTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executa o expurgo contra o esquema real (migrações Flyway no H2), garantindo que a ordem
 * dos DELETEs respeita as chaves estrangeiras que não têm ON DELETE CASCADE.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExpurgoTutoresIntegrationTest {

    @Autowired
    private ExpurgoTutoresService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("PCE - Deve remover definitivamente o tutor excluído há mais de 30 dias e todos os seus dados.")
    void deveRemoverTutorExpiradoComDependentes() {
        var expirado = criarTutorCompleto(LocalDateTime.now().minusDays(31));
        var recente = criarTutorCompleto(LocalDateTime.now().minusDays(29));
        var ativo = criarTutorCompleto(null);

        int removidos = service.expurgar();

        assertThat(removidos).isEqualTo(1);
        assertThat(existe("tutores", expirado.tutorId())).isFalse();
        assertThat(existe("enderecos", expirado.enderecoId())).isFalse();
        assertThat(existe("pets", expirado.petId())).isFalse();
        assertThat(existe("agendamentos", expirado.agendamentoId())).isFalse();
        assertThat(contar("agendamento_servicos", "agendamento_id", expirado.agendamentoId())).isZero();
        assertThat(contar("pagamentos", "agendamento_id", expirado.agendamentoId())).isZero();
        assertThat(contar("historico_pets", "pet_id", expirado.petId())).isZero();

        for (var mantido : new Dados[]{recente, ativo}) {
            assertThat(existe("tutores", mantido.tutorId())).isTrue();
            assertThat(existe("enderecos", mantido.enderecoId())).isTrue();
            assertThat(existe("pets", mantido.petId())).isTrue();
            assertThat(existe("agendamentos", mantido.agendamentoId())).isTrue();
            assertThat(contar("agendamento_servicos", "agendamento_id", mantido.agendamentoId())).isEqualTo(1);
        }

        // O serviço é compartilhado entre tutores e não deve ser apagado.
        assertThat(existe("servicos", expirado.servicoId())).isTrue();
        // O funcionário também é compartilhado: sai o histórico do pet, mas o funcionário permanece.
        assertThat(existe("funcionarios", expirado.funcionarioId())).isTrue();
    }

    private record Dados(long tutorId, long enderecoId, long petId, long agendamentoId, long servicoId,
                         long funcionarioId) {
    }

    private Dados criarTutorCompleto(LocalDateTime excluidoEm) {
        long enderecoId = inserir("enderecos", Map.of(
                "cep", "01001000", "logradouro", "Praça da Sé", "bairro", "Sé",
                "cidade", "São Paulo", "estado", "SP"));
        var tutor = new java.util.HashMap<String, Object>(Map.of(
                "nome", "Tutor expurgo", "cpf", CpfTestData.gerar(),
                "email", "expurgo" + System.nanoTime() + "@test.com",
                "telefone", "11999999999", "endereco_id", enderecoId));
        if (excluidoEm != null) {
            tutor.put("deleted_at", excluidoEm);
        }
        long tutorId = inserir("tutores", tutor);
        long petId = inserir("pets", Map.of("tutor_id", tutorId, "nome", "Rex", "especie", "Cachorro"));
        long servicoId = inserir("servicos", Map.of("nome", "Banho", "preco", 50));
        long agendamentoId = inserir("agendamentos", Map.of(
                "pet_id", petId, "data_hora", LocalDateTime.now().plusDays(1), "status", "AGENDADO"));
        new SimpleJdbcInsert(jdbc).withTableName("agendamento_servicos")
                .execute(Map.of("agendamento_id", agendamentoId, "servico_id", servicoId, "preco_cobrado", 50));
        inserir("pagamentos", Map.of("agendamento_id", agendamentoId, "valor", 50));
        long funcionarioId = inserir("funcionarios", Map.of(
                "nome", "Tosador", "cpf", CpfTestData.gerar(), "cargo", "TOSADOR", "telefone", "11988887777"));
        inserir("historico_pets", Map.of(
                "pet_id", petId, "funcionario_id", funcionarioId, "tipo_evento", "SERVICO",
                "descricao", "Banho", "data_evento", LocalDateTime.now().minusDays(40)));
        return new Dados(tutorId, enderecoId, petId, agendamentoId, servicoId, funcionarioId);
    }

    private long inserir(String tabela, Map<String, ?> valores) {
        return new SimpleJdbcInsert(jdbc)
                .withTableName(tabela)
                .usingColumns(valores.keySet().toArray(String[]::new))
                .usingGeneratedKeyColumns("id")
                .executeAndReturnKey(Map.copyOf(valores))
                .longValue();
    }

    private boolean existe(String tabela, long id) {
        return contar(tabela, "id", id) > 0;
    }

    private int contar(String tabela, String coluna, long valor) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabela + " WHERE " + coluna + " = ?", Integer.class, valor);
    }
}
