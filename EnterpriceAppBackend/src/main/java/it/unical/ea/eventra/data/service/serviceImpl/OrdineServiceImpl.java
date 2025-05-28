package it.unical.ea.eventra.data.service.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.data.constants.StatoPagamento;
import it.unical.ea.eventra.data.entity.Ordine;
import it.unical.ea.eventra.data.entity.Pagamento;
import it.unical.ea.eventra.data.entity.Utente;
import it.unical.ea.eventra.data.repository.OrdineRepository;
import it.unical.ea.eventra.data.repository.PagamentoRepository;
import it.unical.ea.eventra.data.repository.UtenteRepository;
import it.unical.ea.eventra.data.service.OrdineService;
import it.unical.ea.eventra.dto.OrdineDTO;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdineServiceImpl implements OrdineService {

    private final OrdineRepository ordineRepository;
    private final PagamentoRepository pagamentoRepository;
    private final UtenteRepository utenteRepository;

    @Override
    @Transactional
    public Ordine findById(long id) {return ordineRepository.findById(id).orElseThrow(() -> new RuntimeException("Ordine non trovato con id: " + id));}

    @Override
    public List<OrdineDTO> findByProprietario(Long proprietarioId) {
        List<Ordine> ordini = ordineRepository.findByProprietarioId(proprietarioId);
        return ordini.stream().map(this::todto).toList();
    }

    @Override
    @Transactional
    public OrdineDTO aggiungiOrdine(OrdineDTO ordineDTO, Long proprietarioId) {
        Optional<Utente> utenteOpt = utenteRepository.findById(proprietarioId);

        if (utenteOpt.isEmpty()) {
            throw new RuntimeException("Utente non trovato con ID: " + proprietarioId);
        }

        Utente proprietario = utenteOpt.get();

        Ordine ordine = new Ordine();
        ordine.setEmailProprietario(proprietario.getEmail());
        ordine.setPrezzoTotale(ordineDTO.getPrezzoTotale());
        ordine.setProprietario(proprietario);
        ordine.setDataCreazione(LocalDateTime.now());

        Ordine ordineSalvato = ordineRepository.save(ordine);
        return todto(ordineSalvato);
    }


    @Override
    public void save(OrdineDTO ordineDTO) {
        Ordine ordine = toEntity(ordineDTO);
        ordineRepository.save(ordine);
    }

    @Override
    public void delete(Long id) {
        Ordine ordine = ordineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato con id: " + id));
        ordineRepository.delete(ordine);
    }

    @Override
    public OrdineDTO update(Long id, OrdineDTO ordineDTO) {
        Ordine ordineEsistente = ordineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato con id: " + id));

        ordineEsistente.setDataCreazione(ordineDTO.getDataCreazione());
        ordineEsistente.setEmailProprietario(ordineDTO.getEmailProprietario());


        if (ordineDTO.getPrezzoTotale() != null) {
            ordineEsistente.setPrezzoTotale(ordineDTO.getPrezzoTotale());
        }


        if (ordineDTO.getPagamento() != null) {
            Pagamento pagamentoDTO = ordineDTO.getPagamento();
            Pagamento nuovoPagamento = ordineEsistente.getPagamento();

            if (nuovoPagamento == null) {
                nuovoPagamento = new Pagamento();
                nuovoPagamento.setOrdine(ordineEsistente);  // Associa l'ordine al pagamento
            }

            nuovoPagamento.setNomeTitolare(pagamentoDTO.getNomeTitolare());
            nuovoPagamento.setCognomeTitolare(pagamentoDTO.getCognomeTitolare());
            nuovoPagamento.setNumeroCarta(pagamentoDTO.getNumeroCarta());
            nuovoPagamento.setScadenza(pagamentoDTO.getScadenza());
            nuovoPagamento.setCvv(pagamentoDTO.getCvv());
            nuovoPagamento.setImporto(pagamentoDTO.getImporto());
            nuovoPagamento.setDataPagamento(LocalDateTime.now());
            nuovoPagamento.setStato(pagamentoDTO.getStato());

            ordineEsistente.setPagamento(nuovoPagamento);

            ordineEsistente.setPrezzoTotale(nuovoPagamento.getImporto().doubleValue());
        }

        ordineEsistente = ordineRepository.save(ordineEsistente);

        return todto(ordineEsistente);
    }

    @Override
    public OrdineDTO createOrdineWithPagamento(OrdineDTO ordineDTO, Long eventoId, Long tipoPostoId, int quantita) {
        Ordine ordine = toEntity(ordineDTO);
        ordine = ordineRepository.save(ordine);

        Pagamento pagamento = new Pagamento();
        pagamento.setOrdine(ordine);
        pagamento.setImporto(java.math.BigDecimal.valueOf(ordineDTO.getPrezzoTotale()));
        pagamento.setDataPagamento(java.time.LocalDateTime.now());
        pagamento.setStato(StatoPagamento.COMPLETED);

        pagamentoRepository.save(pagamento);

        ordine.setPagamento(pagamento);
        ordine = ordineRepository.save(ordine);

        return todto(ordine);
    }

    // -------------MAPPER INTERNO----------------//

    private OrdineDTO todto(Ordine ordine) {
        OrdineDTO dto = new OrdineDTO();
        dto.setId(ordine.getId());
        dto.setPagamento(ordine.getPagamento());
        dto.setDataCreazione(ordine.getDataCreazione());
        dto.setEmailProprietario(ordine.getEmailProprietario());
        dto.setPrezzoTotale(ordine.getPrezzoTotale());
        dto.setProprietarioId(
                ordine.getProprietario() != null ? ordine.getProprietario().getId() : null
        );
        return dto;
    }

    private Ordine toEntity(OrdineDTO dto) {
        Ordine ordine = new Ordine();
        ordine.setDataCreazione(dto.getDataCreazione());
        ordine.setEmailProprietario(dto.getEmailProprietario());
        ordine.setPrezzoTotale(dto.getPrezzoTotale());
        if (dto.getProprietarioId() != null) {
            Utente proprietario = new Utente();
            proprietario.setId(dto.getProprietarioId());
            ordine.setProprietario(proprietario);
        }
        if (dto.getPagamento() != null) {
            Pagamento pagamento = dto.getPagamento();
            pagamento.setOrdine(ordine);
            ordine.setPagamento(pagamento);
        }
        return ordine;
    }
}
