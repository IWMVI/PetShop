package br.iwmvi.petshop.funcionario.controller;

import br.iwmvi.petshop.common.controller.CrudController;
import br.iwmvi.petshop.common.service.CrudService;
import br.iwmvi.petshop.funcionario.dto.request.FuncionarioRequest;
import br.iwmvi.petshop.funcionario.dto.response.FuncionarioResponse;
import br.iwmvi.petshop.funcionario.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para operações CRUD de Funcionário.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/funcionarios")
public class FuncionarioController extends CrudController<Long, FuncionarioRequest, FuncionarioResponse> {

    private final FuncionarioService funcionarioService;

    @Override
    protected CrudService<?, Long, FuncionarioRequest, FuncionarioResponse> getService() {
        return funcionarioService;
    }
}
