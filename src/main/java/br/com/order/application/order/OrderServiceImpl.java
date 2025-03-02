package br.com.order.application.order;

import br.com.order.exceptions.BadRequestException;
import br.com.order.exceptions.NotFoundException;
import br.com.order.exceptions.UniqueConstraintViolationException;
import br.com.order.application.customer.Customer;
import br.com.order.application.product.Product;
import br.com.order.application.customer.CustomerRepository;
import br.com.order.application.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public static final String ORDER_NOT_FOUND = "Order not found";
    public static final String CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String PRODUCT_NOT_FOUND = "Product %d not found";
    public static final String DUPLICATE_PRODUCT = "Duplicate product ID: %d";
    public static final String ORDER_ALREADY_PROCESSED = "Order cannot be modified as it has already been processed.";
    public static final String INVALID_STATUS_CHANGE = "Cannot change status from %s to %s";

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CUSTOMER_NOT_FOUND));
    }

    private List<OrderItem> getOrderItems(Order order, List<OrderItemDTO> listItems) {
        return listItems.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new NotFoundException(
                                    String.format(PRODUCT_NOT_FOUND, item.getProductId()))
                            );
                    var orderItem = new OrderItem(product, item.getQuantity(), item.getPrice());
                    orderItem.setOrder(order);
                    return orderItem;
                }).toList();
    }

    private void validateOrderStatus(Order order) {
        if (!OrderStatus.CREATED.equals(order.getStatus())) {
            throw new BadRequestException(ORDER_ALREADY_PROCESSED);
        }
    }

    private void validDuplicatedProducts(List<OrderItem> existingItems, List<OrderItemDTO> newItems) {
        Set<Long> productIds = existingItems.stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());

        for (OrderItemDTO item : newItems) {
            if (!productIds.add(item.getProductId())) {
                throw new UniqueConstraintViolationException(String.format(DUPLICATE_PRODUCT, item.getProductId()));
            }
        }
    }

    private boolean canModifyOrder(Order order) {
        return OrderStatus.CREATED.equals(order.getStatus());
    }

    private void validateStatusFlow(OrderStatus currentStatus, OrderStatus newStatus) {
        if (newStatus.getId() < currentStatus.getId()) {
            throw new BadRequestException(
                    String.format(INVALID_STATUS_CHANGE, currentStatus, newStatus)
            );
        }
    }

    @Transactional
    @Override
    public void create(CreateOrderDTO createOrder) {
        validDuplicatedProducts(Collections.emptyList(), createOrder.getItems());
        var customer = getCustomer(createOrder.getCustomerId());

        var order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);
        order.setDiscount(createOrder.getDiscount());

        for (OrderItemDTO itemDTO : createOrder.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product " + itemDTO.getProductId() + " not found"));

            OrderItem item = new OrderItem(product, itemDTO.getQuantity(), itemDTO.getPrice());
            order.addItem(item);
        }

        repository.save(order);
    }

    @Transactional
    @Override
    public void update(Long id, UpdateOrderDTO updateOrder) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));

        boolean statusChanged = false;

        if (updateOrder.getStatus() != null) {
            if (!order.getStatus().canTransitionTo(updateOrder.getStatus())) {
                throw new BadRequestException("Cannot change status from " + order.getStatus() + " to " + updateOrder.getStatus());
            }
            order.setStatus(updateOrder.getStatus());
            statusChanged = true;
        }

        if (!canModifyOrder(order)) {
            if (!statusChanged) {
                throw new BadRequestException(ORDER_ALREADY_PROCESSED);
            }
            repository.save(order);
            return;
        }

        if (updateOrder.getItems() != null) {
            validDuplicatedProducts(order.getItems(), updateOrder.getItems());
            List<OrderItem> updatedItems = updateOrder.getItems().stream().map(itemDTO -> {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new NotFoundException(
                                String.format(PRODUCT_NOT_FOUND, itemDTO.getProductId())));

                OrderItem orderItem = new OrderItem(product, itemDTO.getQuantity(), itemDTO.getPrice());
                orderItem.setOrder(order);
                return orderItem;
            }).toList();

            order.getItems().clear();
            order.getItems().addAll(updatedItems);
        }

        if (updateOrder.getCustomerId() != null) {
            Customer customer = getCustomer(updateOrder.getCustomerId());
            order.setCustomer(customer);
        }

        if (updateOrder.getDiscount() != null) {
            order.setDiscount(updateOrder.getDiscount());
        }

        repository.save(order);
    }

    @Transactional
    @Override
    public void addItems(Long id, List<OrderItemDTO> items) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));

        validateOrderStatus(order);

        validDuplicatedProducts(order.getItems(), items);
        var orderItems = getOrderItems(order, items);
        order.getItems().addAll(orderItems);

        repository.save(order);
    }

    @Transactional
    @Override
    public void updateItem(Long orderId, Long productId, UpdateOrderItemDTO updateOrderItemDTO) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));

        validateOrderStatus(order);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        String.format(PRODUCT_NOT_FOUND, productId)));

        item.setQuantity(updateOrderItemDTO.getQuantity());
        item.setPrice(updateOrderItemDTO.getPrice());
        item.setOrder(order);

        repository.save(order);
    }


    @Transactional
    @Override
    public void deleteItem(Long orderId, Long productId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));

        validateOrderStatus(order);

        var item = order.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        String.format(PRODUCT_NOT_FOUND, productId))
                );

        order.getItems().remove(item);
        repository.save(order);
    }

    @Transactional
    @Override
    public void delete(Long orderId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));

        validateOrderStatus(order);

        repository.delete(order);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OrderWithoutItems> listOrders(OrderStatus status, Long customerId, Long productId, Pageable pageable) {
        Specification<Order> spec = Specification.where(OrderSpecifications.filterByStatus(status))
                .and(OrderSpecifications.filterByCustomerId(customerId))
                .and(OrderSpecifications.filterByProductId(productId));
        return repository.findAll(spec, pageable).map(mapper::toOrderWithoutItems);
    }

    @Transactional
    @Override
    public OrderDTO getOrderById(Long id) {
        return repository.findById(id).map(mapper::toOrder)
                .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));
    }
}
