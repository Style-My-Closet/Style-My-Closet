package com.stylemycloset.security;

import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

  private final UserRepository userRepository;

  public CustomAuthenticationProvider(UserRepository userRepository,
      PasswordEncoder passwordEncoder, ClosetUserDetailsService userDetailsService) {
    this.userRepository = userRepository;
    super.setPasswordEncoder(passwordEncoder);
    super.setUserDetailsService(userDetailsService);
  }

  @Override
  protected void additionalAuthenticationChecks(UserDetails userDetails,
      UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    try {
      super.additionalAuthenticationChecks(userDetails, authentication);
      return;
    } catch (BadCredentialsException e) {
      ClosetUserDetails closetUserDetails = (ClosetUserDetails) userDetails;
      log.info("로그인 이메일은", closetUserDetails.getEmail());

      User user = userRepository.findByEmail(closetUserDetails.getEmail())
          .orElseThrow(() -> new BadCredentialsException("인증 정보가 유효하지 않습니다."));

      if (user.getResetPasswordTime() != null && user.getResetPasswordTime()
          .isBefore(Instant.now())) {
        user.resetTempPassword(null, null);
        throw e;
      }

      if (user.getTempPassword() != null) {
        String presentedPassword = authentication.getCredentials().toString();
        if (getPasswordEncoder().matches(presentedPassword, user.getTempPassword())) {
          return;
        }
      }

      throw e;
    }
  }
}
