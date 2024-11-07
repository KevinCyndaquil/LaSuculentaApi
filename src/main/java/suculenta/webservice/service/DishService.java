package suculenta.webservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import suculenta.webservice.dto.ActionResponse;
import suculenta.webservice.model.Dish;
import suculenta.webservice.repository.DishRepository;
import suculenta.webservice.repository.RecipeRepository;

import java.util.List;
import java.util.UUID;

@Service

@RequiredArgsConstructor
public class DishService implements CrudService<Dish, UUID> {
    private final DishRepository dishRepository;
    private final RecipeRepository recipeRepository;

    @Override
    public DishRepository repository() {
        return dishRepository;
    }

    @Override
    @Transactional
    public List<ActionResponse> save(@NonNull List<Dish> entity) {
        return entity.stream()
            .map(dish -> {
                var savedDish = repository().save(dish);
                savedDish.setRecipe(
                    dish.getRecipe().stream()
                        .map(r -> {
                            r.setDish(
                                Dish.builder()
                                    .id(savedDish.getId())
                                    .build()
                            );
                            r.setCns(recipeRepository.countByDish_Id(savedDish.getId()) + 1);
                            return recipeRepository.save(r);
                        })
                        .toList()
                );

                return ActionResponse.success(savedDish);
            })
            .toList();
    }

    @Override
    @Transactional
    public List<ActionResponse> update(@NonNull List<Dish> entity) {
        entity.forEach(dish -> {
            dish.getRecipe().forEach(r -> r.setDish(dish));
            recipeRepository.saveAll(dish.getRecipe());
        });

        return CrudService.super.update(entity);
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID uuid) {
        var dish = repository().findById(uuid)
            .orElseThrow(() -> new IllegalArgumentException("Dish not found"));

        recipeRepository.deleteAll(dish.getRecipe());
        CrudService.super.delete(uuid);
    }
}
