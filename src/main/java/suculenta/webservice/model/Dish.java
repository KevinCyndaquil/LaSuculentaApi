package suculenta.webservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import suculenta.webservice.group.OnlyRef;
import suculenta.webservice.group.Postable;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString(exclude = {"recipe"})
@NoArgsConstructor
@AllArgsConstructor

@Entity(name = "dishes")
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Null(groups = Postable.class)
    @NotNull(groups = OnlyRef.class)
    UUID id;

    @NotNull(groups = Postable.class)
    String name;

    @Min(value = 0, groups = Postable.class)
    double sell_price;

    @JsonManagedReference
    @NotNull(groups = Postable.class)
    @OneToMany(mappedBy = "dish")
    List<Recipe> recipe;

    @NotNull(groups = Postable.class)
    @Enumerated(EnumType.STRING)
    Category category;

    @NotNull(groups = Postable.class)
    String icon;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor

    @IdClass(Dish.Recipe.ID.class)
    @Entity(name = "recipes")
    public static class Recipe {
        @Id
        @ManyToOne
        @JsonBackReference
        @Null(groups = Postable.class)
        @NotNull(groups = OnlyRef.class)
        Dish dish;

        @Id
        @Null(groups = Postable.class)
        @NotNull(groups = OnlyRef.class)
        int cns;

        @Min(value = 0, groups = Postable.class)
        double quantity;

        @ManyToOne
        @NotNull(groups = Postable.class)
        Ingredient ingredient;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor

        @Embeddable
        public static class ID {
            @ManyToOne
            @JsonBackReference
            Dish dish;

            int cns;
        }
    }

    public enum Category {
        COLD_DRINK,
        WARM_DRINK,
        BREAKFAST,
        DESSERT,
    }
}
