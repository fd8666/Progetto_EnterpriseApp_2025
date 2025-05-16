package org.example.enterpriceappbackend.data.service.serviceImpl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterpriceappbackend.data.constants.Messaggi;
import org.example.enterpriceappbackend.data.constants.Role;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.example.enterpriceappbackend.data.service.JwtService;
import org.example.enterpriceappbackend.data.service.UtenteService;
import org.example.enterpriceappbackend.dto.UtenteDTO;
import org.example.enterpriceappbackend.dto.RequestAuthentication;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final Messaggi messaggi;
    private final JavaMailSender mailSender;
    private final JwtService jwtService;


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

        utenteRepository.save(nuovoUtente);
    }

    @Override
    @Transactional
    public Utente getOrCreateUser(String email) {
        log.info("Cercando utente con email: {}", email);

        Optional<Utente> existingUser = utenteRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            Utente utente = existingUser.get();
            log.info("Utente esistente trovato: {} con ruolo: {}", email, utente.getRole());
            return utente;
        } else {
            log.info("Utente non trovato, creando nuovo utente con email: {}", email);

            Utente nuovoUtente = new Utente();
            nuovoUtente.setEmail(email);
            nuovoUtente.setPassword(passwordEncoder.encode("defaultPassword")); // Imposta una password predefinita
            nuovoUtente.setRole(Role.USER);
            nuovoUtente.setNome("Nuovo Utente"); // Imposta un nome di default se necessario

            Utente savedUser = utenteRepository.save(nuovoUtente);
            log.info("Nuovo utente salvato nel DB: {} con ID: {} e ruolo: {}",
                    savedUser.getEmail(), savedUser.getId(), savedUser.getRole());

            return savedUser;
        }
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

            String messaggio = messaggi.recuperoPassword(utente.getNome(), nuovaPassword);
            sendEmail(email, "Recupero Password", messaggio);

            return nuovaPassword;
        }

        return null;
    }


    public void sendEmail(String to, String soggetto, String testo){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(soggetto);
        mailMessage.setText(testo);
        mailSender.send(mailMessage);
    }

    // ---------- MAPPER INTERNO ----------------//

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
