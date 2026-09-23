package br.iwmvi.petshop.tutor.mapper;

import br.iwmvi.petshop.common.mapper.RequestMapper;
import br.iwmvi.petshop.common.mapper.ResponseMapper;
import br.iwmvi.petshop.endereco.mapper.EnderecoMapper;
import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.tutor.dto.response.TutorResponse;
import br.iwmvi.petshop.tutor.model.Tutor;
import org.springframework.stereotype.Component;

@Component
public class TutorMapper implements RequestMapper<TutorRequest, Tutor>, ResponseMapper<Tutor, TutorResponse> {

    public Tutor toEntity(TutorRequest request) {
        return new Tutor(
                request.nome(),
                request.email().toLowerCase(),
                request.telefone().replaceAll("\\D", ""),
                EnderecoMapper.toEntity(request.endereco())
        );
    }

    public void atualizarEntidade(Tutor tutor, TutorRequest request) {
        tutor.setNome(request.nome());
        tutor.setEmail(request.email().toLowerCase());
        tutor.setTelefone(request.telefone().replaceAll("\\D", ""));
        EnderecoMapper.atualizarEntidade(tutor.getEndereco(), request.endereco());
    }

    @Override
    public TutorResponse toResponse(Tutor tutor) {
        return new TutorResponse(
                tutor.getId(),
                tutor.getNome(),
                tutor.getEmail(),
                tutor.getTelefone(),
                EnderecoMapper.toResponse(tutor.getEndereco())
        );
    }
}