package br.iwmvi.petshop.tutor;

import java.util.concurrent.atomic.AtomicLong;

public final class CpfTestData {

    /** CPF válido, apenas dígitos. */
    public static final String VALIDO = "52998224725";

    /** O mesmo CPF válido, com máscara. */
    public static final String VALIDO_FORMATADO = "529.982.247-25";

    private static final AtomicLong SEQUENCIA = new AtomicLong(100_000_000L);

    private CpfTestData() {
    }

    /** Gera um CPF válido e único durante a execução dos testes (útil onde há restrição de unicidade). */
    public static String gerar() {
        String base = String.format("%09d", SEQUENCIA.incrementAndGet() % 1_000_000_000L);
        int d1 = digitoVerificador(base, 10);
        int d2 = digitoVerificador(base + d1, 11);
        return base + d1 + d2;
    }

    private static int digitoVerificador(String digitos, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < digitos.length(); i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
