package ch.denicola.konfi.brunch.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import jakarta.servlet.http.HttpServletRequest;

public class AuthenticationService {

  private static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";
  private static final String AUTH_TOKEN = "Pow";

  public static Authentication getAuthentication(HttpServletRequest request) {
    return new BrunchPasswordAuthentication("todo:implement", AuthorityUtils.NO_AUTHORITIES);

    /*
    String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
    if (apiKey == null || !apiKey.equals(AUTH_TOKEN)) {
        throw new BadCredentialsException("Invalid API Key");
    }

    return new BrunchPasswordAuthentication(apiKey, AuthorityUtils.NO_AUTHORITIES);*/
  }
}
