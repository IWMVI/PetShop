package br.iwmvi.petshop.funcionario;

import br.iwmvi.petshop.funcionario.dto.request.FuncionarioRequest;
import br.iwmvi.petshop.funcionario.model.Cargo;
import br.iwmvi.petshop.funcionario.model.Funcionario;
import br.iwmvi.petshop.tutor.CpfTestData;
import org.springframework.test.util.ReflectionTestUtils;

public class FuncionarioTestData {

    public static FuncionarioRequest criarFuncionarioRequest() {
        return new FuncionarioRequest("Dra. Ana", CpfTestData.VALIDO, Cargo.VETERINARIO, "11988887777");
    }

    public static Funcionario criarFuncionario() {
        var funcionario = new Funcionario("Dra. Ana", CpfTestData.VALIDO, Cargo.VETERINARIO, "11988887777");

        ReflectionTestUtils.setField(funcionario, "id", 1L);

        return funcionario;
    }
}
