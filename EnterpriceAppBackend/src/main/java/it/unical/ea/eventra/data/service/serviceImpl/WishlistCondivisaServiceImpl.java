package it.unical.ea.eventra.data.service.serviceImpl;

import it.unical.ea.eventra.data.entity.Utente;
import it.unical.ea.eventra.data.entity.Wishlist;
import it.unical.ea.eventra.data.entity.WishlistCondivisa;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.data.constants.Role;
import it.unical.ea.eventra.data.repository.WishlistCondivisaRepository;
import it.unical.ea.eventra.data.repository.WishlistRepository;
import it.unical.ea.eventra.data.service.UtenteService;
import it.unical.ea.eventra.data.service.WishlistCondivisaService;
import it.unical.ea.eventra.dto.UtenteDTO;
import it.unical.ea.eventra.dto.WishlistCondivisaDTO;
import it.unical.ea.eventra.exception.NotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistCondivisaServiceImpl implements WishlistCondivisaService {

    private final WishlistCondivisaRepository repository;
    private final WishlistRepository wishlistRepository;
    private final UtenteService utenteService;

    @Override
    @Transactional(readOnly = true)
    public List<WishlistCondivisaDTO> findByWishlistId(Long id) {
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new NotFound("Wishlist non trovata"));

        return repository.findByWishlist(wishlist).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void condividi(Long wishlistId) {
        //Non Implemantata
        throw new UnsupportedOperationException("Utilizzare il metodo create invece");
    }

    @Override
    @Transactional
    public void rimuoviCondivisione(Long wishlistId, Long utenteId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new NotFound("Wishlist non trovata"));

        Utente utente = convertToEntity(utenteService.getById(utenteId));

        repository.deleteByWishlistAndCondivisaCon(wishlist, utente);
    }

    @Override
    @Transactional
    public WishlistCondivisaDTO create(WishlistCondivisaDTO wishlistCondivisaDTO) {
        Wishlist wishlist = wishlistRepository.findById(wishlistCondivisaDTO.getWishlistId())
                .orElseThrow(() -> new NotFound("Wishlist non trovata"));

        UtenteDTO utenteDTO = utenteService.getById(wishlistCondivisaDTO.getUserId());
        Utente utente = convertToEntity(utenteDTO);

        WishlistCondivisa condivisione = new WishlistCondivisa();
        condivisione.setWishlist(wishlist);
        condivisione.setCondivisaCon(utente);

        WishlistCondivisa saved = repository.save(condivisione);
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public void rimuoviTutteCondivisioni(Long wishlistId) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new NotFound("Wishlist non trovata"));

        repository.deleteByWishlist(wishlist);
    }

    private WishlistCondivisaDTO convertToDTO(WishlistCondivisa condivisione) {
        WishlistCondivisaDTO dto = new WishlistCondivisaDTO();
        dto.setId(condivisione.getId());
        dto.setWishlistId(condivisione.getWishlist().getId());
        dto.setUserId(condivisione.getCondivisaCon().getId());
        return dto;
    }

    private Utente convertToEntity(UtenteDTO dto) {
        Utente utente = new Utente();
        utente.setId(dto.getId());
        utente.setNome(dto.getNome());
        utente.setCognome(dto.getCognome());
        utente.setNumeroTelefono(dto.getNumerotelefono());
        utente.setEmail(dto.getEmail());
        utente.setPassword(dto.getPassword());
        utente.setRole(Role.valueOf(dto.getRole()));
        return utente;
    }
}