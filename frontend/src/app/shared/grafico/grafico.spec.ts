import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Grafico } from './grafico';

describe('Grafico', () => {
  let fixture: ComponentFixture<Grafico>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Grafico] }).compileComponents();
    fixture = TestBed.createComponent(Grafico);
    fixture.componentRef.setInput('tipo', 'line');
    fixture.componentRef.setInput('dados', {
      labels: ['01/01', '02/01'],
      datasets: [{ data: [10, 20], label: 'Teste' }],
    });
  });

  it('instancia o Chart.js ao receber os dados, sem lançar erro', () => {
    expect(() => fixture.detectChanges()).not.toThrow();
    expect(fixture.nativeElement.querySelector('canvas')).not.toBeNull();
  });

  it('recria o chart quando os dados mudam, sem lançar erro', () => {
    fixture.detectChanges();

    fixture.componentRef.setInput('dados', {
      labels: ['01/01', '02/01', '03/01'],
      datasets: [{ data: [10, 20, 30], label: 'Teste' }],
    });

    expect(() => fixture.detectChanges()).not.toThrow();
  });

  it('destrói o chart ao ser destruído, sem lançar erro', () => {
    fixture.detectChanges();

    expect(() => fixture.destroy()).not.toThrow();
  });
});
