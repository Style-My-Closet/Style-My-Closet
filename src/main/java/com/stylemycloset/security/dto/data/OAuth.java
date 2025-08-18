package com.stylemycloset.security.dto.data;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

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
    Map<String, Object> profile = (Map<String, Object>) attributes.get("profile");
    String nickname = (String) profile.get("nickname");

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