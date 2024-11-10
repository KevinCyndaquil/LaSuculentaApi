package suculenta.webservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import java.util.Map;

public record WSResponse(
    WSAction action,
    @JsonProperty("message") Object message,
    String content
) {
    public static WSResponse plaintText(WSAction action, Object message) {
        return new WSResponse(action, message, "plain-text");
    }

    public static <T> WSResponse json(WSAction action, @NonNull T object) {
        ObjectMapper mapper = new ObjectMapper();
        return new WSResponse(
            action,
            mapper.convertValue(object, Map.class),
            "json");
    }
}