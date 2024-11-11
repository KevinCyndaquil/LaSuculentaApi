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
    ObjectMapper mapper = new ObjectMapper();

    ConcurrentHashMap<String, WebSocketSession> sessions();

    default void register(@NonNull String id, WebSocketSession session) {
        sessions().put(id, session);
    }

    default WebSocketSession getSession(@NonNull String id) {
        return sessions().get(id);
    }

    default WebSocketSession removeSession(@NonNull String id)  {
        return sessions().remove(id);
    }

    default void notify(@NonNull String id, WSResponse response) {
        WebSocketSession session = getSession(id);
        if (session == null) {
            System.out.printf("Skipping notify %s not found%n", id);
            return;
        }

        try {
            var message = new TextMessage(mapper.writeValueAsString(response));
            session.sendMessage(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default void broadcast(WSResponse response) {
        System.out.println(sessions().size());
        sessions().values().forEach(session -> {
            try {
                var message = new TextMessage(mapper.writeValueAsString(response));
                session.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
