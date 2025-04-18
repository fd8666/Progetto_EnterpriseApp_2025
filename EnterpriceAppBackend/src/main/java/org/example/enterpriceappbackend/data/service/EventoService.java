package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.dto.EventoDto;

import java.util.List;

public interface EventoService {
    EventoDto create(EventoDto eventoDTO);
    EventoDto getEventoById(Long id);
    List<EventoDto> getAllEventi();
    void delete(Long id);
}
