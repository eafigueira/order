package br.com.order.application.order;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications {
    public static Specification<Order> filterByStatus(OrderStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> filterByCustomerId(Long customerId) {
        return (root, query, cb) -> customerId == null ? cb.conjunction() : cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Order> filterByProductId(Long productId) {
        return (root, query, cb) -> {
            if (productId == null) return cb.conjunction();
            Join<Order, OrderItem> orderItems = root.join("items");
            return cb.equal(orderItems.get("product").get("id"), productId);
        };
    }
}
