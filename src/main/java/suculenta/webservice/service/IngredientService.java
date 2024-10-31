package suculenta.webservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import suculenta.webservice.model.Ingredient;
import suculenta.webservice.repository.IngredientRepository;

import java.util.UUID;

@Service

@RequiredArgsConstructor
public class IngredientService implements CrudService<Ingredient, UUID> {
    private final IngredientRepository repository;

    @Override
    public JpaRepository<Ingredient, UUID> repository() {
        return repository;
    }
}
