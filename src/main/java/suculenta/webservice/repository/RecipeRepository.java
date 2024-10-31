package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import suculenta.webservice.model.Dish;

import java.util.UUID;

public interface RecipeRepository extends JpaRepository<Dish.Recipe, Dish.Recipe.ID> {
    int countByDish_Id(UUID dish_id);
}
