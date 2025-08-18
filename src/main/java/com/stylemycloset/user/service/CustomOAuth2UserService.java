package com.stylemycloset.user.service;

import com.stylemycloset.security.dto.data.OAuth;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    String registrationId = userRequest.getClientRegistration()
        .getRegistrationId(); //google,kakao 둘 중 하나
    String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
        .getUserInfoEndpoint().getUserNameAttributeName();
    OAuth attributes = OAuth.of(registrationId, userNameAttributeName,
        oAuth2User.getAttributes());

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    User user;

    if (authentication != null && authentication.isAuthenticated()
        && !(authentication.getPrincipal() instanceof String)) {
      String currentUsername = authentication.getName();
      user = userRepository.findByEmail(currentUsername)
          .orElseThrow(() -> new OAuth2AuthenticationException("현재 로그인된 사용자를 찾을 수 없습니다."));

      user.addOAuthProvider(registrationId);
      userRepository.save(user);

    } else {
      user = saveOrUpdate(attributes, registrationId);
    }

    return new DefaultOAuth2User(
        Collections.singleton(new SimpleGrantedAuthority(user.getRole().toString())),
        attributes.getAttributes(),
        attributes.getNameAttributeKey());
  }

  private User saveOrUpdate(OAuth attributes, String provider) {
    Optional<User> userOptional = userRepository.findByEmail(attributes.getEmail());

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      List<String> providers = user.getLinkedOAuthProviders();
      if (providers != null && !providers.contains(provider)) {
        user.addOAuthProvider(provider);
        return userRepository.save(user);
      }
      return user;
    } else {
      String tempPassword = passwordEncoder.encode(UUID.randomUUID().toString());
      User newUser = new User(attributes.getName(), attributes.getEmail(), tempPassword, provider);
      return userRepository.save(newUser);
    }
  }
}