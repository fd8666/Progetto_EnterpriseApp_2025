package it.unical.ea.eventra.data.service.serviceImpl;

import it.unical.ea.eventra.dto.StrutturaInfoOrganizzatoreDTO;
import it.unical.ea.eventra.dto.StrutturaInfoUtenteDTO;
import it.unical.ea.eventra.dto.StrutturaMapInfoDTO;
import lombok.RequiredArgsConstructor;
import it.unical.ea.eventra.data.entity.Struttura;
import it.unical.ea.eventra.data.repository.StrutturaRepository;
import it.unical.ea.eventra.data.repository.specification.StrutturaSpecification;
import it.unical.ea.eventra.data.service.StrutturaService;
import it.unical.ea.eventra.exception.BadRequest;
import it.unical.ea.eventra.exception.NotFound;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class StrutturaServiceImpl implements StrutturaService {

    private final StrutturaRepository strutturaRepository;

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequest(" ID non valido ");
        }
    }


    @Override
    public Struttura getById(Long id) {
        validateId(id);
        return strutturaRepository.findById(id)
                .orElseThrow(() -> new NotFound("Struttura non trovata con ID: " + id));
    }

    @Override
    public List<Struttura> getByNome(String nome) {

        if (nome.length() < 3) {
            throw new BadRequest("Il nome da cercare deve contenere almeno 3 caratteri.");
        }
        List<Struttura> s = strutturaRepository.findByNomeContainingIgnoreCase(nome);
        if(s.isEmpty()) {
            throw new NotFound("Struttura non trovata con nome " + nome);
        }

        return s;

    }

    @Override
    public List<Struttura> getByIndirizzo(String indirizzo) {

        if(indirizzo.length() < 5) {
            throw new BadRequest("L'indirizzo da cercare deve contenere almeno 5 caratteri.");
        }
        List<Struttura> s = strutturaRepository.findByIndirizzoContainingIgnoreCase(indirizzo);
        if(s.isEmpty()) {
            throw new NotFound("Nessuna struttura all'indirizzo " + indirizzo);
        }

        return s;

    }

    @Override
    public List<Struttura> getByCategoria(String categoria) { //non ho bisono di controllare la stringa perche viene passata completa dal menu a tendina ipotetico

        List<Struttura> s = strutturaRepository.findByCategoria(categoria);
        if (s.isEmpty()) {
            throw new NotFound("Nessuna struttura trovata nella categoria: " + categoria);
        }

        return s;

    }

    @Override
    public Long countByCategoria(String categoria) {

        long count = strutturaRepository.countByCategoria(categoria);
        if (count == 0) {
            throw new NotFound("Nessuna struttura nella categoria: " + categoria);
        }
        return count;
    }

    @Override
    public List<StrutturaInfoOrganizzatoreDTO> filtraStrutture(String nome, String categoria, String indirizzo) {

        if (nome != null && nome.length() < 3) {
            throw new BadRequest("Il nome da cercare deve contenere almeno 3 caratteri.");
        }
        if (indirizzo != null && indirizzo.length() < 5) {
            throw new BadRequest("L'indirizzo da cercare deve contenere almeno 5 caratteri.");        }

        Specification<Struttura> spec = Specification.where(null);

        if (nome != null && !nome.trim().isEmpty()) {
            spec = spec.and(StrutturaSpecification.hasNome(nome));
        }
        if (categoria != null && !categoria.trim().isEmpty()) {
            spec = spec.and(StrutturaSpecification.hasCategoria(categoria));
        }
        if (indirizzo != null && !indirizzo.trim().isEmpty()) {
            spec = spec.and(StrutturaSpecification.hasIndirizzo(indirizzo));
        }

        List<Struttura> risultati = strutturaRepository.findAll(spec);
        if (risultati.isEmpty()) {
            throw new NotFound("Nessuna struttura trovata con i criteri forniti.");
        }

        return risultati.stream().map(this::toOrganizzatoreDTO).toList();

    }

    @Override
    public StrutturaInfoUtenteDTO getInfoForUtenteById(Long id) {

        validateId(id);
        Struttura struttura = strutturaRepository.findById(id)
                .orElseThrow(() -> new NotFound("Nessuna struttura disponibile con l'ID specificato: " + id));

        return toUtenteDTO(struttura);

    }

    @Override
    public StrutturaInfoOrganizzatoreDTO getOrganizzatoreDTO(Long id) {

        validateId(id);
        Struttura struttura = strutturaRepository.findById(id)
                .orElseThrow(() -> new NotFound("Struttura non trovata. Verifica l'ID inserito: " + id));

        return toOrganizzatoreDTO(struttura);

    }

    @Override
    public StrutturaMapInfoDTO getMapInfoById(Long id) {

        validateId(id);
        Struttura struttura = strutturaRepository.findById(id)
                .orElseThrow(() -> new NotFound("Impossibile recuperare le coordinate: struttura con ID " + id + " non esistente."));

        return toMapDTO(struttura);

    }

    @Override
    public List<String> getAllCategorie() {

        List<String> categorie = strutturaRepository.findAllCategorieDistinct();
        if (categorie == null || categorie.isEmpty()) {
            throw new NotFound("Errore nel recupero delle categorie o nessuna categoria disponibile.");
        }

        return categorie;

    }

    @Override
    public StrutturaInfoUtenteDTO getStrutturaByEvento(Long eventoId) {

        validateId(eventoId);
        Struttura struttura = strutturaRepository.findByEventoId(eventoId);
        if (struttura == null) {
            throw new NotFound("Nessuna struttura trovata per l'evento con ID: " + eventoId);
        }

        return toUtenteDTO(struttura);

    }

    @Override
    public List<StrutturaInfoOrganizzatoreDTO> getAllForOrganizzatore() {

        List<Struttura> strutture = strutturaRepository.findAll();
        if (strutture.isEmpty()) {
            throw new NotFound("Nessuna struttura disponibile.");
        }
        return strutture.stream().map(this::toOrganizzatoreDTO).toList();

    }

    private StrutturaInfoUtenteDTO toUtenteDTO(Struttura struttura) {
        StrutturaInfoUtenteDTO dto = new StrutturaInfoUtenteDTO();
        dto.setId(struttura.getId());
        dto.setNome(struttura.getNome());
        dto.setDescrizione(struttura.getDescrizione());
        dto.setImmagine(struttura.getImmagine());
        return dto;
    }

    private StrutturaMapInfoDTO toMapDTO(Struttura struttura) {
        StrutturaMapInfoDTO dto = new StrutturaMapInfoDTO();
        dto.setId(struttura.getId());
        dto.setCoordinateLatitude(struttura.getCoordinateLatitude());
        dto.setCoordinateLongitude(struttura.getCoordinateLongitude());
        return dto;
    }

    private StrutturaInfoOrganizzatoreDTO toOrganizzatoreDTO(Struttura struttura) {
        StrutturaInfoOrganizzatoreDTO dto = new StrutturaInfoOrganizzatoreDTO();
        dto.setId(struttura.getId());
        dto.setNome(struttura.getNome());
        dto.setCategoria(struttura.getCategoria());
        dto.setDescrizione(struttura.getDescrizione());
        dto.setImmagine(struttura.getImmagine());
        dto.setIndirizzo(struttura.getIndirizzo());
        return dto;
    }

}
