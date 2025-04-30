package org.example.enterpriceappbackend.data.service.serviceImpl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.constants.Visibilita;
import org.example.enterpriceappbackend.data.entity.Evento;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.entity.Wishlist;
import org.example.enterpriceappbackend.data.repository.EventoRepository;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.example.enterpriceappbackend.data.repository.WishlistRepository;
import org.example.enterpriceappbackend.data.service.WishlistService;
import org.example.enterpriceappbackend.dto.WishlistDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {


    private final WishlistRepository wishlistRepository;
    private final UtenteRepository utenteRepository;
    private final EventoRepository eventoRepository;


    @Override
    @Transactional(readOnly = true)
    public Optional<WishlistDTO> findById(Long id) {
        return wishlistRepository.findById(id)
                .map(this::toDto);
    }

    @Override
    public List<WishlistDTO> findByAll() {
        return wishlistRepository.findAll().stream().map(this::toDto).collect(toList());
    }

    @Override
    public List<WishlistDTO> findByUtente(Long utenteId) {
        return wishlistRepository.findByUtenteId(utenteId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WishlistDTO> findByUtenteAndVisibilita(Long utenteId, Visibilita visibilita) {
        return wishlistRepository.findByUtenteIdAndVisibilita(utenteId, visibilita)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public WishlistDTO create(WishlistDTO wishlistDTO) {
        // Verifica se l'utente ha già una wishlist attiva
        List<Wishlist> existing = wishlistRepository.findByUtenteId(wishlistDTO.getUtenteId());
        if (!existing.isEmpty()) {
            throw new RuntimeException("L'utente ha già una wishlist.");
        }
        Wishlist wishlist = toEntity(wishlistDTO);
        wishlist.setDataCreazione(LocalDateTime.now());
        Wishlist saved = wishlistRepository.save(wishlist);
        return toDto(saved);
    }




    @Override
    @Transactional
    public WishlistDTO save(WishlistDTO wishlistDTO) {
        Wishlist wishlist = toEntity(wishlistDTO);
        Wishlist saved = wishlistRepository.save(wishlist);
        return toDto(saved);
    }


    @Override
    @Transactional
    public WishlistDTO update(Long id, WishlistDTO wishlistDTO) {
        // Trova la wishlist esistente
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist non trovata con id: " + id));

        // Aggiorna le proprietà della wishlist
        wishlist.setVisibilita(wishlistDTO.getVisibilita());
        wishlist.setDataCreazione(wishlistDTO.getDataCreazione());
        List<Evento> eventi = eventoRepository.findAllById(wishlistDTO.getEventi());
        wishlist.setEventi(eventi);
        wishlist = wishlistRepository.save(wishlist);
        return toDto(wishlist);
    }




    @Override
    @Transactional
    public void deleteById(Long id) {
        // Trova la wishlist esistente
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist non trovata con id: " + id));
        Utente utente = wishlist.getUtente();
        utente.setWishlist(null);
        utenteRepository.save(utente);
        wishlist.getEventi().clear();
        wishlistRepository.delete(wishlist);
    }

    private Wishlist toEntity(WishlistDTO dto) {
        Wishlist wishlist = new Wishlist();
        wishlist.setId(dto.getId());


        Utente utente = utenteRepository.findById(dto.getUtenteId())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        wishlist.setUtente(utente);

        wishlist.setVisibilita(dto.getVisibilita());
        wishlist.setDataCreazione(dto.getDataCreazione());
        List<Evento> eventi = eventoRepository.findAllById(dto.getEventi());
        wishlist.setEventi(eventi);
        return wishlist;
    }



    private WishlistDTO toDto(Wishlist wishlist) {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(wishlist.getId());
        dto.setUtenteId(wishlist.getUtente().getId());
        dto.setVisibilita(wishlist.getVisibilita());
        dto.setDataCreazione(wishlist.getDataCreazione());
        List<Long> eventiIds = wishlist.getEventi().stream()
                .map(Evento::getId)
                .collect(Collectors.toList());
        dto.setEventi(eventiIds);

        return dto;
    }


}
