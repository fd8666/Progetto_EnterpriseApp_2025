package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.repository.WishlistCondivisaRepository;
import org.example.enterpriceappbackend.data.repository.WishlistRepository;
import org.example.enterpriceappbackend.data.service.UtenteService;
import org.example.enterpriceappbackend.data.service.WishlistCondivisaService;
import org.example.enterpriceappbackend.dto.WishlistCondivisaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class WishlistCondivisaServiceImp implements WishlistCondivisaService{
    private final WishlistCondivisaRepository repository;
    public final WishlistRepository whishlistRepository;
    private final UtenteService utenteService;


    @Override
    @Transactional
    public List<WishlistCondivisaDTO> findByWishlistId(Long id) {
        return List.of();
    }

    @Override
    public void condividi(Long wishlistId) {

    }

    @Override
    public void rimuoviCondivisione(Long wishlistId, Long utenteId) {

    }
}
