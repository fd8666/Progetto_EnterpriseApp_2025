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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

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
        log.debug("Filtering events between: {} and {}", dataInizio, dataFine);

        // Se entrambe le date sono null, restituisci tutti gli eventi
        if (dataInizio == null && dataFine == null) {
            log.debug("No date range provided, returning all events");
            return findAll();
        }

        // Se solo dataInizio è null, imposta a minima data possibile
        if (dataInizio == null) {
            dataInizio = LocalDateTime.MIN;
            log.debug("Start date not provided, using MIN date");
        }

        // Se solo dataFine è null, imposta a massima data possibile
        if (dataFine == null) {
            dataFine = LocalDateTime.MAX;
            log.debug("End date not provided, using MAX date");
        }

        // Verifica che dataInizio sia prima di dataFine
        if (dataInizio.isAfter(dataFine)) {
            log.warn("Invalid date range: start {} is after end {}", dataInizio, dataFine);
            throw new IllegalArgumentException("La data di inizio deve essere precedente alla data di fine");
        }

        List<Evento> eventi = eventoRepository.findByDataOraAperturaCancelliBetween(dataInizio, dataFine);
        log.debug("Found {} events in date range", eventi.size());

        return eventi.stream()
                .map(this::toDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public void checkEventiScaduti() {
        log.info("Controllo eventi scaduti in esecuzione: {}", LocalDateTime.now());

        LocalDateTime oggi = LocalDateTime.now();

        // Trova eventi la cui data è passata e che non sono già contrassegnati come deleted
        List<Evento> eventiScaduti = eventoRepository.findByDataOraEventoBeforeAndDeletedEquals(oggi, 0);

        if (!eventiScaduti.isEmpty()) {
            log.info("Trovati {} eventi scaduti da eliminare", eventiScaduti.size());

            // Contrassegna come eliminati (soft delete)
            for (Evento evento : eventiScaduti) {
                evento.setDeleted(1);
                eventoRepository.save(evento);
                log.info("Evento ID: {} '{}' contrassegnato come scaduto", evento.getId(), evento.getNome());
            }
        } else {
            log.info("Nessun evento scaduto trovato");
        }
    }


    @Override
    public byte[] generaICS(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        LocalDateTime inizio = evento.getDataOraEvento();
        LocalDateTime fine = inizio.plusHours(3);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\n")
                .append("VERSION:2.0\n")
                .append("PRODID:-//Eventra//EventraApp//EN\n")
                .append("METHOD:PUBLISH\n")
                .append("BEGIN:VEVENT\n")
                .append("UID:").append(UUID.randomUUID()).append("\n")
                .append("DTSTAMP:").append(LocalDateTime.now().format(formatter)).append("\n")
                .append("DTSTART:").append(inizio.format(formatter)).append("\n")
                .append("DTEND:").append(fine.format(formatter)).append("\n")
                .append("SUMMARY:").append(sanitize(evento.getNome())).append("\n")
                .append("DESCRIPTION:").append(sanitize(evento.getDescrizione())).append("\n")
                .append("LOCATION:").append(sanitize(evento.getStruttura().getNome())).append("\n")
                .append("END:VEVENT\n")
                .append("END:VCALENDAR");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\n", "\\n");
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
        evento.setDeleted(0);


        if (dto.getOrganizzatoreId() != null) {
            Utente organizzatore = utenteRepository.findById(dto.getOrganizzatoreId())
                    .orElseThrow(() -> new NotFound("Organizzatore non trovato con id: " + dto.getOrganizzatoreId()));
            evento.setOrganizzatore(organizzatore);
        }

        return evento;
    }
}