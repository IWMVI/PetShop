package br.iwmvi.petshop.funcionario.mapper;

import br.iwmvi.petshop.common.mapper.RequestMapper;
import br.iwmvi.petshop.common.mapper.ResponseMapper;
import br.iwmvi.petshop.funcionario.dto.request.FuncionarioRequest;
import br.iwmvi.petshop.funcionario.dto.response.FuncionarioResponse;
import br.iwmvi.petshop.funcionario.model.Funcionario;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioMapper implements RequestMapper<FuncionarioRequest, Funcionario>, ResponseMapper<Funcionario, FuncionarioResponse> {

    @Override
    public Funcionario toEntity(FuncionarioRequest request) {
        return new Funcionario(
                request.nome(),
                apenasDigitos(request.cpf()),
                request.cargo(),
                request.telefone()
        );
    }

    public void atualizarEntidade(Funcionario funcionario, FuncionarioRequest request) {
        funcionario.atualizar(
                request.nome(),
                apenasDigitos(request.cpf()),
                request.cargo(),
                request.telefone()
        );
    }

    @Override
    public FuncionarioResponse toResponse(Funcionario entity) {
        return new FuncionarioResponse(
                entity.getId(),
                entity.getNome(),
                entity.getCpf(),
                entity.getCargo(),
                entity.getTelefone()
        );
    }

    private String apenasDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
