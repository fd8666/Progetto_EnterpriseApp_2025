package it.unical.ea.eventra.data.service.serviceImpl;

import it.unical.ea.eventra.data.entity.Biglietto;
import it.unical.ea.eventra.data.entity.Evento;
import it.unical.ea.eventra.data.entity.Pagamento;
import it.unical.ea.eventra.data.entity.TipoPosto;
import it.unical.ea.eventra.data.repository.BigliettoRepository;
import it.unical.ea.eventra.data.repository.EventoRepository;
import it.unical.ea.eventra.data.repository.PagamentoRepository;
import it.unical.ea.eventra.data.repository.TipoPostoRepository;
import it.unical.ea.eventra.dto.BigliettoCreateDTO;
import it.unical.ea.eventra.dto.BigliettoEditSpettatoreDTO;
import it.unical.ea.eventra.dto.BigliettoInfoDTO;
import it.unical.ea.eventra.core.EmailService;
import it.unical.ea.eventra.data.service.BigliettoService;
import it.unical.ea.eventra.exception.NotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BigliettoServiceImpl implements BigliettoService {

    private final BigliettoRepository bigliettoRepository;
    private final EventoRepository eventoRepository;
    private final TipoPostoRepository tipoPostoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final EmailService emailService;

    public BigliettoServiceImpl(BigliettoRepository bigliettoRepository,
                                EventoRepository eventoRepository,
                                TipoPostoRepository tipoPostoRepository,
                                PagamentoRepository pagamentoRepository, EmailService emailService) {
        this.bigliettoRepository = bigliettoRepository;
        this.eventoRepository = eventoRepository;
        this.tipoPostoRepository = tipoPostoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.emailService = emailService;
    }

    @Override
    public BigliettoInfoDTO findById(Long id) {
        Biglietto biglietto = bigliettoRepository.findById(id)
                .orElseThrow(() -> new NotFound("Biglietto non trovato"));
        return convertToInfoDTO(biglietto);
    }

    @Override
    public List<BigliettoInfoDTO> findByTipoPosto(Long tipoPostoId) {
        return bigliettoRepository.findByTipoPostoId(tipoPostoId).stream()
                .map(this::convertToInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BigliettoInfoDTO> findByEvento(Long eventoId) {
        return bigliettoRepository.findByEventoId(eventoId).stream()
                .map(this::convertToInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BigliettoInfoDTO createBiglietto(BigliettoCreateDTO bigliettoCreateDTO) {
        Evento evento = eventoRepository.findById(bigliettoCreateDTO.getEventoId())
                .orElseThrow(() -> new NotFound("Evento non trovato"));

        TipoPosto tipoPosto = tipoPostoRepository.findById(bigliettoCreateDTO.getTipoPostoId())
                .orElseThrow(() -> new NotFound("Tipo posto non trovato"));

        Pagamento pagamento = null;
        if (bigliettoCreateDTO.getPagamentoId() != null) {
            pagamento = pagamentoRepository.findById(bigliettoCreateDTO.getPagamentoId())
                    .orElseThrow(() -> new NotFound("Pagamento non trovato"));
        }

        Biglietto biglietto = new Biglietto();
        biglietto.setNomeSpettatore(bigliettoCreateDTO.getNomeSpettatore());
        biglietto.setCognomeSpettatore(bigliettoCreateDTO.getCognomeSpettatore());
        biglietto.setEmailSpettatore(bigliettoCreateDTO.getEmailSpettatore());
        biglietto.setEvento(evento);
        biglietto.setTipoPosto(tipoPosto);
        biglietto.setPagamento(pagamento);
        biglietto.setDeleted(0);

        Biglietto savedBiglietto = bigliettoRepository.save(biglietto);

        emailService.sendBigliettoConferma(savedBiglietto);

        return convertToInfoDTO(savedBiglietto);
    }

    @Override
    public List<BigliettoInfoDTO> findByUtente(Long utenteId) {
        return bigliettoRepository.findByPagamentoOrdineProprietarioId(utenteId).stream()
                .map(this::convertToInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BigliettoInfoDTO updateSpettatore(Long id, BigliettoEditSpettatoreDTO bigliettoEditSpettatoreDTO) {
        Biglietto biglietto = bigliettoRepository.findById(id)
                .orElseThrow(() -> new NotFound("Biglietto non trovato"));

        biglietto.setNomeSpettatore(bigliettoEditSpettatoreDTO.getNomeSpettatore());
        biglietto.setCognomeSpettatore(bigliettoEditSpettatoreDTO.getCognomeSpettatore());
        biglietto.setEmailSpettatore(bigliettoEditSpettatoreDTO.getEmailSpettatore());

        Biglietto updatedBiglietto = bigliettoRepository.save(biglietto);

        emailService.sendModificaBigliettoConferma(updatedBiglietto);
        return convertToInfoDTO(updatedBiglietto);
    }

    @Override
    @Transactional
    public void deleteBiglietto(Long id) {
        Biglietto biglietto = bigliettoRepository.findById(id)
                .orElseThrow(() -> new NotFound("Biglietto non trovato"));
        bigliettoRepository.delete(biglietto);
    }

    @Override
    public Double getPrezzoBiglietto(Long id) {
        Biglietto biglietto = bigliettoRepository.findById(id)
                .orElseThrow(() -> new NotFound("Biglietto non trovato"));
        return biglietto.getTipoPosto().getPrezzo();
    }

    private BigliettoInfoDTO convertToInfoDTO(Biglietto biglietto) {
        BigliettoInfoDTO dto = new BigliettoInfoDTO();
        dto.setId(biglietto.getId());
        dto.setNomeSpettatore(biglietto.getNomeSpettatore());
        dto.setCognomeSpettatore(biglietto.getCognomeSpettatore());
        dto.setEmailSpettatore(biglietto.getEmailSpettatore());
        dto.setDataCreazione(biglietto.getDataCreazione());
        dto.setEventoId(biglietto.getEvento().getId());
        dto.setTipoPostoId(biglietto.getTipoPosto().getId());
        if (biglietto.getPagamento() != null) {
            dto.setPagamentoId(biglietto.getPagamento().getId());
        }
        return dto;
    }

    public void checkBigliettiScaduti() {
        // Trova tutti gli eventi passati
        List<Biglietto> biglietti = bigliettoRepository.findAll();

        biglietti.forEach(biglietto -> {
            if (biglietto.getEvento().getDataOraEvento().isBefore(LocalDateTime.now())) {
                // Se l'evento è passato, il biglietto è scaduto
                System.out.println("Biglietto scaduto per l'evento: " + biglietto.getEvento().getNome());
                biglietto.setDeleted(1);
                bigliettoRepository.save(biglietto);

            }
        });
    }
}