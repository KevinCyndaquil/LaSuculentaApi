package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import suculenta.webservice.model.Ingredient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {
    @Query(value = "SELECT * FROM count_last_month_ingredients_used()", nativeQuery = true)
    List<Map<String, Object>> countLastMonthIngredientUsed();
}
