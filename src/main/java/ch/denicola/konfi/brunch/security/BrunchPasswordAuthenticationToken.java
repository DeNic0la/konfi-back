package ch.denicola.konfi.brunch.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import lombok.Getter;

public class BrunchPasswordAuthenticationToken extends AbstractAuthenticationToken {
  private final String brunchId;
  private String credentials;
  @Getter private final boolean isAdmin;

  public void clearCredentialsAndSetAuth(boolean authenticated) {
    this.credentials = null;
    setAuthenticated(authenticated);
  }

  @Override
  public String getCredentials() {
    return this.credentials;
  }

  @Override
  public String getPrincipal() {
    return this.brunchId;
  }

  public static BrunchPasswordAuthenticationToken forGuest(String brunchId) {
    return new BrunchPasswordAuthenticationToken(
        brunchId, false, null, AuthorityUtils.createAuthorityList("GUEST"));
  }

  public static BrunchPasswordAuthenticationToken forVoter(
      String brunchId, String voterCredentials) {
    return new BrunchPasswordAuthenticationToken(
        brunchId, false, voterCredentials, AuthorityUtils.createAuthorityList("VOTER"));
  }

  public static BrunchPasswordAuthenticationToken forAdmin(
      String brunchId, String adminCredentials) {
    return new BrunchPasswordAuthenticationToken(
        brunchId, true, adminCredentials, AuthorityUtils.createAuthorityList("ADMIN"));
  }

  public BrunchPasswordAuthenticationToken(
      String brunchId,
      boolean isAdmin,
      String credentials,
      Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.isAdmin = isAdmin;
    this.credentials = credentials;
    this.brunchId = brunchId;
    setAuthenticated(false);
  }
}
