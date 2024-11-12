package suculenta.webservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import suculenta.webservice.model.Kitchener;
import suculenta.webservice.service.KitchenerService;

import java.sql.Date;
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

    @GetMapping("best")
    public ResponseEntity<Page<Kitchener>> findBest(
        @RequestParam("since") Date since,
        @RequestParam("from") Date from,
        Pageable pageable) {
        return ResponseEntity.ok(service.selectBest(since, from, pageable));
    }
}
