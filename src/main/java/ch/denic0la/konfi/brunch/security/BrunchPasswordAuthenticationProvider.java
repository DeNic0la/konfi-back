package ch.denic0la.konfi.brunch.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ch.denic0la.konfi.KonfiApplication;
import ch.denic0la.konfi.brunch.data.BrunchAuthorization;
import ch.denic0la.konfi.brunch.data.BrunchAuthorizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Component
@Log
@RequiredArgsConstructor
@Primary
public class BrunchPasswordAuthenticationProvider implements AuthenticationProvider {

  private final BrunchAuthorizationRepository authorizationRepository;
  private final PasswordEncoder passwordEncoder = KonfiApplication.getPasswordEncoder();

  private Authentication handleBlankBrunchId() {
    return null;
  }

  private boolean matches(@Nullable String rawPassword, @Nullable String encodedPassword) {
    if (StringUtils.isBlank(rawPassword) || StringUtils.isBlank(encodedPassword)) {
      return false;
    }
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }

  private BrunchPasswordAuthenticationToken runAuthentication(
      BrunchAuthorization authData, BrunchPasswordAuthenticationToken token) {
    boolean votingPasswordIsEmpty = StringUtils.isEmpty(authData.getVotingPasswordHash());
    boolean adminPasswordIsEmpty = StringUtils.isEmpty(authData.getAdminPasswordHash());

    if (votingPasswordIsEmpty && (adminPasswordIsEmpty || !token.isAdmin())) {
      token.clearCredentialsAndSetAuth(true);
      return token;
    }
    if (token.isAdmin()) {
      if (adminPasswordIsEmpty
          || matches(token.getCredentials(), authData.getAdminPasswordHash())) {
        token.clearCredentialsAndSetAuth(true);
        return token;
      }
    } else if (matches(token.getCredentials(), authData.getVotingPasswordHash())) {
      token.clearCredentialsAndSetAuth(true);
      return token;
    }

    throw new BadCredentialsException("Failed to validate credentials");
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    log.info("Authenticating: " + authentication.getName());
    if (authentication instanceof BrunchPasswordAuthenticationToken token) {
      var brunchId = token.getPrincipal();
      if (StringUtils.isBlank(brunchId)) return handleBlankBrunchId();
      try {
        var brunchAuthorization =
            authorizationRepository
                .findByBrunchId(brunchId)
                .orElse(
                    BrunchAuthorization.builder()
                        .adminPasswordHash(null)
                        .votingPasswordHash(null)
                        .brunchId(brunchId)
                        .build());
        log.info("Authenticating for brunch: " + brunchId);

        return runAuthentication(brunchAuthorization, token);
      } catch (Exception e) {
        log.severe(e.getMessage());
        log.info(e.getLocalizedMessage());
        throw e;
      }
    } else {
      return null;
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    log.info(authentication.getName());
    return BrunchPasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
