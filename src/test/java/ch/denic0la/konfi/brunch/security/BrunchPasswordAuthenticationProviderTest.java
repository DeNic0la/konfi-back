package ch.denic0la.konfi.brunch.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ch.denic0la.konfi.brunch.data.BrunchAuthorizationRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BrunchPasswordAuthenticationProviderTest {

  @Mock private BrunchAuthorizationRepository authorizationRepository;

  private BrunchPasswordAuthenticationProvider authProvider;
  private BCryptPasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    authProvider = new BrunchPasswordAuthenticationProvider(authorizationRepository);
    passwordEncoder = new BCryptPasswordEncoder();
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should return null for blank brunch ID")
    void shouldReturnNullForBlankBrunchId() {
      // Given
      BrunchPasswordAuthenticationToken tokenWithEmptyId =
          BrunchPasswordAuthenticationToken.forVoter("", "password");
      BrunchPasswordAuthenticationToken tokenWithNullId =
          BrunchPasswordAuthenticationToken.forVoter(null, "password");
      BrunchPasswordAuthenticationToken tokenWithWhitespaceId =
          BrunchPasswordAuthenticationToken.forVoter("   ", "password");

      // When & Then
      assertThat(authProvider.authenticate(tokenWithEmptyId)).isNull();
      assertThat(authProvider.authenticate(tokenWithNullId)).isNull();
      assertThat(authProvider.authenticate(tokenWithWhitespaceId)).isNull();
    }

    @Test
    @DisplayName("Should return null for non-BrunchPasswordAuthenticationToken")
    void shouldReturnNullForWrongTokenType() {
      // Given
      Authentication differentToken =
          new Authentication() {
            @Override
            public String getName() {
              return "test";
            }

            @Override
            public Object getCredentials() {
              return "creds";
            }

            @Override
            public Object getDetails() {
              return null;
            }

            @Override
            public Object getPrincipal() {
              return "principal";
            }

            @Override
            public boolean isAuthenticated() {
              return false;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) {}

            @Override
            public java.util.Collection<
                    ? extends org.springframework.security.core.GrantedAuthority>
                getAuthorities() {
              return java.util.Collections.emptyList();
            }
          };

      // When & Then
      assertThat(authProvider.authenticate(differentToken)).isNull();
    }

    @Test
    @DisplayName("Should support correct authentication token class")
    void shouldSupportCorrectTokenClass() {
      assertThat(authProvider.supports(BrunchPasswordAuthenticationToken.class)).isTrue();
      assertThat(authProvider.supports(Authentication.class)).isFalse();
      assertThat(authProvider.supports(String.class)).isFalse();
    }
  }
}
