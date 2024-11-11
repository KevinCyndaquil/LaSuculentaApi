package suculenta.webservice.service;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import suculenta.webservice.dto.*;
import suculenta.webservice.model.Kitchener;
import suculenta.webservice.model.Order;
import suculenta.webservice.repository.OrdenDetailRepository;
import suculenta.webservice.repository.OrderRepository;

import java.sql.Date;
import java.time.LocalDate;
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
    private final IngredientService ingredientService;
    private final AdminService adminService;

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

    public Page<OrderDTO> selectDTO(Pageable pageable) {
        return repository().selectAll(pageable)
            .map(OrderDTO::toDTO);
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
                if (entityManager.contains(detail))
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
                if (entityManager.contains(detail))
                    entityManager.refresh(detail);

                var upDetail = detailsRepository.findById(detail.getId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

                notifySoldOrder(detail);

                /*if (repository.isReady(detail.getOrder().getId())) {
                    var order = repository().findById(detail.getOrder().getId())
                        .orElseThrow(() -> new NullPointerException("Error"));
                    var response = WSResponse.json(WSAction.FINISH_ORDER, order);

                    waiterService.notify(
                        order.getTake_by().getId().toString(),
                        response);
                }*/

                return Response.from(result, upDetail);
            })
            .toList();
    }

    @Async
    public void notifySoldOrder(@NonNull Order.Detail detail) {
        if (!repository.isReady(detail.getOrder().getId()))
            return;

        var order = repository().findById(detail.getOrder().getId())
            .orElseThrow(() -> new NullPointerException("Error"));
        var response = WSResponse.json(WSAction.FINISH_ORDER, order);

        waiterService.notify(
            order.getTake_by().getId().toString(),
            response);
    }

    public List<Response<Order.Detail>> deliver(@NonNull List<Order.Detail> details) {
        return details.stream()
            .map(detail -> {
                var result = detailsRepository.deliverOrder(
                    detail.getOrder().getId(),
                    detail.getCns()
                );
                if (entityManager.contains(detail))
                    entityManager.refresh(detail);

                var upDetail = detailsRepository.findById(detail.getId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

                checkOutSales();

                return Response.from(result, upDetail);
            })
            .toList();
    }

    @Async
    public void checkOutSales() {
        long todaySales = repository.countTodaySales();

        if (todaySales % 2 != 0) {
            System.out.printf("There is %s sales%n", todaySales);
            return;
        }
        var predicted = ingredientService.predict(Date.valueOf(LocalDate.now().plusDays(1)))
            .stream()
            .filter(prediction -> prediction.priority().equals("mucho"))
            .map(PredictedIngredient::name)
            .toList();

        if (predicted.isEmpty())
            return;
        adminService.broadcast(WSResponse.json(WSAction.NEW_PREDICTION, predicted));
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
            "new order")
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
