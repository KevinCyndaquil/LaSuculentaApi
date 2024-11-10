package suculenta.webservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import suculenta.webservice.dto.WSResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface WebSocketService {
    Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    ObjectMapper mapper = new ObjectMapper();

    default void register(@NonNull String id, WebSocketSession session) {
        sessions.put(id, session);
    }

    default WebSocketSession getSession(@NonNull String id) {
        System.out.println(sessions);
        return sessions.get(id);
    }

    default WebSocketSession removeSession(@NonNull String id)  {
        return sessions.remove(id);
    }

    default void notify(@NonNull String id, WSResponse response) {
        WebSocketSession session = getSession(id);
        try {
            var message = new TextMessage(mapper.writeValueAsString(response));
            session.sendMessage(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default void broadcast(WSResponse response) {
        sessions.values().forEach(session -> {
            try {
                var message = new TextMessage(mapper.writeValueAsString(response));
                session.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
