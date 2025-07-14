package ch.denicola.konfi.brunch.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class BrunchPasswordAuthentication extends AbstractAuthenticationToken {
  private final String password;

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return null;
  }

  public BrunchPasswordAuthentication(
      String password, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.password = password;
    setAuthenticated(true);
  }
}
