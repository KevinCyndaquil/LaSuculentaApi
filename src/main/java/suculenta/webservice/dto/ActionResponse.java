package suculenta.webservice.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record ActionResponse(
    boolean success,
    String content
) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> ActionResponse from(boolean success, T object) {
        try {
            return new ActionResponse(success, object == null ? null : mapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
