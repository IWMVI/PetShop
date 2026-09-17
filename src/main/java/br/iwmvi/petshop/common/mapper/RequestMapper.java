package br.iwmvi.petshop.common.mapper;

public interface RequestMapper<REQ, T> {

    T toEntity(REQ request);
}
