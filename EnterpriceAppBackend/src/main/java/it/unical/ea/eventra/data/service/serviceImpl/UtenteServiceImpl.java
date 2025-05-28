package it.unical.ea.eventra.data.service.serviceImpl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import it.unical.ea.eventra.core.EmailService;
import it.unical.ea.eventra.data.constants.Role;
import it.unical.ea.eventra.data.entity.Utente;
import it.unical.ea.eventra.data.repository.UtenteRepository;
import it.unical.ea.eventra.core.JwtService;
import it.unical.ea.eventra.data.service.UtenteService;
import it.unical.ea.eventra.dto.UtenteDTO;
import it.unical.ea.eventra.dto.RequestAuthentication;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtService jwtService;
    private final EmailService emailService;


    @Override
    public void save(Utente utente) {
        utenteRepository.save(utente);
    }

    @Override
    public void RegistrazioneUtente(UtenteDTO utenteDTO) throws Exception {

        if (utenteRepository.existsByEmail(utenteDTO.getEmail())){
            throw new IllegalArgumentException("L'email è già in uso");
        }

        Utente nuovoUtente = new Utente();
        nuovoUtente.setNome(utenteDTO.getNome());
        nuovoUtente.setCognome(utenteDTO.getCognome());
        nuovoUtente.setNumeroTelefono(utenteDTO.getNumerotelefono());
        nuovoUtente.setEmail(utenteDTO.getEmail());
        nuovoUtente.setPassword(passwordEncoder.encode(utenteDTO.getPassword()));
        nuovoUtente.setRole(Role.USER);

        emailService.sendRegistrazioneConferma(nuovoUtente);
        utenteRepository.save(nuovoUtente);
    }
    @Transactional
    @Override
    public Utente getOrCreateUser(String email, Map<String, Object> attributes) {
        log.info("Verifica utente con email (OAuth2): {}", email);

        Optional<Utente> optionalUtente = utenteRepository.findByEmail(email);

        String nomeOAuth = (attributes.get("given_name") != null) ? attributes.get("given_name").toString() :
                (attributes.get("name") != null) ? attributes.get("name").toString() : "Utente";

        String cognomeOAuth = (attributes.get("family_name") != null) ? attributes.get("family_name").toString() : "";

        Utente utente;
        boolean updated = false;

        if (optionalUtente.isPresent()) {
            utente = optionalUtente.get();

            if (utente.getNome() == null || utente.getNome().isEmpty()) {
                utente.setNome(nomeOAuth);
                updated = true;
            }

            if (utente.getCognome() == null || utente.getCognome().isEmpty()) {
                utente.setCognome(cognomeOAuth);
                updated = true;
            }

            if (utente.getRole() == null) {
                utente.setRole(Role.USER);
                updated = true;
            }

            if (updated) {
                utenteRepository.save(utente);
                log.info("Utente aggiornato con info da OAuth2: {}", email);
            } else {
                log.info("Utente esistente trovato senza modifiche: {}", email);
            }

        } else {
            // Crea nuovo utente
            utente = Utente.builder()
                    .email(email)
                    .nome(nomeOAuth)
                    .cognome(cognomeOAuth)
                    .password(passwordEncoder.encode(generateRandomPassword()))
                    .role(Role.USER)
                    .build();

            utenteRepository.save(utente);
            log.info("Nuovo utente creato da OAuth2: {} con nome: {}", email, nomeOAuth);
        }

        return utente;
    }

    private String generateRandomPassword() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[12];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }


    @Override
    public Utente AutenticazioneUtente(RequestAuthentication credenziali) {
        Utente utente = utenteRepository.findByEmail(credenziali.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Utente non trovato"));

        if (!passwordEncoder.matches(credenziali.getPassword(), utente.getPassword())) {
            throw new BadCredentialsException("Credenziali errate");
        }
        return utente;
    }

    @Override
    public UtenteDTO getById(Long id) {
        Utente utente = utenteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("Non esiste un utente con id: [%s]", id)));
        return toDto(utente);
    }

    @Override
    public void AggiornaPassword(String token, String newPassword) throws Exception {
        String email = jwtService.extractUsername(token);

        Utente utente = utenteRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Utente non trovato"));
        utente.setPassword(passwordEncoder.encode(newPassword));

        utenteRepository.save(utente);
    }

    @Override
    public List<Utente> getAllUtenti() {
        return utenteRepository.findAll();
    }

    @Override
    public void deleteUtente(Long id) {
        utenteRepository.deleteUtenteById(id);
    }

    @Override
    public String AggiornaLaPasswordTramiteEmail(String email) {
        Optional<Utente> optionalUtente = utenteRepository.findByEmail(email);

        if (optionalUtente.isPresent()) {
            Utente utente = optionalUtente.get();

            String nuovaPassword = "";
            if (!utente.getPassword().isEmpty()) {
                SecureRandom secureRandom = new SecureRandom();
                byte[] randomBytes = new byte[12];
                secureRandom.nextBytes(randomBytes);
                nuovaPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
                utente.setPassword(passwordEncoder.encode(nuovaPassword));
                utenteRepository.save(utente);
            }

            return nuovaPassword;
        }

        return null;
    }

    private UtenteDTO toDto(Utente utente){
        UtenteDTO dto = new UtenteDTO();
        dto.setId(utente.getId());
        dto.setNome(utente.getNome());
        dto.setCognome(utente.getCognome());
        dto.setNumerotelefono(utente.getNumeroTelefono());
        dto.setEmail(utente.getEmail());
        dto.setPassword(utente.getPassword());
        dto.setRole(String.valueOf(Role.USER));

        return dto;
    }

    private Utente toEntity(UtenteDTO dto) {
        Utente utente = new Utente();

        utente.setNome(dto.getNome());
        utente.setCognome(dto.getCognome());
        utente.setNumeroTelefono(dto.getNumerotelefono());
        utente.setEmail(dto.getEmail());
        utente.setPassword(dto.getPassword());
        utente.setRole(Role.valueOf(dto.getRole()));

        return utente;
    }

}
