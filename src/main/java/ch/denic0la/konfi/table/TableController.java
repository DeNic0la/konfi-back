package ch.denic0la.konfi.table;

import ch.denic0la.konfi.table.user.TableUser;
import ch.denic0la.konfi.table.user.TableUserService;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Log
public class TableController {
  private static final String topicTemplate = "/table/%s";
  private final SimpMessageSendingOperations messagingTemplate;

  @Resource
  private TableUserService tableUserService;

    private static @Nullable String getAttr(SimpMessageHeaderAccessor header, String attributeName) {
        try {
            return (String) Objects.requireNonNull(header.getSessionAttributes()).get(attributeName);
        } catch (Exception e) {
            // Handle the case where the attribute is not found or any other error
            log.severe(e.getLocalizedMessage());
            return null;
        }
    }
    private static void setAttr(SimpMessageHeaderAccessor header, @NonNull String attributeName, String value) {
        try{
            Objects.requireNonNull(header.getSessionAttributes()).put(attributeName,value);
        }catch (Exception e) {
            // Handle the case where the attribute is not found or any other error
            log.severe(e.getLocalizedMessage());
        }
    }

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

    try {
        var existing = getAttr(headerAccessor,"tableUserId");
        if (existing != null){
            log.warning("User already registered with id "+existing);
            tableUserService.removeUser(existing);
        }
    } catch (Exception e) {
        log.severe(e.getLocalizedMessage());
    }
    try{
        var username = tableMessage.getUser();
        var tableUser = TableUser.builder().tableId(tableId).name(username).build();
        var uuid = tableUserService.registerUser(tableUser);
        setAttr(headerAccessor,"tableUserId", uuid);
    } catch (Exception e) {
        log.severe(e.getLocalizedMessage());
        messagingTemplate.convertAndSendToUser(Objects.requireNonNull(headerAccessor.getSessionId()),"/queue/errors",e.getLocalizedMessage());
        return;
    }

    messagingTemplate.convertAndSend(String.format(topicTemplate, tableId), tableMessage);
  }

  @MessageMapping("/vote")
  public void vote(@Payload TableUserMessage tableUserMessage,SimpMessageHeaderAccessor headerAccessor) {
        var userId = getAttr(headerAccessor,"tableUserId");
        if (userId == null){
            log.warning("No user registered for session "+headerAccessor.getSessionId());
            messagingTemplate.convertAndSendToUser(Objects.requireNonNull(headerAccessor.getSessionId()),"/queue/errors","No user registered");
            return;
        }
        var user = tableUserService.getUser(userId);
        if (user == null){
            log.warning("No user found for id "+userId);
            messagingTemplate.convertAndSendToUser(Objects.requireNonNull(headerAccessor.getSessionId()),"/queue/errors","No user found");
            return;
        }
        var tableMessage = tableUserMessage.toTableMessage(user);
        messagingTemplate.convertAndSend(String.format(topicTemplate, user.getTableId()), tableMessage);
  }
}
