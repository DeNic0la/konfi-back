package ch.denic0la.konfi.table;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableMessage {
  private MessageType type;
  private Integer konfi;
  private String user;
}
