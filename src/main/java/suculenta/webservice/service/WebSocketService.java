package suculenta.webservice.service;

import lombok.NonNull;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface WebSocketService {
    Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    default void register(@NonNull String id, WebSocketSession session) {
        sessions.put(id, session);
    }

    default WebSocketSession getSession(@NonNull String id) {
        return sessions.get(id);
    }

    default WebSocketSession removeSession(@NonNull String id)  {
        return sessions.remove(id);
    }
}
