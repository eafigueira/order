package br.com.order.application.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {
    void create(CreateOrderDTO createOrder);

    void update(Long id, UpdateOrderDTO updateOrder);

    void addItems(Long id, List<OrderItemDTO> orderItems);

    void updateItem(Long orderId, Long productId, UpdateOrderItemDTO updateOrderItem);

    void deleteItem(Long orderId, Long productId);

    void delete(Long orderId);

    Page<OrderWithoutItems> listOrders(OrderStatus status, Long customerId, Long productId, Pageable pageable);

    OrderDTO getOrderById(Long id);
}
