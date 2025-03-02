package br.com.order.services;

import br.com.order.application.customer.Customer;
import br.com.order.application.customer.CustomerRepository;
import br.com.order.application.order.*;
import br.com.order.application.product.Product;
import br.com.order.application.product.ProductRepository;
import br.com.order.exceptions.BadRequestException;
import br.com.order.exceptions.NotFoundException;
import br.com.order.exceptions.UniqueConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper mapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final String ORDER_NOT_FOUND = "Order not found";
    private static final String CUSTOMER_NOT_FOUND = "Customer not found";
    private static final String PRODUCT_NOT_FOUND = "Product %d not found";
    private static final String DUPLICATE_PRODUCT = "Duplicate product ID: %d";
    private static final String ORDER_ALREADY_PROCESSED = "Order cannot be modified as it has already been processed.";
    private static final String INVALID_STATUS_CHANGE = "Cannot change status from %s to %s";

    @Test
    @DisplayName("Given a valid CreateOrderDTO, should create an order successfully")
    void create_WhenValidDto_ShouldCreateOrderSuccessfully() {
        CreateOrderDTO dto = new CreateOrderDTO(List.of(new OrderItemDTO(1L, 2, BigDecimal.TEN)), 1L, BigDecimal.ZERO);
        Customer customer = new Customer("Customer 1", "John Doe");
        Product product = new Product("SKU-1234", "Product 1", BigDecimal.TEN);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());

        orderService.create(dto);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Given a CreateOrderDTO with duplicate products, should throw UniqueConstraintViolationException")
    void create_WhenDuplicateProducts_ShouldThrowUniqueConstraintViolationException() {
        CreateOrderDTO dto = new CreateOrderDTO(List.of(
                new OrderItemDTO(1L, 2, BigDecimal.TEN),
                new OrderItemDTO(1L, 3, BigDecimal.valueOf(15.0))), 1L,
                BigDecimal.ZERO
        );

        UniqueConstraintViolationException exception = assertThrows(UniqueConstraintViolationException.class, () -> orderService.create(dto));
        assertEquals(String.format(DUPLICATE_PRODUCT, 1L), exception.getMessage());
    }

    @Test
    @DisplayName("Given a CreateOrderDTO with non-existing customer, should throw NotFoundException")
    void create_WhenCustomerNotFound_ShouldThrowNotFoundException() {
        CreateOrderDTO dto = new CreateOrderDTO(List.of(new OrderItemDTO(1L, 2, BigDecimal.TEN)), 1L, BigDecimal.ZERO);
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> orderService.create(dto));
        assertEquals(CUSTOMER_NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Given a valid UpdateOrderDTO, should update the order successfully")
    void update_WhenValidDto_ShouldUpdateOrderSuccessfully() {
        Long orderId = 1L;
        UpdateOrderDTO dto = new UpdateOrderDTO(List.of(new OrderItemDTO(1L, 3, BigDecimal.valueOf(15.0))), 1L, BigDecimal.valueOf(5.0), null);
        Order order = new Order(new ArrayList<>(), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(orderId);
        Product product = new Product("SKU-123", "Product 1", BigDecimal.valueOf(15.0));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer("Customer 1", "John Doe")));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.update(orderId, dto);

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Given an UpdateOrderDTO with invalid status transition, should throw BadRequestException")
    void update_WhenInvalidStatusTransition_ShouldThrowBadRequestException() {
        Long orderId = 1L;
        UpdateOrderDTO dto = new UpdateOrderDTO(null, null, null, OrderStatus.CREATED);
        Order order = new Order(Collections.emptyList(), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.SHIPPED);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> orderService.update(orderId, dto));
        assertEquals(String.format(INVALID_STATUS_CHANGE, OrderStatus.SHIPPED, OrderStatus.CREATED), exception.getMessage());
    }

    @Test
    @DisplayName("Given an order that cannot be modified, should throw BadRequestException if trying to update items")
    void update_WhenOrderCannotBeModified_ShouldThrowBadRequestException() {
        Long orderId = 1L;
        UpdateOrderDTO dto = new UpdateOrderDTO(List.of(new OrderItemDTO(1L, 3, BigDecimal.valueOf(15.0))), null, null, null);
        Order order = new Order(Collections.emptyList(), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.PROCESSING);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> orderService.update(orderId, dto));
        assertEquals(ORDER_ALREADY_PROCESSED, exception.getMessage());
    }

    @Test
    @DisplayName("Given a valid list of OrderItemDTO, should add items to the order successfully")
    void addItems_WhenValidItems_ShouldAddItemsSuccessfully() {
        Long orderId = 1L;
        List<OrderItemDTO> items = List.of(new OrderItemDTO(1L, 2, BigDecimal.TEN));
        Order order = new Order(new ArrayList<>(), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(orderId);
        Product product = new Product("SKU-123", "Product 1", BigDecimal.TEN);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.addItems(orderId, items);

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Given a list of OrderItemDTO with duplicate products, should throw UniqueConstraintViolationException")
    void addItems_WhenDuplicateProducts_ShouldThrowUniqueConstraintViolationException() {
        Long orderId = 1L;
        List<OrderItemDTO> items = List.of(new OrderItemDTO(1L, 2, BigDecimal.TEN));
        Product product = new Product("SKU-123", "Product 1", BigDecimal.TEN);
        product.setId(1L);
        List<OrderItem> orderItems = List.of(new OrderItem(product, 1, BigDecimal.ZERO));
        Order order = new Order(orderItems, new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.CREATED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UniqueConstraintViolationException exception = assertThrows(UniqueConstraintViolationException.class, () -> orderService.addItems(orderId, items));
        assertEquals(String.format(DUPLICATE_PRODUCT, 1L), exception.getMessage());
    }

    @Test
    @DisplayName("Given an order that cannot be modified, should throw BadRequestException when adding items")
    void addItems_WhenOrderCannotBeModified_ShouldThrowBadRequestException() {
        Long orderId = 1L;
        List<OrderItemDTO> items = List.of(new OrderItemDTO(1L, 2, BigDecimal.TEN));
        Order order = new Order(Collections.emptyList(), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.PROCESSING);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> orderService.addItems(orderId, items));
        assertEquals(ORDER_ALREADY_PROCESSED, exception.getMessage());
    }

    @Test
    @DisplayName("Given a valid UpdateOrderItemDTO, should update the item successfully")
    void updateItem_WhenValidDto_ShouldUpdateItemSuccessfully() {
        Long orderId = 1L;
        Long productId = 1L;
        Product product = new Product("SKU-123", "Product 1", BigDecimal.TEN);
        product.setId(productId);
        UpdateOrderItemDTO dto = new UpdateOrderItemDTO(3, BigDecimal.valueOf(15.0));
        Order order = new Order(List.of(new OrderItem(product, 1, BigDecimal.ZERO)), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.CREATED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateItem(orderId, productId, dto);

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Given a non-existing product in the order, should throw NotFoundException when updating item")
    void updateItem_WhenProductNotFound_ShouldThrowNotFoundException() {
        Long orderId = 1L;
        Long productId = 1L;
        UpdateOrderItemDTO dto = new UpdateOrderItemDTO(3, BigDecimal.valueOf(15.0));
        Order order = new Order(Collections.emptyList(), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> orderService.updateItem(orderId, productId, dto));
        assertEquals(String.format(PRODUCT_NOT_FOUND, productId), exception.getMessage());
    }

    @Test
    @DisplayName("Given a valid order ID and product ID, should delete the item successfully")
    void deleteItem_WhenValidIds_ShouldDeleteItemSuccessfully() {
        Long orderId = 1L;
        Long productId = 1L;
        Product product = new Product("Sku-123", "Product 1", BigDecimal.TEN);
        product.setId(productId);
        Order order = new Order(new ArrayList<>(List.of(new OrderItem(product, 1, BigDecimal.ZERO))), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.deleteItem(orderId, productId);

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Given a non-existing product in the order, should throw NotFoundException when deleting item")
    void deleteItem_WhenProductNotFound_ShouldThrowNotFoundException() {
        Long orderId = 1L;
        Long productId = 1L;
        Product product = new Product("Sku-123", "Product 1", BigDecimal.TEN);
        product.setId(productId);
        Order order = new Order(List.of(new OrderItem(product, 1, BigDecimal.ZERO)), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> orderService.deleteItem(orderId, 2L));
        assertEquals(String.format(PRODUCT_NOT_FOUND, 2L), exception.getMessage());
    }

    @Test
    @DisplayName("Given a valid order ID, should delete the order successfully")
    void delete_WhenValidId_ShouldDeleteOrderSuccessfully() {
        Long orderId = 1L;
        Product product = new Product("Sku-123", "Product 1", BigDecimal.TEN);
        Order order = new Order(List.of(new OrderItem(product, 1, BigDecimal.ZERO)), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.CREATED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        orderService.delete(orderId);

        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    @DisplayName("Given a non-existing order ID, should throw NotFoundException when deleting order")
    void delete_WhenOrderNotFound_ShouldThrowNotFoundException() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> orderService.delete(orderId));
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Given filters and pageable, should list orders successfully")
    void listOrders_WhenValidFilters_ShouldListOrdersSuccessfully() {
        OrderStatus status = OrderStatus.CREATED;
        Long customerId = 1L;
        Long productId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Order order = new Order(Collections.emptyList(), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.CREATED);
        Page<Order> ordersPage = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(ordersPage);
        when(mapper.toOrderWithoutItems(order))
                .thenReturn(new OrderWithoutItems(order.getId(), order.getCustomer().getId(), order.getDiscount(), order.getStatus(),
                                BigDecimal.TEN, LocalDateTime.now(), LocalDateTime.now()
                        )
                );

        Page<OrderWithoutItems> result = orderService.listOrders(status, customerId, productId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Given a valid order ID, should return the OrderDTO")
    void getOrderById_WhenValidId_ShouldReturnOrderDto() {
        Long orderId = 1L;
        Order order = new Order(Collections.emptyList(), new Customer("Customer 1", "John Doe"), BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(orderId);
        OrderDTO orderDTO = new OrderDTO(1L, Collections.emptyList(), 1L, BigDecimal.ZERO,
                OrderStatus.CREATED, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());
        orderDTO.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(mapper.toOrder(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderDTO, result);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Given a non-existing order ID, should throw NotFoundException when getting order by ID")
    void getOrderById_WhenOrderNotFound_ShouldThrowNotFoundException() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> orderService.getOrderById(orderId));
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw PessimisticLockException when order is locked")
    void getOrderById_WhenOrderIsLocked_ShouldThrowPessimisticLockException() {
        Long orderId = 1L;

        when(orderRepository.findById(orderId)).thenThrow(new PessimisticLockingFailureException("Order is locked"));

        assertThrows(PessimisticLockingFailureException.class, () -> orderService.getOrderById(orderId));

        verify(orderRepository, times(1)).findById(orderId);
    }
}
