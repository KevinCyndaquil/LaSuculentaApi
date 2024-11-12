package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;
import suculenta.webservice.model.Dish;
import suculenta.webservice.service.DishService;

import java.util.List;
import java.util.Map;
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

    @GetMapping("selltable")
    public ResponseEntity<List<Map<String, Object>>> sellTable() {
        return ResponseEntity.ok(service.selectSellTable());
    }
}
