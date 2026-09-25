import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed, discardPeriodicTasks, fakeAsync, tick } from '@angular/core/testing';
import { INTERVALO_STATUS_MS, StatusApiService } from './status-api.service';

describe('StatusApiService', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  const url = '/api/actuator/health';

  it('fica online quando o health responde UP e verifica de novo periodicamente', fakeAsync(() => {
    const service = TestBed.inject(StatusApiService);
    expect(service.situacao()).toBe('verificando');

    tick(0);
    http.expectOne(url).flush({ status: 'UP' });
    expect(service.situacao()).toBe('online');

    tick(INTERVALO_STATUS_MS);
    http.expectOne(url).flush(null, { status: 0, statusText: 'Unknown Error' });
    expect(service.situacao()).toBe('offline');
    discardPeriodicTasks();
  }));

  it('fica instável quando o Actuator responde 503 (ex.: banco fora)', fakeAsync(() => {
    const service = TestBed.inject(StatusApiService);
    tick(0);
    http
      .expectOne(url)
      .flush({ status: 'DOWN' }, { status: 503, statusText: 'Service Unavailable' });
    expect(service.situacao()).toBe('instavel');
    discardPeriodicTasks();
  }));

  it('fica offline quando o proxy não alcança o back-end', fakeAsync(() => {
    const service = TestBed.inject(StatusApiService);
    tick(0);
    http.expectOne(url).flush('Bad Gateway', { status: 502, statusText: 'Bad Gateway' });
    expect(service.situacao()).toBe('offline');
    discardPeriodicTasks();
  }));
});
