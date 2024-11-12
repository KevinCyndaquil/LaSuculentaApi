package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import suculenta.webservice.model.Dish;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID> {

    @Query(value = "SELECT * FROM sell_dishes_table()", nativeQuery = true)
    List<Map<String, Object>> selectSellTable();
}
