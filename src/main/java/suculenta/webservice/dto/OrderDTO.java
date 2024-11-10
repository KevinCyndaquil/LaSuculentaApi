package suculenta.webservice.dto;

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
        UUID dish_id
    ) {}
}
