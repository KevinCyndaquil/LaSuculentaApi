package suculenta.webservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import suculenta.webservice.dto.Response;
import suculenta.webservice.model.*;
import suculenta.webservice.service.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderTest {
    @Autowired
    WaiterService waiterService;
    @Autowired
    KitchenerService kitchenerService;
    @Autowired
    IngredientService ingredientService;
    @Autowired
    DishService dishService;
    @Autowired
    OrderService orderService;

    @Test
    @Transactional(isolation = Isolation.READ_COMMITTED)
    void order() {
        var waiters = waiterService.select();
        var kitcheners = kitchenerService.select();
        var dishes = dishService.select();

        Order unsavedOrder = Order.builder()
            .client_name("CHALINO")
            .table_number(2)
            .take_by(waiters.get(0))
            .details(List.of(
                Order.Detail.builder()
                    .dish(dishes.get(0))
                    .build(),
                Order.Detail.builder()
                    .dish(dishes.get(1))
                    .build()
            ))
            .build();

        var savedResponse = orderService.save(List.of(unsavedOrder));
        savedResponse.stream()
            .map(r -> {
                assertTrue(r.success());
                return r.content();
            })
            .forEach(order -> order.getDetails().forEach(detail -> {
                assertEquals(
                    Order.Process.WAITING_KITCHENER,
                    detail.getCurrentProcess());
                assertNotNull(order.getRequested_on());
            }));

        savedResponse.stream()
            .map(Response::content)
            .peek(order -> {
                order.getDetails().forEach(detail -> detail.setMadeBy(kitcheners.get(0)));
                var assignedResponse = orderService.assign(order.getDetails());

                assignedResponse.stream()
                    .map(r -> {
                        assertTrue(r.success());
                        return r.content();
                    })
                    .forEach(detail -> {
                        assertEquals(
                            Order.Process.GETTING_READY,
                            detail.getCurrentProcess());
                        assertEquals(
                            kitcheners.get(0),
                            detail.getMadeBy());
                    });
            })
            .peek(order -> {
                var finishedResponse = orderService.finish(order.getDetails());

                finishedResponse.stream()
                    .map(r -> {
                        assertTrue(r.success());
                        return r.content();
                    })
                    .forEach(detail -> {
                        assertEquals(
                            Order.Process.READY_TO_DELIVER,
                            detail.getCurrentProcess());
                        assertNotNull(detail.getReady_on());
                    });
            })
            .forEach(order -> {
                var deliveredResponse = orderService.deliver(order.getDetails());

                deliveredResponse.stream()
                    .map(r -> {
                        assertTrue(r.success());
                        return r.content();
                    })
                    .forEach(detail -> assertEquals(
                        Order.Process.FINISHED,
                        detail.getCurrentProcess()));
            });
    }
}
