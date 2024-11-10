package suculenta.webservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import suculenta.webservice.model.Order;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query(value = "SELECT is_ready(:order)", nativeQuery = true)
    boolean isReady(@Param("order") UUID orderId);

    @Query(value = "SELECT * FROM orders WHERE is_ready(id) = true ORDER BY requested_on DESC OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<Order> selectReady(
        @Param("offset") long offset,
        @Param("limit") int limit);

    @Query(value = "SELECT count(*) FROM orders WHERE is_ready(id) = true", nativeQuery = true)
    long countReady();

    @Query(value = "SELECT * FROM orders ORDER BY requested_on DESC", nativeQuery = true)
    Page<Order> selectAll(Pageable pageable);

    @Query(value = "SELECT * FROM sold_orders(:since, :until) OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<Order> soldOrders(
        @Param("since") Date since,
        @Param("until") Date until,
        @Param("offset") long offset,
        @Param("limit") int limit);
}
