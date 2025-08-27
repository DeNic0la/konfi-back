package ch.denic0la.konfi.brunch.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

  @Nested
  @DisplayName("Authentication from HTTP request")
  class GetAuthenticationTests {

    @Test
    @DisplayName("Should return guest authentication for non-brunch paths")
    void shouldReturnGuestAuthForNonBrunchPaths() {
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getRequestURI()).thenReturn("/api/other/endpoint");

      Authentication auth = AuthenticationService.getAuthentication(request);

      assertThat(auth).isInstanceOf(BrunchPasswordAuthenticationToken.class);
      BrunchPasswordAuthenticationToken token = (BrunchPasswordAuthenticationToken) auth;
      assertThat(token.getPrincipal()).isNull();
      assertThat(token.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Should extract brunch ID from path and return guest auth when no headers")
    void shouldExtractBrunchIdAndReturnGuestAuth() {
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getRequestURI()).thenReturn("/api/brunches/test-brunch-123");
      when(request.getHeader("X-ADMIN-PASSWORD")).thenReturn(null);
      when(request.getHeader("X-VOTING-PASSWORD")).thenReturn(null);

      Authentication auth = AuthenticationService.getAuthentication(request);

      assertThat(auth).isInstanceOf(BrunchPasswordAuthenticationToken.class);
      BrunchPasswordAuthenticationToken token = (BrunchPasswordAuthenticationToken) auth;
      assertThat(token.getPrincipal()).isEqualTo("test-brunch-123");
      assertThat(token.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Should return admin authentication when admin header is present")
    void shouldReturnAdminAuthWhenAdminHeaderPresent() {
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getRequestURI()).thenReturn("/api/brunches/admin-brunch");
      when(request.getHeader("X-ADMIN-PASSWORD")).thenReturn("admin-secret-123");
      when(request.getHeader("X-VOTING-PASSWORD")).thenReturn(null);

      Authentication auth = AuthenticationService.getAuthentication(request);

      assertThat(auth).isInstanceOf(BrunchPasswordAuthenticationToken.class);
      BrunchPasswordAuthenticationToken token = (BrunchPasswordAuthenticationToken) auth;
      assertThat(token.getPrincipal()).isEqualTo("admin-brunch");
      assertThat(token.isAdmin()).isTrue();
      assertThat(token.getCredentials()).isEqualTo("admin-secret-123");
    }

    @Test
    @DisplayName("Should return voter authentication when voting header is present")
    void shouldReturnVoterAuthWhenVotingHeaderPresent() {
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getRequestURI()).thenReturn("/api/brunches/voting-brunch/votes");
      when(request.getHeader("X-ADMIN-PASSWORD")).thenReturn(null);
      when(request.getHeader("X-VOTING-PASSWORD")).thenReturn("vote-password");

      Authentication auth = AuthenticationService.getAuthentication(request);

      assertThat(auth).isInstanceOf(BrunchPasswordAuthenticationToken.class);
      BrunchPasswordAuthenticationToken token = (BrunchPasswordAuthenticationToken) auth;
      assertThat(token.getPrincipal()).isEqualTo("voting-brunch");
      assertThat(token.isAdmin()).isFalse();
      assertThat(token.getCredentials()).isEqualTo("vote-password");
    }

    @Test
    @DisplayName("Should prioritize admin auth over voting auth when both headers present")
    void shouldPrioritizeAdminAuthOverVotingAuth() {
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getRequestURI()).thenReturn("/api/brunches/priority-test");
      when(request.getHeader("X-ADMIN-PASSWORD")).thenReturn("admin-pass");
      when(request.getHeader("X-VOTING-PASSWORD")).thenReturn("vote-pass");

      Authentication auth = AuthenticationService.getAuthentication(request);

      assertThat(auth).isInstanceOf(BrunchPasswordAuthenticationToken.class);
      BrunchPasswordAuthenticationToken token = (BrunchPasswordAuthenticationToken) auth;
      assertThat(token.getPrincipal()).isEqualTo("priority-test");
      assertThat(token.isAdmin()).isTrue();
      assertThat(token.getCredentials()).isEqualTo("admin-pass");
    }

    @Test
    @DisplayName("Should handle brunch paths with additional segments")
    void shouldHandleBrunchPathsWithAdditionalSegments() {
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getRequestURI()).thenReturn("/api/brunches/complex-brunch/questions/1/answers");
      when(request.getHeader("X-ADMIN-PASSWORD")).thenReturn(null);
      when(request.getHeader("X-VOTING-PASSWORD")).thenReturn("voter-pass");

      Authentication auth = AuthenticationService.getAuthentication(request);

      assertThat(auth).isInstanceOf(BrunchPasswordAuthenticationToken.class);
      BrunchPasswordAuthenticationToken token = (BrunchPasswordAuthenticationToken) auth;
      assertThat(token.getPrincipal()).isEqualTo("complex-brunch");
      assertThat(token.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Should ignore empty password headers")
    void shouldIgnoreEmptyPasswordHeaders() {
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getRequestURI()).thenReturn("/api/brunches/empty-headers");
      when(request.getHeader("X-ADMIN-PASSWORD")).thenReturn("");
      when(request.getHeader("X-VOTING-PASSWORD")).thenReturn("   ");

      Authentication auth = AuthenticationService.getAuthentication(request);

      assertThat(auth).isInstanceOf(BrunchPasswordAuthenticationToken.class);
      BrunchPasswordAuthenticationToken token = (BrunchPasswordAuthenticationToken) auth;
      assertThat(token.getPrincipal()).isEqualTo("empty-headers");
      assertThat(token.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Should handle brunch IDs with special characters")
    void shouldHandleBrunchIdsWithSpecialCharacters() {
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getRequestURI()).thenReturn("/api/brunches/brunch-123_test.special");
      when(request.getHeader("X-ADMIN-PASSWORD")).thenReturn("admin");
      when(request.getHeader("X-VOTING-PASSWORD")).thenReturn(null);

      Authentication auth = AuthenticationService.getAuthentication(request);

      assertThat(auth).isInstanceOf(BrunchPasswordAuthenticationToken.class);
      BrunchPasswordAuthenticationToken token = (BrunchPasswordAuthenticationToken) auth;
      assertThat(token.getPrincipal()).isEqualTo("brunch-123_test.special");
      assertThat(token.isAdmin()).isTrue();
    }
  }

  @Nested
  @DisplayName("Brunch ID pattern matching")
  class BrunchIdPatternTests {

    @Test
    @DisplayName("Should match valid brunch API paths")
    void shouldMatchValidBrunchApiPaths() {
      assertThat(
              AuthenticationService.getBRUNCH_ID_PATTERN()
                  .matcher("/api/brunches/test-id")
                  .matches())
          .isTrue();
      assertThat(
              AuthenticationService.getBRUNCH_ID_PATTERN().matcher("/api/brunches/123").matches())
          .isTrue();
      assertThat(
              AuthenticationService.getBRUNCH_ID_PATTERN()
                  .matcher("/api/brunches/complex-id-123")
                  .matches())
          .isTrue();
      assertThat(
              AuthenticationService.getBRUNCH_ID_PATTERN()
                  .matcher("/api/brunches/id/votes")
                  .matches())
          .isTrue();
      assertThat(
              AuthenticationService.getBRUNCH_ID_PATTERN()
                  .matcher("/api/brunches/id/questions/1")
                  .matches())
          .isTrue();
    }

    @Test
    @DisplayName("Should not match invalid brunch API paths")
    void shouldNotMatchInvalidBrunchApiPaths() {
      assertThat(AuthenticationService.getBRUNCH_ID_PATTERN().matcher("/api/brunches/").matches())
          .isFalse();
      assertThat(AuthenticationService.getBRUNCH_ID_PATTERN().matcher("/api/brunches").matches())
          .isFalse();
      assertThat(
              AuthenticationService.getBRUNCH_ID_PATTERN().matcher("/api/other/endpoint").matches())
          .isFalse();
      assertThat(
              AuthenticationService.getBRUNCH_ID_PATTERN().matcher("/brunches/test-id").matches())
          .isFalse();
      assertThat(
              AuthenticationService.getBRUNCH_ID_PATTERN()
                  .matcher("api/brunches/test-id")
                  .matches())
          .isFalse();
    }

    @Test
    @DisplayName("Should extract brunch ID correctly")
    void shouldExtractBrunchIdCorrectly() {
      var matcher1 =
          AuthenticationService.getBRUNCH_ID_PATTERN().matcher("/api/brunches/simple-id");
      assertThat(matcher1.matches()).isTrue();
      assertThat(matcher1.group(1)).isEqualTo("simple-id");

      var matcher2 =
          AuthenticationService.getBRUNCH_ID_PATTERN()
              .matcher("/api/brunches/complex-id/votes/123");
      assertThat(matcher2.matches()).isTrue();
      assertThat(matcher2.group(1)).isEqualTo("complex-id");

      var matcher3 =
          AuthenticationService.getBRUNCH_ID_PATTERN().matcher("/api/brunches/test_brunch.123");
      assertThat(matcher3.matches()).isTrue();
      assertThat(matcher3.group(1)).isEqualTo("test_brunch.123");
    }
  }
}
