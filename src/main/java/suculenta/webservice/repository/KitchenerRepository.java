package suculenta.webservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import suculenta.webservice.model.Kitchener;

import java.util.UUID;

public interface KitchenerRepository extends JpaRepository<Kitchener, UUID> {
}
