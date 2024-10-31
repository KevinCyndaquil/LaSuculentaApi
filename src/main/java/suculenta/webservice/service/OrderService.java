package suculenta.webservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import suculenta.webservice.model.Order;
import suculenta.webservice.repository.OrdenDetailRepository;
import suculenta.webservice.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Service

@RequiredArgsConstructor
public class OrderService implements CrudService<Order, UUID> {
    private final OrderRepository repository;
    private final OrdenDetailRepository detailsRepository;

    @Override
    public OrderRepository repository() {
        return repository;
    }

    public List<Order.Detail> ordersToMade() {
        return detailsRepository.findByMadeByIsNull();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean assign(@NonNull List<Order.Detail> details) {
        return details.stream()
            .map(detail -> detailsRepository.assignOrder(
                detail.getOrder().getId(),
                detail.getCns(),
                detail.getMadeBy().getId()
            ))
            .reduce(true, Boolean::logicalAnd);
    }

    public boolean finish(@NonNull List<Order.Detail> details) {
        return details.stream()
            .map(detail -> detailsRepository.finishOrder(
                detail.getOrder().getId(),
                detail.getCns(),
                detail.getMadeBy().getId()
            ))
            .reduce(true, Boolean::logicalAnd);
    }

    @Override
    @Transactional
    public List<Order> save(@NonNull List<Order> entity) {
        return entity.stream()
            .map(order -> {
                var savedOrder = repository.save(order);

                savedOrder.setDetails(
                    order.getDetails().stream()
                        .map(detail -> {
                            detail.setOrder(
                                Order.builder()
                                    .id(savedOrder.getId())
                                    .build()
                            );
                            detail.setCns(detailsRepository.countByOrder_Id(savedOrder.getId()) + 1);
                            return detailsRepository.save(detail);
                        })
                        .toList()
                );

                return savedOrder;
            })
            .toList();
    }

    @Override
    @Transactional
    public List<Order> update(@NonNull List<Order> entity) {
        entity.forEach(order -> {
            order.getDetails().forEach(detail -> detail.setOrder(order));
            detailsRepository.saveAll(order.getDetails());
        });

        return CrudService.super.update(entity);
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID uuid) {
        var order = repository().findById(uuid)
            .orElseThrow(() -> new IllegalArgumentException("Order did not found"));

        detailsRepository.deleteAll(order.getDetails());
        CrudService.super.delete(uuid);
    }
}
