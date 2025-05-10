package org.example.enterpriceappbackend.data.service.serviceImpl;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Ordine;
import org.example.enterpriceappbackend.data.entity.Pagamento;
import org.example.enterpriceappbackend.data.repository.BigliettoRepository;
import org.example.enterpriceappbackend.data.repository.OrdineRepository;
import org.example.enterpriceappbackend.data.repository.PagamentoRepository;
import org.example.enterpriceappbackend.data.service.PagamentoService;
import org.example.enterpriceappbackend.dto.PagamentoDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PagamentoServiceImpl implements PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final BigliettoRepository bigliettoRepository;
    private final OrdineRepository ordineRepository;

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

    private PagamentoDTO todto(Pagamento pagamento){
        PagamentoDTO dto = new PagamentoDTO();
        dto.setId(pagamento.getId());
        dto.setNomeTitolare(pagamento.getNomeTitolare());
        dto.setCognomeTitolare(pagamento.getCognomeTitolare());
        dto.setScadenza(String.valueOf(pagamento.getScadenza()));
        dto.setCvv(pagamento.getCvv());
        dto.setImporto(pagamento.getImporto());
        dto.setDataPagamento(LocalDate.from(pagamento.getDataPagamento()));
        dto.setStato(pagamento.getStato());
        dto.setOrdineId(pagamento.getOrdine().getId());
        dto.setBiglietti(pagamento.getBiglietti());

        return dto;
    }

    private Pagamento toEntity(PagamentoDTO dto){
        Pagamento pagamento = new Pagamento();

        pagamento.setNomeTitolare(dto.getNomeTitolare());
        pagamento.setCognomeTitolare(dto.getCognomeTitolare());
        pagamento.setScadenza(LocalDateTime.parse(dto.getScadenza()));
        pagamento.setCvv(dto.getCvv());
        pagamento.setImporto(dto.getImporto());
        pagamento.setDataPagamento(LocalDateTime.from(dto.getDataPagamento()));
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
