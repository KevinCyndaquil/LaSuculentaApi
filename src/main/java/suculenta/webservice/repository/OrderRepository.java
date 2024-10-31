package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import suculenta.webservice.model.Order;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
