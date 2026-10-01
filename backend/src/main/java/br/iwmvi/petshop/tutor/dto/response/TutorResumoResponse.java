package br.iwmvi.petshop.tutor.dto.response;

/**
 * Resumo do tutor dono de um CPF, usado para oferecer a recuperação de um cadastro excluído
 * (ou apontar o cadastro ativo) quando o CPF informado já existe.
 */
public record TutorResumoResponse(
        Long id,
        String nome,
        String email,
        boolean excluido
) {
}
