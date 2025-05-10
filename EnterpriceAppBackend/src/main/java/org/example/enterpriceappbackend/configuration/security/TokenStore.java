package org.example.enterpriceappbackend.configuration.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.example.enterpriceappbackend.data.entity.Utente;
import org.example.enterpriceappbackend.data.repository.UtenteRepository;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenStore {

    @Value("${jwt.secret}")
    private String secretKey;

    private final UtenteRepository utenteRepository;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("La chiave segreta JWT deve essere definita e lunga almeno 32 caratteri.");
        }
    }

    public String creaToken(Map<String, Object> claims) throws JOSEException {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant notBefore = now.minus(5, ChronoUnit.MINUTES);
        Instant expiration = now.plus(7, ChronoUnit.HOURS);

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(notBefore))
                .expirationTime(Date.from(expiration));

        claims.forEach(builder::claim);

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), builder.build());
        signedJWT.sign(new MACSigner(secretKey.getBytes()));

        return signedJWT.serialize();
    }

    public boolean verificaToken(String token) {
        try {
            return isValido(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailDaToken(String token) throws ParseException, JOSEException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);  // Se il token è valido, non lancia eccezioni
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getStringClaim("email");  // o "sub" se usi subject
        } catch (Exception e) {
            System.out.println("TOKEN: invalido");
            e.printStackTrace();
            return null;
        }
    }

    public Optional<Utente> getUtenteDaToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (isValido(token)){
            String email = (String) signedJWT.getPayload().toJSONObject().get("email");
            return utenteRepository.findByEmail(email);
        }else{
            throw new RuntimeException("Invalid token");
        }
    }

    public Boolean isValido(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier jwsVerifier = new MACVerifier(secretKey.getBytes());
        if(signedJWT.verify(jwsVerifier)) {
            return new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime()) && new Date().after(signedJWT.getJWTClaimsSet().getNotBeforeTime());
        }
        return false;
    }


    public String estraiTokenDaRichiesta(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.replace("Bearer ","");
        }
        return "invalido";
    }

    public String estraiTokenDaRisposta(ResponseEntity<?> response) {
        String authorizationHeader = Objects.requireNonNull(response.getHeaders().get("Authorization")).get(0);
        return authorizationHeader.substring("Bearer ".length());
    }

}
