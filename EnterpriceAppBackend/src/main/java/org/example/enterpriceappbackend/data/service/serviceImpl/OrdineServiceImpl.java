package org.example.enterpriceappbackend.data.service.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Ordine;
import org.example.enterpriceappbackend.data.repository.BigliettoRepository;
import org.example.enterpriceappbackend.data.repository.OrdineRepository;
import org.example.enterpriceappbackend.data.repository.PagamentoRepository;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.example.enterpriceappbackend.data.service.OrdineService;
import org.example.enterpriceappbackend.dto.OrdineDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdineServiceImpl implements OrdineService {

    private final OrdineRepository ordineRepository;
    private final UtenteRepository utenteRepository;
    private final PagamentoRepository pagamentoRepository;
    private final BigliettoRepository bigliettoRepository;

    @Override
    @Transactional
    public Ordine findById(long id) {
        return null;
    }

    @Override
    public List<OrdineDTO> findByProprietario(Long proprietarioId){
        return List.of();
    }

    @Override
    public Long save(OrdineDTO ordineDTO){
        Ordine ordine = new Ordine();
        return ordine.getId();
    }

    @Override
    public OrdineDTO updateOrdineProdotti(Long id, OrdineDTO ordineDTO) {
        return null;
    }

    @Override
    public void delete(Long id){}

}
