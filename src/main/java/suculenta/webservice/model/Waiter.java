package suculenta.webservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import suculenta.webservice.group.OnlyRef;
import suculenta.webservice.group.Postable;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity(name = "waiters")
public class Waiter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Null(groups = Postable.class)
    @NotNull(groups = OnlyRef.class)
    UUID id;

    @Pattern(regexp = "^[A-Z]{2,}( [A-Z]{2,})?$", groups = Postable.class)
    String name;

    @Pattern(regexp = "^[A-Z]{2,}( [A-Z]{2,})?$", groups = Postable.class)
    String lastname;
}
