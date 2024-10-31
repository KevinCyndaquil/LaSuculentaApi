package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import suculenta.webservice.model.Ingredient;
import suculenta.webservice.service.CrudService;
import suculenta.webservice.service.IngredientService;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("ingredient")

@RequiredArgsConstructor
public class IngredientController implements CrudController<Ingredient, UUID> {
    private final IngredientService service;

    @Override
    public CrudService<Ingredient, UUID> service() {
        return service;
    }
}
