package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.EventoDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface EventoService {
    EventoDTO findById(Long id);
    List<EventoDTO> findAll();
    List<EventoDTO> findByOrganizzatore(Long organizzatoreId);
    List<EventoDTO> findByCategoria(Long categoriaId);
    List<EventoDTO> findByData(LocalDateTime data);
    List<EventoDTO> findByNomeContaining(String nome);

    //CRUD
    EventoDTO create(EventoDTO eventoDTO);
    EventoDTO save(EventoDTO eventoDTO);
    EventoDTO update(Long id, EventoDTO eventoDTO);
    void delete(Long id);
    //metodo che trova evento tramite luogo ignorando minuscole/maiuscole
    List<EventoDTO> findByLuogoContainingIgnoreCase(String luogo);
    List<EventoDTO> findByDataOraAperturaCancelliBetween(LocalDateTime dataInizio, LocalDateTime dataFine);
    void checkEventiScaduti();



    byte[] generaICS(Long eventoId);
}
