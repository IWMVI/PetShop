package br.iwmvi.petshop.historico;

import br.iwmvi.petshop.historico.controller.HistoricoPetController;
import br.iwmvi.petshop.historico.model.HistoricoPet;
import br.iwmvi.petshop.historico.repository.HistoricoPetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricoPetImutabilidadeTest {

    @Test
    @DisplayName("PCE - O controller não deve expor endpoints de alteração ou exclusão.")
    void controllerNaoDeveExporPutPatchOuDelete() {
        for (Method metodo : HistoricoPetController.class.getDeclaredMethods()) {
            assertThat(metodo.isAnnotationPresent(PutMapping.class)).as(metodo.getName()).isFalse();
            assertThat(metodo.isAnnotationPresent(PatchMapping.class)).as(metodo.getName()).isFalse();
            assertThat(metodo.isAnnotationPresent(DeleteMapping.class)).as(metodo.getName()).isFalse();
            assertThat(metodo.isAnnotationPresent(RequestMapping.class)).as(metodo.getName()).isFalse();
        }
    }

    @Test
    @DisplayName("PCE - O repositório não deve oferecer métodos de exclusão.")
    void repositorioNaoDeveOferecerExclusao() {
        assertThat(HistoricoPetRepository.class.getMethods())
                .extracting(Method::getName)
                .noneMatch(nome -> nome.startsWith("delete"));
    }

    @Test
    @DisplayName("PCE - A entidade não deve possuir setters públicos.")
    void entidadeNaoDevePossuirSetters() {
        var setters = Arrays.stream(HistoricoPet.class.getDeclaredMethods())
                .filter(metodo -> Modifier.isPublic(metodo.getModifiers()))
                .filter(metodo -> metodo.getName().startsWith("set"));

        assertThat(setters).isEmpty();
    }
}
