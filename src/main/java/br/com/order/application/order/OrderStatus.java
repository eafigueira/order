package br.com.order.application.order;

import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public enum OrderStatus {
    CREATED(1),
    PROCESSING(2),
    SHIPPED(3),
    DELIVERED(4),
    CANCELED(5);

    private final int id;

    OrderStatus(int id) {
        this.id = id;
    }

    private static final Map<OrderStatus, Set<OrderStatus>> validTransitions = Map.of(
            CREATED, Set.of(PROCESSING, CANCELED),
            PROCESSING, Set.of(SHIPPED),
            SHIPPED, Set.of(DELIVERED),
            DELIVERED, Set.of(),
            CANCELED, Set.of()
    );
    public boolean canTransitionTo(OrderStatus newStatus) {
        return validTransitions.getOrDefault(this, Set.of()).contains(newStatus);
    }
}
