package suculenta.webservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import java.util.Map;

public record SocketResponse(
    SocketAction action,
    @JsonProperty("message") Object message,
    String content
) {
    public static SocketResponse plaintText(SocketAction action, Object message) {
        return new SocketResponse(action, message, "plain-text");
    }

    public static <T> SocketResponse json(SocketAction action, @NonNull T object) {
        ObjectMapper mapper = new ObjectMapper();
        return new SocketResponse(
            action,
            mapper.convertValue(object, Map.class),
            "json");
    }
}
