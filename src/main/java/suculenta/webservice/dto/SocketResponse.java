package suculenta.webservice.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record SocketResponse(
    SocketAction action,
    String message,
    String content
) {
    public static SocketResponse plaintText(SocketAction action, String message) {
        return new SocketResponse(action, message, "plain-text");
    }

    public static <T> SocketResponse json(SocketAction action, T object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return new SocketResponse(
                action,
                mapper.writeValueAsString(object),
                "json");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
