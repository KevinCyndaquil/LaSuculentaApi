package suculenta.webservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import suculenta.webservice.model.Kitchener;
import suculenta.webservice.model.Order;

import java.util.UUID;

public interface OrdenDetailRepository extends JpaRepository<Order.Detail, Order.Detail.ID> {
    int countByOrder_Id(UUID order_id);

    Page<Order.Detail> findByMadeByIsNull(Pageable pageable);

    Page<Order.Detail> findByCurrentProcessAndMadeBy(Order.Process process, Kitchener madeBy, Pageable pageable);

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

    @Query(value = "SELECT deliver_order(:order, :dish)", nativeQuery = true)
    boolean deliverOrder(
        @Param("order") UUID orderId,
        @Param("dish") int dish);
}
