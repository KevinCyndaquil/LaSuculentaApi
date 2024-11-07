package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import suculenta.webservice.model.Order;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query(value = "SELECT is_ready(:order)", nativeQuery = true)
    boolean isReady(@Param("order") UUID orderId);
}
