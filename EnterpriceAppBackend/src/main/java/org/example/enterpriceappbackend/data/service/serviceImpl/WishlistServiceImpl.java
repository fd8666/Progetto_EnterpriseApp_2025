package org.example.enterpriceappbackend.data.service.serviceImpl;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterpriceappbackend.data.constants.Visibilita;
import org.example.enterpriceappbackend.data.entity.Evento;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.entity.Wishlist;
import org.example.enterpriceappbackend.data.repository.EventoRepository;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.example.enterpriceappbackend.data.repository.WishlistCondivisaRepository;
import org.example.enterpriceappbackend.data.repository.WishlistRepository;
import org.example.enterpriceappbackend.data.service.WishlistService;
import org.example.enterpriceappbackend.dto.WishlistDTO;
import org.example.enterpriceappbackend.exceptions.BadRequest;
import org.example.enterpriceappbackend.exceptions.NotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UtenteRepository utenteRepository;
    private final EventoRepository eventoRepository;
    private final WishlistCondivisaRepository wishlistCondivisaRepository;

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequest("ID non valido");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WishlistDTO> findById(Long id) {
        validateId(id);
        return wishlistRepository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistDTO> findByAll() {
        return wishlistRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistDTO> findByUtente(Long utenteId) {
        validateId(utenteId);
        return wishlistRepository.findByUtenteId(utenteId).stream().map(this::toDto).collect(Collectors.toList());
    }
    @Transactional
    @Override
    public void removeEventoFromWishlist(Long wishlistId, Long eventoId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist non trovata"));

        Evento eventoToRemove = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EntityNotFoundException("Evento non trovato"));

        wishlist.getEventi().remove(eventoToRemove);

        wishlistRepository.save(wishlist);
    }


    @Transactional
    @Override
    public List<WishlistDTO> findCondiviseConUtente(Long utenteId) {
        validateId(utenteId);
        List<Wishlist> condivise = wishlistRepository.findByCondivisaCon(utenteId);
        return condivise.stream()
                .map(this::toDto)
                .toList();
    }


    @Override
    @Transactional
    public void addEventoToWishlist(Long wishlistId, Long eventoId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist non trovata con id: " + wishlistId));

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EntityNotFoundException("Evento non trovato con id: " + eventoId));

        wishlist.getEventi().add(evento);
        wishlistRepository.save(wishlist);
    }
    @Override
    @Transactional(readOnly = true)
    public List<WishlistDTO> findByUtenteAndVisibilita(Long utenteId, Visibilita visibilita) {
        validateId(utenteId);
        return wishlistRepository.findByUtenteIdAndVisibilita(utenteId, visibilita)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public WishlistDTO create(WishlistDTO dto) {
        validateId(dto.getUtenteId());

        if (!wishlistRepository.findByUtenteId(dto.getUtenteId()).isEmpty()) {
            throw new BadRequest("L'utente ha già una wishlist.");
        }

        Wishlist entity = toEntity(dto);
        entity.setDataCreazione(LocalDateTime.now());
        Wishlist saved = wishlistRepository.save(entity);
        log.info("Wishlist creata con ID {}", saved.getId());
        return toDto(saved);
    }

    @Override
    public WishlistDTO save(WishlistDTO dto) {
        Wishlist saved = wishlistRepository.save(toEntity(dto));
        log.info("Wishlist salvata con ID {}", saved.getId());
        return toDto(saved);
    }

    @Override
    public WishlistDTO update(Long id, WishlistDTO dto) {
        validateId(id);

        Wishlist existing = wishlistRepository.findById(id)
                .orElseThrow(() -> new NotFound("Wishlist non trovata con id: " + id));

        existing.setVisibilita(dto.getVisibilita());
        existing.setDataCreazione(dto.getDataCreazione());
        existing.setEventi(eventoRepository.findAllById(dto.getEventi()));

        Wishlist updated = wishlistRepository.save(existing);
        log.info("Wishlist aggiornata con ID {}", updated.getId());
        return toDto(updated);
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);

        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new NotFound("Wishlist non trovata con id: " + id));

        Utente utente = wishlist.getUtente();
        utente.setWishlist(null);
        utenteRepository.save(utente);

        wishlist.getEventi().clear();
        wishlistRepository.delete(wishlist);
        log.info("Wishlist eliminata con ID {}", id);
    }

    private Wishlist toEntity(WishlistDTO dto) {
        Wishlist wishlist = new Wishlist();
        wishlist.setId(dto.getId());

        Utente utente = utenteRepository.findById(dto.getUtenteId())
                .orElseThrow(() -> new NotFound("Utente non trovato con id: " + dto.getUtenteId()));
        wishlist.setUtente(utente);

        wishlist.setVisibilita(dto.getVisibilita());
        wishlist.setDataCreazione(dto.getDataCreazione());
        wishlist.setEventi(eventoRepository.findAllById(dto.getEventi()));

        return wishlist;
    }

    private WishlistDTO toDto(Wishlist wishlist) {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(wishlist.getId());
        dto.setUtenteId(wishlist.getUtente().getId());
        dto.setVisibilita(wishlist.getVisibilita());
        dto.setDataCreazione(wishlist.getDataCreazione());
        dto.setEventi(wishlist.getEventi().stream().map(Evento::getId).collect(Collectors.toList()));
        return dto;
    }
}
