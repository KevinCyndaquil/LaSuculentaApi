package suculenta.webservice.dto;

import lombok.NonNull;
import suculenta.webservice.model.Ingredient;

import java.util.UUID;

public record PredictedIngredient(
    UUID id,
    String name,
    double quantity_predicted,
    Ingredient.Unit unit,
    double stock
) {

    public PredictedIngredient complete(@NonNull Ingredient ingredient) {
        return new PredictedIngredient(
            ingredient.getId(),
            ingredient.getName(),
            quantity_predicted,
            ingredient.getUnit(),
            ingredient.getStock()
        );
    }
}
