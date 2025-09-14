package ch.denic0la.konfi.table;

import ch.denic0la.konfi.table.user.TableUserService;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Log
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
  private static final String topicTemplate = "/table/%s";
  private final SimpMessageSendingOperations messagingTemplate;


    @Resource
    private TableUserService tableUserService;

  private @Nullable String getAttr(StompHeaderAccessor header, String attributeName) {
    try {
      return (String) Objects.requireNonNull(header.getSessionAttributes()).get(attributeName);
    } catch (Exception e) {
      // Handle the case where the attribute is not found or any other error
      return null;
    }
  }

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
      log.severe("Connected");
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String username = this.getAttr(headerAccessor, "username");
    String table = this.getAttr(headerAccessor, "table");

    if (table != null) {

      var tableMessage = TableMessage.builder().type(MessageType.JOIN).user(username).build();

      messagingTemplate.convertAndSend(String.format(topicTemplate, table), tableMessage);
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    var tableUserId = this.getAttr(headerAccessor, "tableUserId");
    if (tableUserId != null) {
        var user = tableUserService.getUser(tableUserId);
        var leaveMessage = TableUserMessage.builder().type(MessageType.LEAVE).build().toTableMessage(user);
        messagingTemplate.convertAndSend(String.format(topicTemplate, user.getTableId()), leaveMessage);
        tableUserService.removeUser(tableUserId);
    }
  }
}
