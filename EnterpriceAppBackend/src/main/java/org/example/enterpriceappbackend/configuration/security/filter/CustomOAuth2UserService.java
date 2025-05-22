package org.example.enterpriceappbackend.configuration.security.filter;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.CoreService.AuthService;
import org.example.enterpriceappbackend.data.service.UtenteService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Lazy
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AuthService authService;
    private final UtenteService utenteService;


    //Serve per Caricare l'utente della richiesta OAuth fatta (Supporta Google e Git)
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        String email = (String) attributes.get("email");
        utenteService.getOrCreateUser(email, attributes);
        if (email == null && registrationId.equals("github")) {
            email = attributes.get("login") + "@github.local";
        }

        String nome = (String) attributes.getOrDefault("given_name", "Utente");
        String cognome = (String) attributes.getOrDefault("family_name", "OAuth");
        if (registrationId.equals("github")) {
            String fullName = (String) attributes.get("name");
            if (fullName != null) {
                String[] parts = fullName.split(" ");
                nome = parts.length > 0 ? parts[0] : "Git";
                cognome = parts.length > 1 ? parts[1] : "Hub";
            }
        }

        return authService.createOAuth2UserWithAuthorities(attributes, email, userNameAttributeName);
    }
}
