package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import suculenta.webservice.dto.OrderDTO;
import suculenta.webservice.dto.Response;
import suculenta.webservice.dto.KitchenerOrder;
import suculenta.webservice.model.Order;
import suculenta.webservice.service.OrderService;

import java.sql.Date;
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

    @PostMapping("filter")
    public ResponseEntity<Page<Order.Detail>> filter(
        @RequestBody KitchenerOrder filterRequest,
        Pageable pageable) {
        return ResponseEntity.ok(service.select(
            filterRequest.process(),
            filterRequest.kitchener(),
            pageable));
    }

    @GetMapping("to-made")
    public ResponseEntity<Page<Order.Detail>> toMade(Pageable pageable) {
        return ResponseEntity.ok(service.ordersToMade(pageable));
    }

    @PutMapping("assign")
    public ResponseEntity<List<Response<Order.Detail>>> assign(@RequestBody List<Order.Detail> details) {
        return ResponseEntity.ok(service.assign(details));
    }

    @PutMapping("finish")
    public ResponseEntity<List<Response<Order.Detail>>> finish(@RequestBody List<Order.Detail> details) {
        return ResponseEntity.ok(service.finish(details));
    }

    @PutMapping("deliver")
    public ResponseEntity<List<Response<Order.Detail>>> delivered(@RequestBody List<Order.Detail> details) {
        return ResponseEntity.ok(service.deliver(details));
    }

    @GetMapping("pages")
    public ResponseEntity<Page<OrderDTO>> select(Pageable pageable) {
        return ResponseEntity.ok(service().selectDTO(pageable));
    }

    @GetMapping("sold")
    public ResponseEntity<Page<Order>> sold(
        @RequestParam("since") Date since,
        @RequestParam("until") Date until,
        Pageable pageable) {
        return ResponseEntity.ok(service().selectSold(since, until, pageable));
    }

    @GetMapping("ready")
    public ResponseEntity<Page<Order>> selectReady(Pageable pageable) {
        return ResponseEntity.ok(service().selectReady(pageable));
    }
}
