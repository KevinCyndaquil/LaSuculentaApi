package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import suculenta.webservice.model.Waiter;
import suculenta.webservice.service.WaiterService;

import java.sql.Date;
import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("waiter")

@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class WaiterController implements CrudController<Waiter, UUID> {
    private final WaiterService service;

    @Override
    public WaiterService service() {
        return service;
    }

    @GetMapping("best")
    public ResponseEntity<Page<Map<String, Object>>> findBest(
        @RequestParam("since") Date since,
        @RequestParam("from") Date from,
        Pageable pageable) {
        return ResponseEntity.ok(service.selectBest(since, from, pageable));
    }
}
