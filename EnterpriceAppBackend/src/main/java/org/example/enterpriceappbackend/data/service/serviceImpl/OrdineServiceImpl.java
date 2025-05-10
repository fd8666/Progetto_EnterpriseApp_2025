package org.example.enterpriceappbackend.data.service.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.constants.StatoPagamento;
import org.example.enterpriceappbackend.data.entity.Ordine;
import org.example.enterpriceappbackend.data.entity.Pagamento;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.repository.OrdineRepository;
import org.example.enterpriceappbackend.data.repository.PagamentoRepository;
import org.example.enterpriceappbackend.data.service.OrdineService;
import org.example.enterpriceappbackend.dto.OrdineDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdineServiceImpl implements OrdineService {

    private final OrdineRepository ordineRepository;
    private final PagamentoRepository pagamentoRepository;

    @Override
    @Transactional
    public Ordine findById(long id) {return ordineRepository.findById(id).orElseThrow(() -> new RuntimeException("Ordine non trovato con id: " + id));}

    @Override
    public List<OrdineDTO> findByProprietario(Long proprietarioId) {
        List<Ordine> ordini = ordineRepository.findByProprietarioId(proprietarioId);
        return ordini.stream().map(this::todto).toList();
    }

    @Override
    public Long save(OrdineDTO ordineDTO) {
        Ordine ordine = toEntity(ordineDTO);
        ordine = ordineRepository.save(ordine);
        return ordine.getId();
    }

    @Override
    public void delete(Long id) {ordineRepository.deleteById(id);}

    @Override
    public OrdineDTO update(Long id, OrdineDTO ordineDTO) {
        Ordine ordineEsistente = ordineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato con id: " + id));

        ordineEsistente.setPagamenti(ordineDTO.getPagamenti());
        ordineEsistente.setDataCreazione(ordineDTO.getDataCreazione());
        ordineEsistente.setEmailProprietario(ordineDTO.getEmailProprietario());
        ordineEsistente.setPrezzoTotale(ordineDTO.getPrezzoTotale());

        if (ordineDTO.getProprietarioId() != null) {
            Utente proprietario = new Utente();
            proprietario.setId(ordineDTO.getProprietarioId());
            ordineEsistente.setProprietario(proprietario);
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

        ordine.getPagamenti().add(pagamento);
        ordine = ordineRepository.save(ordine);

        return todto(ordine);
    }

    // -------------MAPPER INTERNO----------------//

    private OrdineDTO todto(Ordine ordine){
        OrdineDTO dto = new OrdineDTO();
        dto.setId(ordine.getId());
        dto.setPagamenti(ordine.getPagamenti());
        dto.setDataCreazione(ordine.getDataCreazione());
        dto.setEmailProprietario(ordine.getEmailProprietario());
        dto.setPrezzoTotale(ordine.getPrezzoTotale());
        dto.setProprietarioId(ordine.getProprietario().getId());

        return dto;
    }

    private Ordine toEntity(OrdineDTO dto){
        Ordine ordine = new Ordine();
        ordine.setPagamenti(dto.getPagamenti());
        ordine.setDataCreazione(dto.getDataCreazione());
        ordine.setEmailProprietario(dto.getEmailProprietario());
        ordine.setPrezzoTotale(dto.getPrezzoTotale());
        if (dto.getProprietarioId() != null){
            Utente proprietario = new Utente();
            proprietario.setId(dto.getProprietarioId());
            ordine.setProprietario(proprietario);
        }

        return ordine;
    }
}
