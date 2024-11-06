package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import suculenta.webservice.model.Kitchener;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface KitchenerRepository extends JpaRepository<Kitchener, UUID> {
    @Query(value = "SELECT best_kitcheners(:since, :until)", nativeQuery = true)
    List<Kitchener> best(
        @Param("since") Date since,
        @Param("until") Date until);
}
