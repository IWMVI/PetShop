package br.iwmvi.petshop.common.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Página de resultados de uma listagem.
 *
 * @param itens        itens da página atual
 * @param pagina       índice da página (começa em 0)
 * @param tamanho      quantidade máxima de itens por página
 * @param total        total de itens em todas as páginas
 * @param totalPaginas quantidade de páginas
 */
public record PaginaResponse<T>(List<T> itens, int pagina, int tamanho, long total, int totalPaginas) {

    public static final int TAMANHO_PADRAO = 10;
    public static final int TAMANHO_MAXIMO = 50;

    public static <T> PaginaResponse<T> of(Page<T> page) {
        return new PaginaResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /** Monta o Pageable limitando página e tamanho a valores válidos. */
    public static Pageable pageable(int pagina, int tamanho, Sort ordenacao) {
        int tamanhoValido = Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO);
        return PageRequest.of(Math.max(pagina, 0), tamanhoValido, ordenacao);
    }
}
