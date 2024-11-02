package suculenta.webservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import suculenta.webservice.group.OnlyRef;
import suculenta.webservice.group.Postable;
import suculenta.webservice.service.CrudService;

import java.util.List;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public interface CrudController<T, ID> {
    CrudService<T, ID> service();

    @PostMapping
    default ResponseEntity<List<T>> save(@RequestBody @Validated(Postable.class) List<T> entities) {
        return ResponseEntity.ok(service().save(entities));
    }

    @GetMapping
    default ResponseEntity<List<T>> select() {
        return ResponseEntity.ok(service().select());
    }

    @PutMapping
    default ResponseEntity<List<T>> update(@RequestBody @Validated(OnlyRef.class) List<T> entities) {
        return ResponseEntity.ok(service().update(entities));
    }

    @DeleteMapping
    default ResponseEntity<String> delete(@RequestParam("uuid") ID id) {
        service().delete(id);
        return ResponseEntity.ok("deleted %s".formatted(id));
    }
}
