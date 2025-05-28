package it.unical.ea.eventra.data.service.serviceImpl;



import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.core.EmailService;
import it.unical.ea.eventra.data.entity.Ordine;
import it.unical.ea.eventra.data.entity.Pagamento;
import it.unical.ea.eventra.data.repository.OrdineRepository;
import it.unical.ea.eventra.data.repository.PagamentoRepository;
import it.unical.ea.eventra.data.service.PagamentoService;
import it.unical.ea.eventra.dto.PagamentoDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PagamentoServiceImpl implements PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final OrdineRepository ordineRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public PagamentoDTO createPagamento(Long ordineId, PagamentoDTO pagamentoDTO) {
        Optional<Ordine> ordineOpt = ordineRepository.findById(ordineId);

        if (ordineOpt.isEmpty()) {
            throw new RuntimeException("Ordine non trovato con ID: " + ordineId);
        }

        Ordine ordine = ordineOpt.get();

        Pagamento pagamento = new Pagamento();
        pagamento.setNomeTitolare(pagamentoDTO.getNomeTitolare());
        pagamento.setCognomeTitolare(pagamentoDTO.getCognomeTitolare());
        pagamento.setNumeroCarta(pagamentoDTO.getNumeroCarta());
        pagamento.setScadenza(pagamentoDTO.getScadenza());
        pagamento.setCvv(pagamentoDTO.getCvv());
        pagamento.setImporto(pagamentoDTO.getImporto());
        pagamento.setDataPagamento(LocalDateTime.now());
        pagamento.setStato(pagamentoDTO.getStato());

        pagamento.setOrdine(ordine);
        ordine.setPagamento(pagamento);

        pagamento = pagamentoRepository.save(pagamento);

        emailService.sendOrdineConferma(pagamento);

        return todto(pagamento);
    }

    @Override
    public PagamentoDTO create(PagamentoDTO pagamentoDTO) {
        Pagamento pagamento = toEntity(pagamentoDTO);

        if (pagamentoDTO.getOrdineId() != null) {
            Ordine ordine = ordineRepository.findById(pagamentoDTO.getOrdineId())
                    .orElseThrow(() -> new RuntimeException("Ordine non trovato con id: " + pagamentoDTO.getOrdineId()));
            pagamento.setOrdine(ordine);
        }

        pagamento.setDataPagamento(LocalDateTime.now());
        pagamento = pagamentoRepository.save(pagamento);

        emailService.sendOrdineConferma(pagamento);
        return todto(pagamento);
    }

    @Override
    public List<PagamentoDTO> findByUtenteId(Long utenteId) {return pagamentoRepository.findAll().stream().filter(p -> p.getOrdine() != null && p.getOrdine().getProprietario() != null && p.getOrdine().getProprietario().getId().equals(utenteId)).map(this::todto).toList();}

    @Override
    public PagamentoDTO save(PagamentoDTO pagamentoDTO) {
        Pagamento pagamento = toEntity(pagamentoDTO);

        if(pagamentoDTO.getOrdineId() != null){
            Ordine ordine = ordineRepository.findById(pagamentoDTO.getOrdineId()).orElseThrow(()-> new RuntimeException("ordine non trovato con id: " + pagamentoDTO.getOrdineId()));
            pagamento.setOrdine(ordine);
        }

        pagamento.setDataPagamento(LocalDateTime.now());
        pagamento = pagamentoRepository.save(pagamento);

        return todto(pagamento);
    }

    @Override
    public PagamentoDTO update(Long id, PagamentoDTO pagamentoDTO) {
        Pagamento pagamentoEsistente = pagamentoRepository.findById(id).orElseThrow(()-> new RuntimeException("PAGAMENTO NON TROVATO CON ID: " + id));
        pagamentoEsistente.setImporto(pagamentoDTO.getImporto());

        if(pagamentoDTO.getOrdineId() != null){
            Ordine ordine = ordineRepository.findById(pagamentoDTO.getOrdineId()).orElseThrow(()-> new RuntimeException("ORDINE NON TROVATO CON ID: " + pagamentoDTO.getOrdineId()));
            pagamentoEsistente.setOrdine(ordine);
        }

        pagamentoEsistente = pagamentoRepository.save(pagamentoEsistente);

        return todto(pagamentoEsistente);

    }

    @Override
    public void deleteById(Long id){
        if(!pagamentoRepository.existsById(id)){
            throw new RuntimeException("PAGAMENTO NON TROVATO CON ID: " + id);
        }
        pagamentoRepository.deleteById(id);
    }

    //----------- MAPPER INTERNO -----------//

    private PagamentoDTO todto(Pagamento pagamento) {
        PagamentoDTO dto = new PagamentoDTO();
        dto.setId(pagamento.getId());
        dto.setNomeTitolare(pagamento.getNomeTitolare());
        dto.setCognomeTitolare(pagamento.getCognomeTitolare());
        dto.setNumeroCarta(pagamento.getNumeroCarta());
        dto.setScadenza(pagamento.getScadenza() != null ? pagamento.getScadenza() : null);
        dto.setCvv(pagamento.getCvv());
        dto.setImporto(pagamento.getImporto());
        dto.setDataPagamento(pagamento.getDataPagamento() != null ? pagamento.getDataPagamento() : null);
        dto.setStato(pagamento.getStato());
        if (pagamento.getOrdine() != null) {
            dto.setOrdineId(pagamento.getOrdine().getId());
        }
        dto.setBiglietti(pagamento.getBiglietti());

        return dto;
    }

    private Pagamento toEntity(PagamentoDTO dto) {
        Pagamento pagamento = new Pagamento();

        pagamento.setNomeTitolare(dto.getNomeTitolare());
        pagamento.setCognomeTitolare(dto.getCognomeTitolare());
        pagamento.setNumeroCarta(dto.getNumeroCarta());

        if (dto.getScadenza() != null) {
            pagamento.setScadenza((dto.getScadenza()));
        }

        pagamento.setCvv(dto.getCvv());
        pagamento.setImporto(dto.getImporto());

        if (dto.getDataPagamento() != null) {
            pagamento.setDataPagamento(dto.getDataPagamento());
        }

        pagamento.setStato(dto.getStato());
        pagamento.setBiglietti(dto.getBiglietti());

        if (dto.getOrdineId() != null) {
            Ordine ordine = ordineRepository.findById(dto.getOrdineId())
                    .orElseThrow(() -> new RuntimeException("Ordine non trovato con id: " + dto.getOrdineId()));
            pagamento.setOrdine(ordine);
        }

        return pagamento;
    }
}
