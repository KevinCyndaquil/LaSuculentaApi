package suculenta.webservice.dto;

import lombok.NonNull;

import java.util.UUID;

public record PredictedIngredient(
    UUID id,
    String name,
    String unit,
    String priority,
    double last_month_used,
    double predicted_quantity,
    double stock,
    double difference
) {

    public PredictedIngredient complete(@NonNull CountedIngredient ingredient) {
        return new PredictedIngredient(
            ingredient.id(),
            ingredient.name(),
            ingredient.unit(),
            priority,
            ingredient.last_month_used(),
            predicted_quantity,
            ingredient.stock(),
            ingredient.stock() - predicted_quantity
        );
    }

    public static PredictedIngredient zero() {
        return new PredictedIngredient(null, "", "", "none", 0, 0, 0, 0
        );
    }
}
