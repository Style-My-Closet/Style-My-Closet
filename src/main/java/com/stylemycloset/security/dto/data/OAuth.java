package com.stylemycloset.security.dto.data;

import com.stylemycloset.common.exception.StyleMyClosetException;
import lombok.Builder;
import lombok.Getter;
import java.util.Map;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@Getter
public class OAuth {

  private final Map<String, Object> attributes;
  private final String nameAttributeKey;
  private final String name;
  private final String email;

  @Builder
  public OAuth(Map<String, Object> attributes, String nameAttributeKey, String name,
      String email) {
    this.attributes = attributes;
    this.nameAttributeKey = nameAttributeKey;
    this.name = name;
    this.email = email;
  }

  public static OAuth of(String registrationId, String userNameAttributeName,
      Map<String, Object> attributes) {
    if ("kakao".equals(registrationId)) {
      return ofKakao(userNameAttributeName, attributes);
    }
    return ofGoogle(userNameAttributeName, attributes);
  }

  private static OAuth ofGoogle(String userNameAttributeName,
      Map<String, Object> attributes) {
    return OAuth.builder()
        .name((String) attributes.get("name"))
        .email((String) attributes.get("email"))
        .attributes(attributes)
        .nameAttributeKey(userNameAttributeName)
        .build();
  }

  private static OAuth ofKakao(String userNameAttributeName,
      Map<String, Object> attributes) {
    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    if (kakaoAccount == null) {
      throw new OAuth2AuthenticationException("kakao_account가 없습니다.");
    }

    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
    if (profile == null) {
      throw new OAuth2AuthenticationException("profile이 없습니다.");
    }

    String nickname = (String) profile.get("nickname");
    if (nickname == null) {
      throw new OAuth2AuthenticationException("nickname이 없습니다.");
    }

    //카카오에서는 이메일 제공을 안해서 저렇게 닉네임 + kakao로 이메일 해놨습니다.
    String email = nickname + "@kakao.com";

    return OAuth.builder()
        .name(nickname)
        .email(email)
        .attributes(attributes)
        .nameAttributeKey(userNameAttributeName)
        .build();
  }
}