package ch.denic0la.konfi.brunch.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  static RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role("ADMIN")
        .implies("VOTER")
        .role("VOTER")
        .implies("USER")
        .role("USER")
        .implies("ANONYMOUS")
        .build();
  }

  @Autowired private final BrunchPasswordAuthenticationProvider authProvider;

  @Autowired private AuthenticationConfiguration authenticationConfiguration;

  @Bean
  @Order(3)
  public SecurityFilterChain webSocketsSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .securityMatcher("/native", "/sockJs", "/live/**")
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers("/native")
                    .permitAll()
                    .requestMatchers("/sockJs*")
                    .permitAll()
                    .requestMatchers("/live/**")
                    .permitAll())
        .httpBasic(Customizer.withDefaults())
        .sessionManagement(
            httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS));
    return http.build();
  }

  private static final String[] PUBLIC_PATHS = {"/api/brunches", "/api/ok", "/actuator"};

  @Bean
  @Order(2)
  public SecurityFilterChain publicApiSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .securityMatcher(PUBLIC_PATHS)
        .authorizeHttpRequests(
            authz -> authz.requestMatchers(PUBLIC_PATHS).permitAll().anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .sessionManagement(
            httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS));
    return http.build();
  }

  private static final String[] SECURITY_PATHS = {
    "/api/brunches/*", "/api/brunches/*/vote", "/api/brunches/*/results",
  };

  @Bean
  @Order(1)
  public SecurityFilterChain privateApiSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .securityMatcher(SECURITY_PATHS)
        .authenticationProvider(authProvider)
        .addFilterBefore(
            new BrunchAuthenticationProcessingFilter(
                authenticationConfiguration.getAuthenticationManager()),
            BasicAuthenticationFilter.class)
        // .addFilterBefore( new VoteAuthenticationFilter(), BasicAuthenticationFilter.class)//,
        // UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(authz -> authz.requestMatchers(SECURITY_PATHS).authenticated())
        .httpBasic(Customizer.withDefaults());
    /*.sessionManagement(
    httpSecuritySessionManagementConfigurer ->
        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(
            SessionCreationPolicy.STATELESS));*/
    return http.build();
  }
  /*
  @Bean
  @Order(10)
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorizationManagerRequestMatcherRegistry ->
                authorizationManagerRequestMatcherRegistry.requestMatchers("/**").authenticated())
        .httpBasic(Customizer.withDefaults())
        .sessionManagement(
            httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
        .addFilterBefore(
            new VoteAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }*/
}
