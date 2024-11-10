package suculenta.webservice.service;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import suculenta.webservice.dto.OrderDTO;
import suculenta.webservice.dto.Response;
import suculenta.webservice.dto.WSAction;
import suculenta.webservice.dto.WSResponse;
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
    private final EntityManager entityManager;

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

    public Page<Order> selectReady(Pageable pageable) {
        return new PageImpl<>(
            repository.selectReady(pageable.getOffset(), pageable.getPageSize()),
            pageable,
            repository.countReady()
        );
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
    public List<Response<Order.Detail>> assign(@NonNull List<Order.Detail> details) {
        return details.stream()
            .map(detail -> {
                var result = detailsRepository.assignOrder(
                    detail.getOrder().getId(),
                    detail.getCns(),
                    detail.getMadeBy().getId()
                );
                entityManager.refresh(detail);

                var upDetail = detailsRepository.findById(detail.getId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));
                System.out.println("Order " + detail.getOrder().getId() + " cns " + upDetail.getCns());
                System.out.println(upDetail.getMadeBy());
                System.out.println("Current Process " + upDetail.getCurrentProcess());
                System.out.println("Current success " + result);
                return Response.from(result, upDetail);
            })
            .toList();
    }

    @Transactional
    public List<Response<Order.Detail>> finish(@NonNull List<Order.Detail> details) {
        return details.stream()
            .map(detail -> {
                var result = detailsRepository.finishOrder(
                    detail.getOrder().getId(),
                    detail.getCns(),
                    detail.getMadeBy().getId()
                );
                entityManager.refresh(detail);
                var upDetail = detailsRepository.findById(detail.getId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

                if (repository.isReady(detail.getOrder().getId())) {
                    var order = repository().findById(detail.getOrder().getId())
                        .orElseThrow(() -> new NullPointerException("Error"));
                    var response = WSResponse.json(WSAction.FINISH_ORDER, order);

                    waiterService.notify(
                        order.getTake_by().getId().toString(),
                        response);
                }

                return Response.from(result, upDetail);
            })
            .toList();
    }

    public List<Response<Order.Detail>> deliver(@NonNull List<Order.Detail> details) {
        return details.stream()
            .map(detail -> {
                var result = detailsRepository.deliverOrder(
                    detail.getOrder().getId(),
                    detail.getCns()
                );
                entityManager.refresh(detail);
                var upDetail = detailsRepository.findById(detail.getId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

                return Response.from(result, upDetail);
            })
            .toList();
    }

    @Override
    @Transactional
    public List<Response<Order>> save(@NonNull List<Order> entity) {
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

                return Response.success(savedOrder);
            })
            .toList();

        kitchenerService.broadcast(WSResponse.plaintText(
            WSAction.NEW_ORDER,
            "pene")
        );
        return newOrder;
    }

    @Override
    @Transactional
    public List<Response<Order>> update(@NonNull List<Order> entity) {
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
