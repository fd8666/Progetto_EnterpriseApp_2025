package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterpriceappbackend.data.entity.Evento;
import org.example.enterpriceappbackend.data.entity.TagCategoria;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.repository.EventoRepository;
import org.example.enterpriceappbackend.data.repository.TagCategoriaRepository;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.example.enterpriceappbackend.data.service.EventoService;
import org.example.enterpriceappbackend.dto.EventoDTO;
import org.example.enterpriceappbackend.exceptions.BadRequest;
import org.example.enterpriceappbackend.exceptions.NotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final UtenteRepository utenteRepository;
    private final TagCategoriaRepository tagCategoriaRepository;

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequest("ID non valido");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventoDTO findById(Long id) {
        validateId(id);
        return eventoRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFound("Evento non trovato con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> findAll() {
        return eventoRepository.findAll().stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> findByOrganizzatore(Long organizzatoreId) {
        validateId(organizzatoreId);
        Utente organizzatore = utenteRepository.findById(organizzatoreId)
                .orElseThrow(() -> new NotFound("Organizzatore non trovato con id: " + organizzatoreId));
        return eventoRepository.findByOrganizzatore(organizzatore).stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> findByCategoria(Long categoriaId) {
        validateId(categoriaId);
        TagCategoria categoria = tagCategoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new NotFound("Categoria non trovata con id: " + categoriaId));
        return eventoRepository.findByCategoria(categoria).stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> findByData(LocalDateTime data) {
        return eventoRepository.findByDataOraAperturaCancelliAfter(data).stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> findByNomeContaining(String nome) {
        return eventoRepository.findByNomeContainingIgnoreCase(nome).stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    public EventoDTO create(EventoDTO eventoDTO) {
        TagCategoria categoria = tagCategoriaRepository.findById(eventoDTO.getCategoriaId())
                .orElseThrow(() -> new NotFound("Categoria non trovata con id: " + eventoDTO.getCategoriaId()));
        Evento evento = toEntity(eventoDTO);
        evento.setCategoria(categoria);
        evento = eventoRepository.save(evento);
        log.info("Evento creato con ID {}", evento.getId());
        return toDto(evento);
    }

    @Override
    public EventoDTO save(EventoDTO eventoDTO) {
        Evento evento = toEntity(eventoDTO);
        evento = eventoRepository.save(evento);
        log.info("Evento salvato con ID {}", evento.getId());
        return toDto(evento);
    }

    @Override
    public EventoDTO update(Long id, EventoDTO eventoDTO) {
        validateId(id);
        if (!eventoRepository.existsById(id)) {
            throw new NotFound("Evento non trovato con id: " + id);
        }
        Evento evento = toEntity(eventoDTO);
        evento.setId(id);
        TagCategoria categoria = tagCategoriaRepository.findById(eventoDTO.getCategoriaId())
                .orElseThrow(() -> new NotFound("Categoria non trovata con id: " + eventoDTO.getCategoriaId()));
        evento.setCategoria(categoria);
        evento = eventoRepository.save(evento);
        log.info("Evento aggiornato con ID {}", evento.getId());
        return toDto(evento);
    }

    @Override
    public void delete(Long id) {
        validateId(id);
        if (!eventoRepository.existsById(id)) {
            throw new NotFound("Evento non trovato con id: " + id);
        }
        eventoRepository.deleteById(id);
        log.info("Evento eliminato con ID {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> findByLuogoContainingIgnoreCase(String luogo) {
        return eventoRepository.findByLuogoContainingIgnoreCase(luogo).stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> findByDataOraAperturaCancelliBetween(LocalDateTime dataInizio, LocalDateTime dataFine) {
        return eventoRepository.findByDataOraAperturaCancelliBetween(dataInizio, dataFine).stream()
                .map(this::toDto)
                .collect(toList());
    }

    private EventoDTO toDto(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setId(evento.getId());
        dto.setNome(evento.getNome());
        dto.setDescrizione(evento.getDescrizione());
        dto.setImmagine(evento.getImmagine());
        dto.setDataOraEvento(evento.getDataOraEvento());
        dto.setDataOraAperturaCancelli(evento.getDataOraAperturaCancelli());
        dto.setPostiDisponibili(evento.getPostiDisponibili());
        dto.setLuogo(evento.getLuogo());
        dto.setBiglietti(evento.getBiglietti());
        dto.setCategoriaId(evento.getCategoria() != null ? evento.getCategoria().getId() : null);
        dto.setOrganizzatoreId(evento.getOrganizzatore() != null ? evento.getOrganizzatore().getId() : null);
        dto.setStruttura(evento.getStruttura());
        dto.setTipiPosto(evento.getTipiPosto());
        return dto;
    }

    private Evento toEntity(EventoDTO dto) {
        Evento evento = new Evento();
        evento.setNome(dto.getNome());
        evento.setDescrizione(dto.getDescrizione());
        evento.setImmagine(dto.getImmagine());
        evento.setDataOraEvento(dto.getDataOraEvento());
        evento.setDataOraAperturaCancelli(dto.getDataOraAperturaCancelli());
        evento.setPostiDisponibili(dto.getPostiDisponibili());
        evento.setLuogo(dto.getLuogo());
        evento.setBiglietti(dto.getBiglietti());
        evento.setStruttura(dto.getStruttura());
        evento.setTipiPosto(dto.getTipiPosto());

        if (dto.getOrganizzatoreId() != null) {
            Utente organizzatore = utenteRepository.findById(dto.getOrganizzatoreId())
                    .orElseThrow(() -> new NotFound("Organizzatore non trovato con id: " + dto.getOrganizzatoreId()));
            evento.setOrganizzatore(organizzatore);
        }

        return evento;
    }
}