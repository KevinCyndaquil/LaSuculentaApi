package suculenta.webservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import suculenta.webservice.dto.KitchenerOrder;
import suculenta.webservice.dto.Response;
import suculenta.webservice.model.*;
import suculenta.webservice.service.*;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = LaSuculentaApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderTest {
    @LocalServerPort
    int port;

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

    @Autowired
    RestTemplate restTemplate;

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

    @Test
    void filterOrders() {
        String url = "http://localhost:%s/order/filter?size=%s&page=%s"
            .formatted(port, 10, 0);

        Kitchener kitchener = kitchenerService.select().get(0);
        KitchenerOrder kitchenerOrder = new KitchenerOrder(
            Order.Process.WAITING_KITCHENER,
            kitchener);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            new HttpEntity<>(kitchenerOrder, headers),
            new ParameterizedTypeReference<Map<String, Object>>() {
            }
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<Order.Detail> orderDetails = new ObjectMapper()
            .convertValue(response.getBody().get("content"), new TypeReference<>() {});
        System.out.println(orderDetails);
    }
}
