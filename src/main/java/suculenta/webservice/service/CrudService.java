package suculenta.webservice.service;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import suculenta.webservice.dto.Response;

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

    default Page<T> select(Pageable pageable) {
        return repository().findAll(pageable);
    }

    default List<Response<T>> save(@NonNull List<T> entity) {
        return entity.stream()
            .map(repository()::save)
            .map(Response::success)
            .toList();
    }

    default List<Response<T>> update(@NonNull List<T> entity) {
        return entity.stream()
            .map(repository()::save)
            .map(Response::success)
            .toList();
    }

    default void delete(@NonNull ID id) {
        repository().deleteById(id);
    }
}
