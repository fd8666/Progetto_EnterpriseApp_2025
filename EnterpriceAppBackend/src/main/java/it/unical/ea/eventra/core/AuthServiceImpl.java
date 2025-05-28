package it.unical.ea.eventra.core;

import lombok.extern.slf4j.Slf4j;
import it.unical.ea.eventra.data.constants.Role;
import it.unical.ea.eventra.data.entity.Utente;
import it.unical.ea.eventra.data.service.UtenteService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UtenteService utenteService;

    public AuthServiceImpl(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    public OAuth2User createOAuth2UserWithAuthorities(Map<String, Object> attributes, String email, String nameAttributeKey) {
        Utente utente = utenteService.getOrCreateUser(email, attributes);
        log.info("Utente trovato/creato nel database: {} con ruoli: {}", utente.getEmail(), utente.getRole());

        Collection<GrantedAuthority> authorities = convertRolesToAuthorities(utente.getRole());

        log.info("Autorità generate per l'utente: {}", authorities);
        return new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
    }

    private Collection<GrantedAuthority> convertRolesToAuthorities(Role ruolo) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String nomeRuolo = ruolo.name();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + nomeRuolo));
        return authorities;
    }
}
