package ch.denic0la.konfi.table.user;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TableUserService {
    private final Map<UUID, TableUser> registeredUsers = new HashMap<>();
    private final Map<String, Boolean> takenUsernamesByTableId = new HashMap<>();

    private static String generateKey(String tableId, String username) {
        return tableId + ":" + username;
    }
    private static String generateKey(TableUser user) {
        return generateKey(user.getTableId(), user.getName());
    }
    private boolean validUserExists(TableUser user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (user.getTableId() == null || user.getTableId().isEmpty()) {
            throw new IllegalArgumentException("Table ID cannot be null or empty");
        }
        return  (takenUsernamesByTableId.containsKey(generateKey(user)));
    }
    public String registerUser(TableUser user) {
        if (validUserExists(user)){
            throw new IllegalArgumentException("Username Taken");
        }
        var key = generateKey(user);
        UUID uuid = UUID.randomUUID();
        takenUsernamesByTableId.put(key, true);
        registeredUsers.put(uuid, user);
        return uuid.toString();
    }
    public void removeUser(String uuidString) {
        UUID uuid = UUID.fromString(uuidString);
        TableUser user = registeredUsers.get(uuid);
        if (user != null) {
            var key = generateKey(user);
            takenUsernamesByTableId.remove(key);
            registeredUsers.remove(uuid);
        }
    }

    public TableUser getUser(String uuidString) {
        UUID uuid = UUID.fromString(uuidString);
        return registeredUsers.get(uuid);
    }
}
