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
                apenasDigitos(request.cpf()),
                request.email().toLowerCase(),
                apenasDigitos(request.telefone()),
                EnderecoMapper.toEntity(request.endereco())
        );
    }

    public void atualizarEntidade(Tutor tutor, TutorRequest request) {
        tutor.setNome(request.nome());
        tutor.setCpf(apenasDigitos(request.cpf()));
        tutor.setEmail(request.email().toLowerCase());
        tutor.setTelefone(apenasDigitos(request.telefone()));
        EnderecoMapper.atualizarEntidade(tutor.getEndereco(), request.endereco());
    }

    @Override
    public TutorResponse toResponse(Tutor tutor) {
        return new TutorResponse(
                tutor.getId(),
                tutor.getNome(),
                tutor.getCpf(),
                tutor.getEmail(),
                tutor.getTelefone(),
                EnderecoMapper.toResponse(tutor.getEndereco())
        );
    }

    private static String apenasDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
