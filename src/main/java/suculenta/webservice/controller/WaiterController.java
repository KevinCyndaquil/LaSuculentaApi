package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import suculenta.webservice.group.Postable;
import suculenta.webservice.model.Waiter;
import suculenta.webservice.service.CrudService;
import suculenta.webservice.service.WaiterService;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("waiter")

@RequiredArgsConstructor
public class WaiterController implements CrudController<Waiter, UUID> {
    private final WaiterService waiterService;

    @Override
    public WaiterService service() {
        return waiterService;
    }
}
