import { Component } from '@angular/core';
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ConsultaPaginada, Pagina } from '../../core/models';
import { ATRASO_BUSCA_MS, listagemPaginada } from './listagem-paginada';

const pagina = (itens: string[], p = 0, total = itens.length): Pagina<string> => ({
  itens,
  pagina: p,
  tamanho: 10,
  total,
  totalPaginas: Math.ceil(total / 10),
});

describe('listagemPaginada', () => {
  let carregar: jest.Mock;

  @Component({ template: '' })
  class Hospedeiro {
    lista = listagemPaginada<string>((c: ConsultaPaginada) => carregar(c));
  }

  const criar = () => TestBed.createComponent(Hospedeiro).componentInstance.lista;

  beforeEach(() => {
    carregar = jest.fn().mockReturnValue(of(pagina(['a', 'b'], 0, 12)));
  });

  it('carrega somente a página pedida, com 10 itens por página', () => {
    const lista = criar();
    lista.recarregar();
    expect(carregar).toHaveBeenCalledWith({ pagina: 0, tamanho: 10, busca: '' });
    expect(lista.itens()).toEqual(['a', 'b']);
    expect(lista.total()).toBe(12);
    expect(lista.carregando()).toBe(false);

    lista.irPara(1);
    expect(carregar).toHaveBeenLastCalledWith({ pagina: 1, tamanho: 10, busca: '' });
    expect(lista.pagina()).toBe(1);
  });

  it('aplica a busca com debounce e volta para a primeira página', fakeAsync(() => {
    const lista = criar();
    lista.recarregar();
    lista.irPara(1);
    carregar.mockClear();

    lista.buscar('an');
    lista.buscar('ana');
    tick(ATRASO_BUSCA_MS - 1);
    expect(carregar).not.toHaveBeenCalled();
    tick(1);

    expect(carregar).toHaveBeenCalledTimes(1);
    expect(carregar).toHaveBeenCalledWith({ pagina: 0, tamanho: 10, busca: 'ana' });
  }));

  it('volta uma página quando a atual fica vazia', () => {
    carregar
      .mockReturnValueOnce(of(pagina([], 1, 10)))
      .mockReturnValueOnce(of(pagina(['x'], 0, 10)));
    const lista = criar();
    lista.irPara(1);

    expect(carregar).toHaveBeenLastCalledWith({ pagina: 0, tamanho: 10, busca: '' });
    expect(lista.itens()).toEqual(['x']);
  });

  it('expõe o erro e continua funcionando depois', () => {
    carregar.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 0 })));
    const lista = criar();
    lista.recarregar();
    expect(lista.erro()).toContain('Não foi possível conectar');

    lista.recarregar();
    expect(lista.erro()).toBeNull();
    expect(lista.itens()).toEqual(['a', 'b']);
  });
});
