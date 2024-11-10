package suculenta.webservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import suculenta.webservice.dto.ActionResponse;
import suculenta.webservice.dto.SocketAction;
import suculenta.webservice.dto.SocketResponse;
import suculenta.webservice.model.Kitchener;
import suculenta.webservice.model.Order;
import suculenta.webservice.repository.OrdenDetailRepository;
import suculenta.webservice.repository.OrderRepository;

import java.sql.Date;
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

    public Page<Order.Detail> ordersToMade(Pageable pageable) {
        return detailsRepository.findByMadeByIsNull(pageable);
    }

    @Override
    public Page<Order> select(Pageable pageable) {
        return repository().selectAll(pageable);
    }

    public Page<Order.Detail> select(
        Order.Process process,
        @NonNull Kitchener kitchener,
        Pageable pageable) {
        return detailsRepository.findByCurrentProcessAndMadeBy(process, kitchener, pageable);
    }

    public Page<Order> selectSold(Date since, Date until, @NonNull Pageable pageable) {
        return new PageImpl<>(
            repository.soldOrders(
                since,
                until,
                pageable.getOffset(),
                pageable.getPageSize()),
            pageable,
            repository.count());
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

    @Transactional
    public List<ActionResponse> finish(@NonNull List<Order.Detail> details) {
        return details.stream()
            .map(detail -> {
                var result = detailsRepository.finishOrder(
                    detail.getOrder().getId(),
                    detail.getCns(),
                    detail.getMadeBy().getId()
                );
                if (repository.isReady(detail.getOrder().getId())) {
                    var order = repository().findById(detail.getOrder().getId())
                        .orElseThrow(() -> new NullPointerException("Error"));
                    var response = SocketResponse.json(SocketAction.FINISH_ORDER, order);

                    waiterService.notify(
                        order.getTake_by().getId().toString(),
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
