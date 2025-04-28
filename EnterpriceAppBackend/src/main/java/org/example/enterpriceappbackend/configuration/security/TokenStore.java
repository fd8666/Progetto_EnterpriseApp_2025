//package org.example.enterpriceappbackend.configuration.security;
//
//
//import jakarta.annotation.PostConstruct;
//import lombok.Getter;
//import org.example.enterpriceappbackend.data.entity.Utente;
//import org.example.enterpriceappbackend.data.repository.UtenteRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.util.Map;
//import java.util.Optional;
//
//@Component
//public class TokenStore {
//
//    @Value("${jwt.secret}")
//    private String secretKey;
//
//    @Getter
//    private static TokenStore instance;
//
//    private final UtenteRepository utenteRepository;
//
//    public TokenStore(UtenteRepository utenteRepository){this.utenteRepository = utenteRepository;}
//
//    @PostConstruct
//    public void init(){
//        instance = this;
//        if(secretKey == null){
//            throw new IllegalArgumentException("la chiave segreta di JWT non è implementata");
//        }
//    }
//
//    public String creazioneToken(Map<String,Object> obj) throws Exception{
//        return "";
//    }
//
//    public String getEmailUtente(String token) throws Exception{
//        return token;
//    }
//
//    public Optional<Utente> getUtente(String token) throws Exception{
//        return Optional.empty();
//    }
//
//    public Boolean Valido(String token) throws Exception{
//        return false;
//    }
//
//    /* metodo per restituire il token .... getToken */
//    /* metodo per estrarre il token .... TokenEstratto */
//}
