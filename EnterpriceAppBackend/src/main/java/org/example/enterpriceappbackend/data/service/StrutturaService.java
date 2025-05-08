package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.Struttura;
import org.example.enterpriceappbackend.dto.StrutturaInfoOrganizzatoreDTO;
import org.example.enterpriceappbackend.dto.StrutturaInfoUtenteDTO;
import org.example.enterpriceappbackend.dto.StrutturaMapInfoDTO;

import java.util.List;

public interface StrutturaService {

    Struttura getById(Long id);

    List<Struttura> getByNome(String nome);

    List<Struttura> getByCategoria(String categoria);

    List<Struttura> getByIndirizzo(String indirizzo);

    Long countByCategoria(String Categoria);

    List<StrutturaInfoOrganizzatoreDTO> filtraStrutture(String nome, String categoria, String indirizzo);

    //anche qua la mappa con coordinate alra api
    StrutturaInfoUtenteDTO getInfoForUtenteById(Long id);
    StrutturaInfoUtenteDTO getStrutturaByEvento(Long eventoId);

    //lista iniziale di tutte le strutture per l'organizzatore che ne selezionera una
    List<StrutturaInfoOrganizzatoreDTO> getAllForOrganizzatore();

    //struttura singola, mappa e zone si prenderanno da altre api
    StrutturaInfoOrganizzatoreDTO getOrganizzatoreDTO(Long id);

    StrutturaMapInfoDTO getMapInfoById(Long id);

    List<String> getAllCategorie();
}
