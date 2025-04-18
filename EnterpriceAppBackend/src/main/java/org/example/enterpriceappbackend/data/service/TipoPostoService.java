package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.TipoPosto;

import java.util.List;

public interface TipoPostoService {
    void save (TipoPosto tipoPosto);
    TipoPosto getTipoPostoById(int id);
    TipoPosto getTipoPostoByNome(String nome);
    List<TipoPosto> getAllTipoPosto();

}
