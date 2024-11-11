package suculenta.webservice.dto;

import lombok.NonNull;
import suculenta.webservice.model.Dish;

import java.util.UUID;

public record DishDTO(
    UUID id,
    String name,
    Dish.Category category
) {

    public static DishDTO toDTO(@NonNull Dish dish) {
        return new DishDTO(
            dish.getId(),
            dish.getName(),
            dish.getCategory()
        );
    }
}
