package br.iwmvi.petshop.servico.controller;

import br.iwmvi.petshop.common.controller.CrudController;
import br.iwmvi.petshop.common.service.CrudService;
import br.iwmvi.petshop.servico.dto.request.ServicoRequest;
import br.iwmvi.petshop.servico.dto.response.ServicoResponse;
import br.iwmvi.petshop.servico.service.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para operações CRUD de Serviço.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/servicos")
public class ServicoController extends CrudController<Long, ServicoRequest, ServicoResponse> {

    private final ServicoService servicoService;

    @Override
    protected CrudService<?, Long, ServicoRequest, ServicoResponse> getService() {
        return servicoService;
    }
}
