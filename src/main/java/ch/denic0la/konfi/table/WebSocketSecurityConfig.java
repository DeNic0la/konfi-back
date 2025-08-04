package ch.denic0la.konfi.table;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

  @Bean
  AuthorizationManager<Message<?>> messageAuthorizationManager(
      MessageMatcherDelegatingAuthorizationManager.Builder messages) {
    // TODO: Maybe ensure there is an user set
    messages
        .simpDestMatchers("/live/**")
        .permitAll()
        .simpTypeMatchers(
            SimpMessageType.MESSAGE,
            SimpMessageType.SUBSCRIBE,
            SimpMessageType.UNSUBSCRIBE,
            SimpMessageType.CONNECT,
            SimpMessageType.DISCONNECT)
        .permitAll()
        .anyMessage()
        .permitAll();
    return messages.build();
  }
}
