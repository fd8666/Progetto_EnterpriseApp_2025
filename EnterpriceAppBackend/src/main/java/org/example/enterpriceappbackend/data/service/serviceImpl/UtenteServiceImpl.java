package org.example.enterpriceappbackend.data.service.serviceImpl;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
//import org.example.enterpriceappbackend.configuration.security.TokenStore;
import org.example.enterpriceappbackend.configuration.ApiResponseConfiguration;
import org.example.enterpriceappbackend.configuration.security.TokenStore;
import org.example.enterpriceappbackend.data.constants.Messaggi;
import org.example.enterpriceappbackend.data.constants.Role;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;
import org.example.enterpriceappbackend.data.service.UtenteService;
import org.example.enterpriceappbackend.dto.UtenteDTO;
//import org.example.enterpriceappbackend.dto.UtenteRegistrazioneDTO;
import org.example.enterpriceappbackend.dto.UtenteLoginDTO;
import org.example.enterpriceappbackend.dto.UtenteRegistrazioneDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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

public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository utenteRepository;
    private final TokenStore tokenStore;
    private final PasswordEncoder passwordEncoder;
    private final Messaggi messaggi;
    private final JavaMailSender mailSender;


    @Override
    public void save(Utente utente) {
        utenteRepository.save(utente);
    }

    @Override
    public ResponseEntity<?> RegistrazioneUtente(UtenteRegistrazioneDTO utenteRegistrazione) throws Exception {

        if (utenteRepository.existsByEmail(utenteRegistrazione.getCredenzialiEmail())) {
            return new ResponseEntity<>("L'email è già in uso", HttpStatus.BAD_REQUEST);
        }

        UtenteDTO nuovoUtente = new UtenteDTO();
        nuovoUtente.setNome(utenteRegistrazione.getNome());
        nuovoUtente.setCognome(utenteRegistrazione.getCognome());
        nuovoUtente.setEmail(utenteRegistrazione.getCredenzialiEmail());
        nuovoUtente.setPassword(passwordEncoder.encode(utenteRegistrazione.getCredenzialiPassword()));  // Assicurati di usare un encoder per la password
        nuovoUtente.setRole("USER");
        Utente utenteSalvato = utenteRepository.save(toEntity(nuovoUtente));

        String token = tokenStore.creaToken(Map.of("email", utenteSalvato.getEmail()));

        return new ResponseEntity<>(new ApiResponseConfiguration<>(true, "Registrazione completata con successo", token), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> AutenticazioneUtente(UtenteLoginDTO utenteLoginDTO) throws JOSEException {
        Optional<Utente> optionalUtente = utenteRepository.findByEmail(utenteLoginDTO.getCredenzialiEmail());

        if(optionalUtente.isPresent()){
            Utente utente = optionalUtente.get();

            if(passwordEncoder.matches(utenteLoginDTO.getCredenzialiPassword(),utente.getPassword())){
                String token = tokenStore.creaToken(Map.of("email", utente.getEmail()));
                return new ResponseEntity<>(new ApiResponseConfiguration<>(true,"autenticazione riuscita", token),HttpStatus.OK);
            }else{
                return new ResponseEntity<>("Credenziali non valide", HttpStatus.UNAUTHORIZED);
            }
        }else{
            return new ResponseEntity<>("utente non trovato", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public UtenteDTO getUtenteByToken(String token) throws Exception {
        Optional<Utente> utente = tokenStore.getUtenteDaToken(token);
        if(utente.isPresent()){
            return toDto(utente.get());
        }
        throw new Exception("utente non trovato! ");
    }

    @Override
    public void AggiornaPassword(String token, String newPassword) throws Exception {
        String email = tokenStore.getEmailDaToken(token);
        Optional<Utente> optionalUtente = utenteRepository.findByEmail(email);

        if (optionalUtente.isEmpty()) {
            throw new Exception("Utente non trovato");
        }

        Utente utente = optionalUtente.get();
        utente.setPassword(passwordEncoder.encode(newPassword));
        utenteRepository.save(utente);
    }

    @Override
    public List<Utente> getAllUtenti() {
        return utenteRepository.findAll();
    }

    @Override
    public void deleteUtente(Long id) {
        utenteRepository.deleteById(id);
    }

    @Override
    public void AggiornaLaPasswordTramiteEmail(String email) {
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
        }
    }

    public void sendEmail(String to, String soggetto, String testo){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(soggetto);
        mailMessage.setText(testo);
        mailSender.send(mailMessage);
    }


    public UserDetails loadUtenteByUsername(String email) throws UsernameNotFoundException {
        Optional<Utente> utente = utenteRepository.findByEmail(email);
        if(utente.isPresent()) {
            Utente utenteDB = utente.get();
            List<SimpleGrantedAuthority> authorities;
            if (utenteDB.getRole().equals(Role.ADMIN)) {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            }
            return new User(utenteDB.getEmail(), utenteDB.getPassword(), authorities);
        }
        throw new UsernameNotFoundException("User not found");
    }

    // ---------- MAPPER INTERNO ----------------//

    private UtenteDTO toDto(Utente utente){
        UtenteDTO dto = new UtenteDTO();
        dto.setId(utente.getId());
        dto.setEmail(utente.getEmail());
        dto.setPassword(utente.getPassword());
        dto.setNome(utente.getNome());
        dto.setCognome(utente.getCognome());
        dto.setRole(String.valueOf(utente.getRole()));

        return dto;
    }

    private Utente toEntity(UtenteDTO dto){
        Utente utente = new Utente();

        utente.setEmail(dto.getEmail());
        utente.setPassword(dto.getPassword());
        utente.setNome(dto.getNome());
        utente.setCognome(dto.getCognome());
        utente.setRole(Role.valueOf(dto.getRole()));

        return utente;
    }
}
