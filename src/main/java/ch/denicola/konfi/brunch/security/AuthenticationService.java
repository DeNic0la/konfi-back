package ch.denicola.konfi.brunch.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.java.Log;

@Log
public class AuthenticationService {
  private static final String ADMIN_PASSWORD_HEADER_NAME = "X-ADMIN-PASSWORD";
  private static final String VOTING_PASSWORD_HEADER_NAME = "X-VOTING-PASSWORD";

  @Getter(lazy = true)
  private static final Pattern BRUNCH_ID_PATTERN = Pattern.compile("/api/brunches/([^/]+)(/.*)?$");

  private static String pathToBrunchId(String path) {
    Matcher matcher = getBRUNCH_ID_PATTERN().matcher(path);
    if (matcher.matches()) {
      return matcher.group(1); // Return the brunch ID
    }
    return null; // No match found
  }

  private static String getBrunchId(HttpServletRequest req) {
    String path = req.getRequestURI();
    return pathToBrunchId(path);
  }

  public static Authentication getAuthentication(HttpServletRequest request) {
    var brunchId = getBrunchId(request);
    if (StringUtils.isBlank(brunchId)) return BrunchPasswordAuthenticationToken.forGuest(null);
    var adminHeader = request.getHeader(ADMIN_PASSWORD_HEADER_NAME);
    if (StringUtils.isNotBlank(adminHeader))
      return BrunchPasswordAuthenticationToken.forAdmin(brunchId, adminHeader);
    var voterHeader = request.getHeader(VOTING_PASSWORD_HEADER_NAME);
    if (StringUtils.isNotBlank(voterHeader))
      return BrunchPasswordAuthenticationToken.forVoter(brunchId, voterHeader);
    return BrunchPasswordAuthenticationToken.forGuest(brunchId);

    /*
    String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
    if (apiKey == null || !apiKey.equals(AUTH_TOKEN)) {
        throw new BadCredentialsException("Invalid API Key");
    }

    return new BrunchPasswordAuthentication(apiKey, AuthorityUtils.NO_AUTHORITIES);*/
  }
}
