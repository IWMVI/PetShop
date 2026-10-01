import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CepService, EnderecoCep, VIACEP_URL } from './cep.service';

describe('CepService', () => {
  let service: CepService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CepService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('consulta o ViaCEP e converte a resposta', () => {
    let resultado: EnderecoCep | null | undefined;
    service.buscar('01001-000').subscribe((r) => (resultado = r));

    http.expectOne(`${VIACEP_URL}/01001000/json/`).flush({
      cep: '01001-000',
      logradouro: 'Praça da Sé',
      complemento: 'lado ímpar',
      bairro: 'Sé',
      localidade: 'São Paulo',
      uf: 'SP',
    });

    expect(resultado).toEqual({
      cep: '01001-000',
      logradouro: 'Praça da Sé',
      complemento: 'lado ímpar',
      bairro: 'Sé',
      cidade: 'São Paulo',
      estado: 'SP',
    });
  });

  it('retorna null quando o CEP não existe', () => {
    let resultado: EnderecoCep | null | undefined;
    service.buscar('99999999').subscribe((r) => (resultado = r));
    http.expectOne(`${VIACEP_URL}/99999999/json/`).flush({ erro: 'true' });
    expect(resultado).toBeNull();
  });

  it('retorna null quando a consulta falha', () => {
    let resultado: EnderecoCep | null | undefined;
    service.buscar('01001000').subscribe((r) => (resultado = r));
    http.expectOne(`${VIACEP_URL}/01001000/json/`).error(new ProgressEvent('offline'));
    expect(resultado).toBeNull();
  });

  it('não consulta CEP incompleto', () => {
    let resultado: EnderecoCep | null | undefined;
    service.buscar('0100').subscribe((r) => (resultado = r));
    http.expectNone(() => true);
    expect(resultado).toBeNull();
  });
});
