package br.iwmvi.petshop.tutor.mapper;

import br.iwmvi.petshop.endereco.mapper.EnderecoMapper;
import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.tutor.dto.response.TutorResponse;
import br.iwmvi.petshop.tutor.model.Tutor;

public final class TutorMapper {

    private TutorMapper() {
    }

    public static Tutor toEntity(TutorRequest request) {
        return new Tutor(
                request.nome(),
                request.email().toLowerCase(),
                request.telefone().replaceAll("\\D", ""),
                EnderecoMapper.toEntity(request.endereco())
        );
    }

    public static TutorResponse toResponse(Tutor tutor) {
        return new TutorResponse(
                tutor.getId(),
                tutor.getNome(),
                tutor.getEmail(),
                tutor.getTelefone(),
                EnderecoMapper.toResponse(tutor.getEndereco())
        );
    }
}
