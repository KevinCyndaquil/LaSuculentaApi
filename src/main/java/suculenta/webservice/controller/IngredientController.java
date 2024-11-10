package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import suculenta.webservice.dto.PredictedIngredient;
import suculenta.webservice.model.Ingredient;
import suculenta.webservice.service.CrudService;
import suculenta.webservice.service.IngredientService;

import java.sql.Date;
import java.util.List;
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

    @GetMapping("predict")
    public ResponseEntity<List<PredictedIngredient>> predict(@RequestParam Date from) {
        return ResponseEntity.ok(service.predict(from));
    }
}
