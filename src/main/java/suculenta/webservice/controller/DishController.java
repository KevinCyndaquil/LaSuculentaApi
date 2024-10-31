package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import suculenta.webservice.model.Dish;
import suculenta.webservice.service.DishService;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("dish")

@RequiredArgsConstructor
public class DishController implements CrudController<Dish, UUID> {
    private final DishService service;

    @Override
    public DishService service() {
        return service;
    }
}
