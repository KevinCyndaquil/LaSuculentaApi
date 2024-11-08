package suculenta.webservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import suculenta.webservice.group.OnlyRef;
import suculenta.webservice.group.Postable;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity(name = "ingredients")
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Null(groups = Postable.class)
    @NotNull(groups = OnlyRef.class)
    UUID id;

    @NotNull(groups = Postable.class)
    String name;

    double cost;

    @Enumerated(EnumType.STRING)
    Unit unit = Unit.GRAMS;

    @Null(groups = Postable.class)
    double stock;

    enum Unit {
        LITERS,
        MILLILITERS,
        KILOS,
        GRAMS,
        POUNDS,
        OUNCES,
    }
}
