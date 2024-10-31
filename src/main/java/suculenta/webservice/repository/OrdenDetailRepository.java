package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import suculenta.webservice.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrdenDetailRepository extends JpaRepository<Order.Detail, Order.Detail.ID> {
    int countByOrder_Id(UUID order_id);
    List<Order.Detail> findByMadeByIsNull();

    @Query(value = "SELECT assign_order(:order, :dish, :kitchener)", nativeQuery = true)
    boolean assignOrder(
        @Param("order") UUID orderId,
        @Param("dish") int dish,
        @Param("kitchener") UUID kitchenerId);

    @Query(value = "SELECT finish_order(:order, :dish, :kitchener)", nativeQuery = true)
    boolean finishOrder(
        @Param("order") UUID orderId,
        @Param("dish") int dish,
        @Param("kitchener") UUID kitchenerId);
}
