package ch.denicola.konfi.table;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
  private static final String topicTemplate = "/table/%s";
  private final SimpMessageSendingOperations messagingTemplate;

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String username = (String) headerAccessor.getSessionAttributes().get("username");
    String table = (String) headerAccessor.getSessionAttributes().get("table");

    if (table != null) {

      var tableMessage = TableMessage.builder().type(MessageType.JOIN).user(username).build();

      messagingTemplate.convertAndSend(String.format(topicTemplate, table), tableMessage);
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String username = (String) headerAccessor.getSessionAttributes().get("username");
    String table = (String) headerAccessor.getSessionAttributes().get("table");

    if (table != null) {

      var tableMessage = TableMessage.builder().type(MessageType.LEAVE).user(username).build();

      messagingTemplate.convertAndSend(String.format(topicTemplate, table), tableMessage);
    }
  }
}
