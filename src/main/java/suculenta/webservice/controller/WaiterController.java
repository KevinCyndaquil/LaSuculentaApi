package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import suculenta.webservice.model.Waiter;
import suculenta.webservice.service.WaiterService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("waiter")

@RequiredArgsConstructor
public class WaiterController implements CrudController<Waiter, UUID> {
    private final WaiterService service;

    @Override
    public WaiterService service() {
        return service;
    }

    //@PostMapping("register")
    @GetMapping("register")
    public ResponseEntity<List<Waiter>> register(@RequestParam String id) throws IOException {
        var session = service.getSession(id);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage("comunicación con web socket realizada"));
        }

        return ResponseEntity.ok(service.select());
    }
}
