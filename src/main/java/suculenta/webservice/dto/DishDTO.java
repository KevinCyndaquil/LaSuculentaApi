package suculenta.webservice.dto;

import java.util.UUID;

public record DishDTO(
    UUID id,
    String name
) {
}
