package com.stylemycloset.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.security.ClosetUserDetailsService;
import com.stylemycloset.security.CustomAuthenticationProvider;
import com.stylemycloset.security.CustomLoginFailureHandler;
import com.stylemycloset.security.CustomLoginSuccessHandler;
import com.stylemycloset.security.JsonUsernamePasswordAuthenticationFilter;
import com.stylemycloset.security.OAuth2LoginSuccessHandler;
import com.stylemycloset.security.SecurityMatchers;
import com.stylemycloset.security.jwt.JwtAuthenticationFilter;
import com.stylemycloset.security.jwt.JwtLogoutHandler;
import com.stylemycloset.security.jwt.JwtService;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.user.service.CustomOAuth2UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      DaoAuthenticationProvider daoAuthenticationProvider,
      ObjectMapper objectMapper,
      JwtService jwtService) throws Exception {
    http
        .authenticationProvider(daoAuthenticationProvider)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .requestMatchers(SecurityMatchers.PUBLIC_MATCHERS).permitAll()
            .anyRequest().hasRole(Role.USER.name())
        )
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService)
            )
            .successHandler(oAuth2LoginSuccessHandler)
        )
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(SecurityMatchers.LOGOUT)
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
        )
        .logout(logout ->
            logout
                .logoutRequestMatcher(SecurityMatchers.LOGOUT)
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .addLogoutHandler(new JwtLogoutHandler(jwtService))
        )
        .with(new JsonUsernamePasswordAuthenticationFilter.Configurer(objectMapper), configurer ->
            configurer
                .successHandler(new CustomLoginSuccessHandler(jwtService))
                .failureHandler(new CustomLoginFailureHandler(objectMapper))
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtService, objectMapper),
            JsonUsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider(
      UserRepository userRepository,
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder,
      RoleHierarchy roleHierarchy
  ) {
    DaoAuthenticationProvider provider = new CustomAuthenticationProvider(userRepository,
        passwordEncoder, (ClosetUserDetailsService) userDetailsService);
    provider.setAuthoritiesMapper(new RoleHierarchyAuthoritiesMapper(roleHierarchy));
    return provider;
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role(Role.ADMIN.name())
        .implies(Role.USER.name())
        .build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      List<AuthenticationProvider> authenticationProviders) {
    return new ProviderManager(authenticationProviders);
  }
}
