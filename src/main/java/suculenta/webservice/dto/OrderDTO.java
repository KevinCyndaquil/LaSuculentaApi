package suculenta.webservice.dto;

import lombok.NonNull;
import suculenta.webservice.model.Kitchener;
import suculenta.webservice.model.Order;
import suculenta.webservice.model.Waiter;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

public record OrderDTO(
    UUID id,
    int table_number,
    String client_name,
    Date request_on,
    Waiter take_by,
    List<DetailsDTO> details
) {

    public record DetailsDTO(
        UUID order_id,
        int cns,
        Date ready_on,
        Order.Process current_process,
        Kitchener made_by,
        DishDTO dish_id
    ) {

        public static List<DetailsDTO> toDTO(List<Order.Detail> orders) {
            return orders.stream()
                .map(detail -> new DetailsDTO(
                    detail.getOrderId(),
                    detail.getCns(),
                    detail.getReady_on(),
                    detail.getCurrentProcess(),
                    detail.getMadeBy(),
                    DishDTO.toDTO(detail.getDish())
                ))
                .toList();
        }
    }

    public static OrderDTO toDTO(@NonNull Order order) {
        return new OrderDTO(
            order.getId(),
            order.getTable_number(),
            order.getClient_name(),
            order.getRequested_on(),
            order.getTake_by(),
            OrderDTO.DetailsDTO.toDTO(order.getDetails())
        );
    }
}
