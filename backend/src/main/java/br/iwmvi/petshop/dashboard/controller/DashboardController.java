package br.iwmvi.petshop.dashboard.controller;

import br.iwmvi.petshop.dashboard.dto.response.DashboardGeralResponse;
import br.iwmvi.petshop.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService service;

    @GetMapping
    public DashboardGeralResponse dashboard() {
        return service.montar();
    }
}
