package suculenta.webservice.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import suculenta.webservice.group.OnlyRef;
import suculenta.webservice.group.Postable;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity(name = "orders")
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Null(groups = Postable.class)
    @NotNull(groups = OnlyRef.class)
    UUID id;

    @Min(value = 1, groups = Postable.class)
    @Max(value = 10, groups = Postable.class)
    int table_number;

    @Pattern(regexp = "^[A-Z]{2,}( [A-Z]{2,}){0,3}$", groups = Postable.class)
    String client_name;

    @Null(groups = Postable.class)
    Date requested_on = new Date(System.currentTimeMillis());

    @ManyToOne
    @NotNull(groups = Postable.class)
    Waiter take_by;

    @OneToMany(mappedBy = "order")
    @JsonManagedReference
    @NotEmpty(groups = Postable.class)
    List<Detail> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor


    @IdClass(Order.Detail.ID.class)
    @Entity(name = "order_details")
    public static class Detail {
        @Id
        @ManyToOne
        @JsonBackReference
        @NotNull(groups = {Postable.class, OnlyRef.class})
        Order order;

        @Id
        @NotNull(groups = {Postable.class, OnlyRef.class})
        int cns;

        @Null(groups = Postable.class)
        Date ready_on;

        @Null(groups = Postable.class)
        @Enumerated(EnumType.STRING)
        @Column(name = "current_process")
        Process currentProcess = Process.WAITING_KITCHENER;

        @ManyToOne
        @Null(groups = Postable.class)
        @JoinColumn(name = "made_by_id")
        Kitchener madeBy;

        @ManyToOne
        @NotNull(groups = Postable.class)
        Dish dish;

        @JsonProperty("order_id")
        public UUID getOrderId() {
            return order == null ? null : order.getId();
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor

        @Embeddable
        public static class ID {
            @ManyToOne
            @JsonBackReference
            Order order;

            int cns;
        }
    }

    public enum Process {
        WAITING_KITCHENER,
        GETTING_READY,
        READY_TO_DELIVER,
        FINISHED,
        CANCELED,
    }
}
