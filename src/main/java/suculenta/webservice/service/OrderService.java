package suculenta.webservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import suculenta.webservice.dto.ActionResponse;
import suculenta.webservice.dto.SocketAction;
import suculenta.webservice.dto.SocketResponse;
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
    private final WaiterService waiterService;
    private final KitchenerService kitchenerService;

    @Override
    public OrderRepository repository() {
        return repository;
    }

    public List<Order.Detail> ordersToMade() {
        return detailsRepository.findByMadeByIsNull();
    }

    @Override
    public Page<Order> select(Pageable pageable) {
        return repository().selectAll(pageable);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<ActionResponse> assign(@NonNull List<Order.Detail> details) {
        return details.stream()
            .map(detail -> {
                var result = detailsRepository.assignOrder(
                    detail.getOrder().getId(),
                    detail.getCns(),
                    detail.getMadeBy().getId()
                );
                return ActionResponse.from(result, detail);
            })
            .toList();
    }

    public List<ActionResponse> finish(@NonNull List<Order.Detail> details) {
        return details.stream()
            .map(detail -> {
                var result = detailsRepository.finishOrder(
                    detail.getOrder().getId(),
                    detail.getCns(),
                    detail.getMadeBy().getId()
                );
                if (repository.isReady(detail.getOrder().getId())) {
                    var response = SocketResponse.json(SocketAction.FINISH_ORDER, detail.getOrder());
                    waiterService.notify(
                        detail.getOrder().getTake_by().getId().toString(),
                        response);
                }

                return ActionResponse.from(result, detail);
            })
            .toList();
    }

    public List<ActionResponse> deliver(@NonNull List<Order.Detail> details) {
        return details.stream()
            .map(detail -> {
                var result = detailsRepository.deliverOrder(
                    detail.getOrder().getId(),
                    detail.getCns()
                );
                return ActionResponse.from(result, detail);
            })
            .toList();
    }

    @Override
    @Transactional
    public List<ActionResponse> save(@NonNull List<Order> entity) {
        var newOrder = entity.stream()
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

                return ActionResponse.success(savedOrder);
            })
            .toList();

        kitchenerService.broadcast(SocketResponse.plaintText(
            SocketAction.NEW_ORDER,
            "pene")
        );
        return newOrder;
    }

    @Override
    @Transactional
    public List<ActionResponse> update(@NonNull List<Order> entity) {
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
