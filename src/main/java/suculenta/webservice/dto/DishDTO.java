package suculenta.webservice.dto;

import suculenta.webservice.model.Dish;

import java.util.UUID;

public record DishDTO(
    UUID id,
    String name,
    Dish.Category category
) {
}
