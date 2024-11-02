package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import suculenta.webservice.model.Order;
import suculenta.webservice.service.OrderService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("order")

@RequiredArgsConstructor
public class OrderController implements CrudController<Order, UUID> {
    private final OrderService service;

    @Override
    public OrderService service() {
        return service;
    }

    @GetMapping("to-made")
    public ResponseEntity<List<Order.Detail>> toMade() {
        return ResponseEntity.ok(service.ordersToMade());
    }

    @PutMapping("assign")
    public ResponseEntity<String> assign(@RequestBody List<Order.Detail> details) {
        if (service.assign(details))
            return ResponseEntity.ok("Assigned");
        return ResponseEntity.badRequest().body("Assignment failed");
    }

    @PutMapping("finish")
    public ResponseEntity<String> finish(@RequestBody List<Order.Detail> details) {
        if (service.finish(details))
            return ResponseEntity.ok("Finished order");
        return ResponseEntity.badRequest().body("Assignment failed");
    }

    @PutMapping("deliver")
    public ResponseEntity<String> delivered(@RequestBody List<Order.Detail> details) {
        if (service.deliver(details))
            return ResponseEntity.ok("Delivered");
        return ResponseEntity.badRequest().body("Assignment failed");
    }
}
