package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import suculenta.webservice.model.Dish;

import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID> {
}
