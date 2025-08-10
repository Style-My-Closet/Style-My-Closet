package com.stylemycloset.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.common.controller.ErrorResponse;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.security.ClosetUserDetails;
import com.stylemycloset.security.SecurityMatchers;
import com.stylemycloset.security.dto.data.TokenInfo;
import com.stylemycloset.user.dto.data.UserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain
  ) throws IOException, ServletException {
    resolveAccessToken(request).ifPresent(token -> {
      if (jwtService.validate(token)) {
        TokenInfo tokenInfo = jwtService.parse(token);

        UserDetails userDetails = new ClosetUserDetails(tokenInfo.userId(), tokenInfo.role(),
            tokenInfo.name());

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    });

    chain.doFilter(request, response);
  }

  private Optional<String> resolveAccessToken(HttpServletRequest request) {
    String prefix = "Bearer ";
    return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
        .map(value -> {
          if (value.startsWith(prefix)) {
            return value.substring(prefix.length());
          } else {
            return null;
          }
        });
  }

  private boolean isPermitAll(HttpServletRequest request) {
    return Arrays.stream(SecurityMatchers.PUBLIC_MATCHERS)
        .anyMatch(requestMatcher -> requestMatcher.matches(request));
  }
}
