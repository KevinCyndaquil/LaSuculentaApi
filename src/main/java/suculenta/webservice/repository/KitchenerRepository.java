package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import suculenta.webservice.model.Kitchener;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface KitchenerRepository extends JpaRepository<Kitchener, UUID> {
    @Query(value = "SELECT * FROM best_kitcheners(:since, :until) OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> best(
        @Param("since") Date since,
        @Param("until") Date until,
        long offset,
        int limit);
}
