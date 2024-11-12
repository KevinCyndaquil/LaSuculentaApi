package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import suculenta.webservice.model.Waiter;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface WaiterRepository extends JpaRepository<Waiter, UUID> {
    @Query(value = "SELECT * FROM best_waiters(:since, :until) OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<Waiter> best(
        @Param("since") Date since,
        @Param("until") Date until,
        long offset,
        int limit);
}
