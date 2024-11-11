package suculenta.webservice.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record CountedIngredient(
    UUID id,
    String name,
    String unit,
    double stock,
    double last_month_used
) {

    public CountedIngredient(Map<String, Object> row) {
        this(
            (UUID) row.get("id"),
            (String) row.get("name"),
            (String) row.get("unit"),
            ((BigDecimal) row.get("stock")).doubleValue(),
            ((BigDecimal) row.get("last_month_used")).doubleValue());
    }
}
