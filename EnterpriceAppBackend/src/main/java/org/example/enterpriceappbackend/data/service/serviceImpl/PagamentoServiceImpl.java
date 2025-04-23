package org.example.enterpriceappbackend.data.service.serviceImpl;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.repository.PagamentoRepository;
import org.example.enterpriceappbackend.data.service.PagamentoService;
import org.example.enterpriceappbackend.dto.PagamentoDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PagamentoServiceImpl implements PagamentoService {

    private final PagamentoRepository pagamentoRepository;

    @Override
    public List<PagamentoDTO> findAll() {
        return List.of();
    }

    @Override
    public PagamentoDTO findById(Long id){
        return null;
    }

    @Override
    public List<PagamentoDTO> findByUtenteId(Long UtenteId) {
        return List.of();
    }

    @Override
    public PagamentoDTO save(PagamentoDTO pagamentoDTO) {
        return pagamentoDTO;
    }

    @Override
    public PagamentoDTO update(Long id, PagamentoDTO pagamentoDTO) {
        return pagamentoDTO;
    }

}
