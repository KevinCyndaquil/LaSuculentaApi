package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import suculenta.webservice.group.OnlyRef;
import suculenta.webservice.model.Waiter;
import suculenta.webservice.service.WaiterService;

import java.io.IOException;
import java.util.List;
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

    //@PostMapping("register")
    @PostMapping("login")
    public ResponseEntity<List<Waiter>> login(@RequestBody @Validated(OnlyRef.class) Waiter waiter)
        throws IOException {
        var session = service.getSession(waiter.getId().toString());
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage("comunicación con web socket realizada"));
        }

        return ResponseEntity.ok(service.select());
    }
}
