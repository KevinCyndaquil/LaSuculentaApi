package suculenta.webservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public record ActionResponse(
    boolean success,
    @JsonProperty("content") Object content
) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> ActionResponse from(boolean success, T object) {
        return new ActionResponse(
            success,
            object == null ? Map.of() : mapper.convertValue(object, Map.class)
        );
    }

    public static <T> ActionResponse success(T object) {
        return ActionResponse.from(
            true,
            object
        );
    }

    public static <T> ActionResponse error(T object) {
        return ActionResponse.from(
            false,
            object
        );
    }
}
