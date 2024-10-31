package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import suculenta.webservice.model.Kitchener;
import suculenta.webservice.service.KitchenerService;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("kitchener")

@RequiredArgsConstructor
public class KitchenerController implements CrudController<Kitchener, UUID> {
    private final KitchenerService service;

    @Override
    public KitchenerService service() {
        return service;
    }
}
