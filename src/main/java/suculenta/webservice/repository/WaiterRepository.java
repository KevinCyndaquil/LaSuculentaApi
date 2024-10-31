package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import suculenta.webservice.model.Waiter;

import java.util.UUID;

public interface WaiterRepository extends JpaRepository<Waiter, UUID> {
}
