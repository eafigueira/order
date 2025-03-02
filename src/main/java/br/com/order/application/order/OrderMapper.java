package br.com.order.application.order;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderWithoutItems toOrderWithoutItems(Order order) {
        return new OrderWithoutItems(order.getId(),
                order.getCustomer().getId(),
                order.getDiscount(), order.getStatus(),
                order.getTotal(), order.getCreatedAt(),
                order.getUpdatedAt());
    }

    public OrderDTO toOrder(Order order) {
        return new OrderDTO(
                order.getId(),
                order.getItems().stream()
                        .map(item -> new OrderItemDTO(
                                item.getProduct().getId(),
                                item.getQuantity(),
                                item.getPrice()))
                        .collect(Collectors.toList()),
                order.getCustomer().getId(),
                order.getDiscount(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
