package org.example.enterpriceappbackend.data.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public interface AuthService {

    OAuth2User createOAuth2UserWithAuthorities(Map<String, Object> attributes, String email, String nameAttributeKey);
}
