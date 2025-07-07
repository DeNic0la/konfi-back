package ch.denicola.konfi.table;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TableController {
  private static final String topicTemplate = "/table/%s";
  private final SimpMessageSendingOperations messagingTemplate;

  @MessageMapping("/update/{tableId}")
  // @SendTo("/table/public/{tableId}") Spring 4.2+
  public void sendMessage(@DestinationVariable String tableId, @Payload TableMessage tableMessage) {

    messagingTemplate.convertAndSend(String.format(topicTemplate, tableId), tableMessage);
  }

  @MessageMapping("/join/{tableId}")
  // @SendTo("/table/public/{tableId}") Spring 4.2+
  public void addUser(
      @DestinationVariable String tableId,
      @Payload TableMessage tableMessage,
      SimpMessageHeaderAccessor headerAccessor) {

    // Add username in web socket session
    headerAccessor.getSessionAttributes().put("username", tableMessage.getUser());
    headerAccessor.getSessionAttributes().put("table", tableId);
    messagingTemplate.convertAndSend(String.format(topicTemplate, tableId), tableMessage);
  }
}
