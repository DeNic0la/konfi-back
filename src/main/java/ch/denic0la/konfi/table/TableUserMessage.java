package ch.denic0la.konfi.table;

import ch.denic0la.konfi.table.user.TableUser;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableUserMessage {

        private MessageType type;
        private Integer konfi;
    public TableMessage toTableMessage(TableUser user) {
        return TableMessage.builder()
                .type(this.type)
                .konfi(this.konfi)
                .user(user != null ? user.getName() : null)
                .build();
    }

}
