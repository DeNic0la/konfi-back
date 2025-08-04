package ch.denic0la.konfi.brunch.security;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BrunchAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

  public BrunchAuthenticationProcessingFilter(
      RequestMatcher requiresAuthenticationRequestMatcher,
      AuthenticationManager authenticationManager) {
    super(requiresAuthenticationRequestMatcher, authenticationManager);
  }

  public BrunchAuthenticationProcessingFilter(AuthenticationManager authenticationManager) {
    super("/api/**", authenticationManager);
  }

  public BrunchAuthenticationProcessingFilter() {
    super("/api/**");
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    Authentication authRequest =
        AuthenticationService.getAuthentication((HttpServletRequest) request);

    return this.getAuthenticationManager().authenticate(authRequest);
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult)
      throws IOException, ServletException {
    logger.info("Successful authentication for: " + authResult.getName());
    SecurityContextHolder.getContext().setAuthentication(authResult);
    chain.doFilter(request, response);
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {
    logger.warn("Unsuccessful authentication for: " + failed.getMessage());
    super.unsuccessfulAuthentication(request, response, failed);
  }
}
