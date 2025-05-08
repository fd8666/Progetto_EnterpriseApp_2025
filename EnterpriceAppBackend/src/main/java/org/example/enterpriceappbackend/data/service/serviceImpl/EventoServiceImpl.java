package org.example.enterpriceappbackend.data.service.serviceImpl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Evento;
import org.example.enterpriceappbackend.data.entity.TagCategoria;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.repository.EventoRepository;
import org.example.enterpriceappbackend.data.repository.TagCategoriaRepository;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.example.enterpriceappbackend.data.service.EventoService;
import org.example.enterpriceappbackend.dto.EventoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final UtenteRepository utenteRepository;
    private final TagCategoriaRepository tagCategoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public EventoDTO findById(Long id) {
        return eventoRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Evento non trovato con id: " + id));
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
        Utente organizzatore = utenteRepository.findById(organizzatoreId)
                .orElseThrow(() -> new EntityNotFoundException("Organizzatore non trovato con id: " + organizzatoreId));
        return eventoRepository.findByOrganizzatore(organizzatore).stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> findByCategoria(Long categoriaId) {
        TagCategoria categoria = tagCategoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria non trovata con id: " + categoriaId));
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
                .orElseThrow(() -> new RuntimeException("Categoria non trovata"));
        Evento evento = toEntity(eventoDTO);
        evento.setCategoria(categoria);
        // Salva l'evento
        evento = eventoRepository.save(evento);
        // Converte l'entità Evento in DTO e lo restituisce
        return toDto(evento);
    }


    @Override
    public EventoDTO save(EventoDTO eventoDTO) {
        Evento evento = toEntity(eventoDTO);
        evento = eventoRepository.save(evento);
        return toDto(evento);
    }

    @Override
    public EventoDTO update(Long id, EventoDTO eventoDTO) {
        if (!eventoRepository.existsById(id)) {
            throw new EntityNotFoundException("Evento non trovato con id: " + id);
        }
        Evento evento = toEntity(eventoDTO);
        evento.setId(id);
        TagCategoria categoria = tagCategoriaRepository.findById(eventoDTO.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria non trovata con id: " + eventoDTO.getCategoriaId()));

        evento.setCategoria(categoria);
        evento = eventoRepository.save(evento);
        return toDto(evento);
    }

    @Override
    public void delete(Long id) {
        if (!eventoRepository.existsById(id)) {
            throw new EntityNotFoundException("Evento non trovato con id: " + id);
        }
        eventoRepository.deleteById(id);
    }

    @Override
    public List<EventoDTO> findByLuogoContainingIgnoreCase(String luogo) {
        List<Evento> eventi = eventoRepository.findByLuogoContainingIgnoreCase(luogo);
        return eventi.stream().map(this::toDto).collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> findByDataOraAperturaCancelliBetween(
            LocalDateTime dataInizio, LocalDateTime dataFine
    ) {
        List<Evento> eventi = eventoRepository.findByDataOraAperturaCancelliBetween(
                dataInizio, dataFine
        );
        return eventi.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }



    // -------------------- MAPPER INTERNO --------------------

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
       // dto.setStrutture(evento.getStrutture()); per mirko andra modificato secondo la 1:n non piu gestita da m:m
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
      //  evento.setStrutture(dto.getStrutture());  per mirko andra modificato secondo la 1:n
        evento.setTipiPosto(dto.getTipiPosto());

        if (dto.getOrganizzatoreId() != null) {
            Utente organizzatore = new Utente();
            organizzatore.setId(dto.getOrganizzatoreId());
            evento.setOrganizzatore(organizzatore);
        }

        return evento;
    }
}
