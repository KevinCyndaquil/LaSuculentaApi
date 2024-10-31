package suculenta.webservice.service;

import lombok.NonNull;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CrudService<T, ID> {
    JpaRepository<T, ID> repository();

    default List<T> select() {
        return repository().findAll();
    }

    default List<T> select(@NonNull Set<ID> idSet) {
        return idSet.stream()
            .map(repository()::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    default List<T> save(@NonNull List<T> entity) {
        return entity.stream()
            .map(repository()::save)
            .toList();
    }

    default List<T> update(@NonNull List<T> entity) {
        return entity.stream()
            .map(repository()::save)
            .toList();
    }

    default void delete(@NonNull ID id) {
        repository().deleteById(id);
    }
}
