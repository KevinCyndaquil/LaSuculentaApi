package suculenta.webservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;

public record Response<T> (
    boolean success,
    @JsonProperty("content") T content,
    String error
) {

    public static <T> Response<T> from(boolean success, @NonNull T object) {
        return new Response<>(
            success,
            object,
            null
        );
    }

    public static <T> Response<T> success(T object) {
        return Response.from(
            true,
            object
        );
    }

    public static <T> Response<T> error(String error) {
        return new Response<>(
            false,
            null,
            error
        );
    }
}
