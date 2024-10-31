package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import suculenta.webservice.model.Ingredient;

import java.util.UUID;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {
}
