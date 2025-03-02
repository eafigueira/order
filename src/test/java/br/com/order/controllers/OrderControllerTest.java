package br.com.order.controllers;

import br.com.order.application.customer.Customer;
import br.com.order.application.customer.CustomerRepository;
import br.com.order.application.order.*;
import br.com.order.application.product.Product;
import br.com.order.application.product.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(orderRepository, customerRepository, productRepository);
    }

    @Test
    @DisplayName("Given a valid CreateOrderDTO, should create an order and return 201")
    void create_WhenValidDto_ShouldReturnCreated() throws Exception {
        CreateOrderDTO dto = new CreateOrderDTO(List.of(new OrderItemDTO(1L, 2, BigDecimal.TEN)), 1L, BigDecimal.ZERO);
        Customer customer = new Customer("Customer 1", "João Silva");
        customer.setId(1L);
        Product product = new Product("SKU-1234", "Produto 1", BigDecimal.TEN);
        product.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Given a CreateOrderDTO with non-existent customer, it should return 404")
    void create_WhenCustomerNotFound_ShouldReturnNotFound() throws Exception {
        CreateOrderDTO dto = new CreateOrderDTO(List.of(new OrderItemDTO(1L, 2, BigDecimal.TEN)), 1L, BigDecimal.ZERO);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Given a valid ID and UpdateOrderDTO, it should update the order and return 200")
    void update_WhenValidIdAndDto_ShouldReturnOk() throws Exception {
        Long id = 1L;
        UpdateOrderDTO dto = new UpdateOrderDTO(List.of(new OrderItemDTO(1L, 3, BigDecimal.valueOf(15.0))),
                1L, BigDecimal.valueOf(5.0), null);
        Customer customer = new Customer("Customer 1", "João Silva");
        customer.setId(1L);
        Product product = new Product("SKU-1234", "Produto 1", BigDecimal.valueOf(15.0));
        product.setId(1L);
        Order order = new Order(new ArrayList<>(), customer, BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(id);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(patch("/orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Given an invalid ID, should return 404 when trying to update")
    void update_WhenInvalidId_ShouldReturnNotFound() throws Exception {
        Long id = 1L;
        UpdateOrderDTO dto = new UpdateOrderDTO(List.of(new OrderItemDTO(1L, 3, BigDecimal.valueOf(15.0))),
                1L, BigDecimal.valueOf(5.0), null);

        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Given a valid ID and list of OrderItemDTO, should add items and return 200")
    void addItems_WhenValidIdAndItems_ShouldReturnOk() throws Exception {
        Long id = 1L;
        List<OrderItemDTO> items = List.of(new OrderItemDTO(1L, 2, BigDecimal.TEN));
        Customer customer = new Customer("Customer 1", "João Silva");
        customer.setId(1L);
        Product product = new Product("SKU-1234", "Produto 1", BigDecimal.TEN);
        product.setId(1L);
        Order order = new Order(new ArrayList<>(), customer, BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(id);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/orders/{id}/items", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isOk());

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Given an invalid ID when adding items, should return 404")
    void addItems_WhenInvalidId_ShouldReturnNotFound() throws Exception {
        Long id = 1L;
        List<OrderItemDTO> items = List.of(new OrderItemDTO(1L, 2, BigDecimal.TEN));

        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(post("/orders/{id}/items", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isNotFound());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Given a valid orderId, productId and UpdateOrderItemDTO, should update the item and return 200")
    void updateItem_WhenValidIdsAndDto_ShouldReturnOk() throws Exception {
        Long orderId = 1L;
        Long productId = 1L;
        UpdateOrderItemDTO dto = new UpdateOrderItemDTO(3, BigDecimal.valueOf(15.0));
        Customer customer = new Customer("Customer 1", "João Silva");
        customer.setId(1L);
        Product product = new Product("SKU-1234", "Produto 1", BigDecimal.TEN);
        product.setId(productId);
        OrderItem orderItem = new OrderItem(product, 2, BigDecimal.TEN);
        Order order = new Order(new ArrayList<>(List.of(orderItem)), customer, BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(put("/orders/{orderId}/items/{productId}", orderId, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Given an invalid orderId when updating item, should return 404")
    void updateItem_WhenInvalidOrderId_ShouldReturnNotFound() throws Exception {
        Long orderId = 1L;
        Long productId = 1L;
        UpdateOrderItemDTO dto = new UpdateOrderItemDTO(3, BigDecimal.valueOf(15.0));

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/orders/{orderId}/items/{productId}", orderId, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Given a valid orderId and productId, should delete the item and return 204")
    void deleteItem_WhenValidIds_ShouldReturnNoContent() throws Exception {
        Long orderId = 1L;
        Long productId = 1L;
        Customer customer = new Customer("Customer 1", "João Silva");
        customer.setId(1L);
        Product product = new Product("SKU-1234", "Produto 1", BigDecimal.TEN);
        product.setId(productId);
        OrderItem orderItem = new OrderItem(product, 2, BigDecimal.TEN);
        Order order = new Order(new ArrayList<>(List.of(orderItem)), customer, BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(delete("/orders/{orderId}/items/{productId}", orderId, productId))
                .andExpect(status().isNoContent());

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Given an invalid orderId when deleting item, should return 404")
    void deleteItem_WhenInvalidOrderId_ShouldReturnNotFound() throws Exception {
        Long orderId = 1L;
        Long productId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/orders/{orderId}/items/{productId}", orderId, productId))
                .andExpect(status().isNotFound());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Given a valid orderId, should delete the order and return 204")
    void deleteOrder_WhenValidId_ShouldReturnNoContent() throws Exception {
        Long orderId = 1L;
        Customer customer = new Customer("Customer 1", "João Silva");
        customer.setId(1L);
        Order order = new Order(new ArrayList<>(), customer, BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        mockMvc.perform(delete("/orders/{orderId}", orderId))
                .andExpect(status().isNoContent());

        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    @DisplayName("Given an invalid orderId when deleting order, should return 404")
    void deleteOrder_WhenInvalidId_ShouldReturnNotFound() throws Exception {
        Long orderId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/orders/{orderId}", orderId))
                .andExpect(status().isNotFound());

        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    @DisplayName("Given valid filters and pagination, it should list orders and return 200")
    void listOrders_WhenValidFilters_ShouldReturnOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Customer customer = new Customer("Customer 1", "João Silva");
        customer.setId(1L);
        Order order = new Order(new ArrayList<>(), customer, BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(1L);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/orders")
                        .param("status", "CREATED")
                        .param("customerId", "1")
                        .param("productId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].customerId").value(1L))
                .andExpect(jsonPath("$.content[0].discount").value(0.0))
                .andExpect(jsonPath("$.content[0].status").value("CREATED"))
                .andExpect(jsonPath("$.content[0].total").exists())
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                .andExpect(jsonPath("$.content[0].updatedAt").exists());

        verify(orderRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("Given a valid ID, the order should return with status 200")
    void getOrderById_WhenValidId_ShouldReturnOrder() throws Exception {
        Long id = 1L;
        Customer customer = new Customer("Customer 1", "João Silva");
        customer.setId(1L);
        Product product = new Product("SKU-1234", "Produto 1", BigDecimal.TEN);
        product.setId(1L);
        OrderItem orderItem = new OrderItem(product, 2, BigDecimal.TEN);
        Order order = new Order(new ArrayList<>(List.of(orderItem)), customer, BigDecimal.ZERO, OrderStatus.CREATED);
        order.setId(id);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.items[0].productId").value(1L))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].price").value(10.0))
                .andExpect(jsonPath("$.discount").value(0.0))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(orderRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Given an invalid ID, should return 404 when fetching request")
    void getOrderById_WhenInvalidId_ShouldReturnNotFound() throws Exception {
        Long id = 1L;

        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]").value("Order not found"));

        verify(orderRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should return 409 Conflict when order is locked")
    void getOrderById_WhenOrderIsLocked_ShouldReturnConflict() throws Exception {

        Long id = 1L;

        when(orderRepository.findById(id)).thenThrow(new PessimisticLockingFailureException("Order is locked"));

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("O registro está sendo atualizado por outro processo. Tente novamente mais tarde."));

        verify(orderRepository, times(1)).findById(id);
    }


}