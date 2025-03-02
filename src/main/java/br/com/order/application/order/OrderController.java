package br.com.order.application.order;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@OpenAPIDefinition(
        info = @Info(
                title = "Orders API",
                version = "1.0.0",
                description = "Orders API"
        )
)
@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService service;

    @Operation(
            summary = "Create a new order",
            description = "Creates a new order with the provided details. The order will be initialized with 'CREATED' status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid data or duplicate products in the order"),
            @ApiResponse(responseCode = "404", description = "Customer or product not found")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@RequestBody @Valid CreateOrderDTO createOrder) {
        log.info("Creating order: {}", createOrder);
        service.create(createOrder);
    }

    @Operation(
            summary = "Update an existing order",
            description = "Updates an order's details. Only orders in 'CREATED' status can be fully updated (items, customer, discount). " +
                    "For other statuses, only the status can be changed."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid data, duplicate products, invalid status transition, " +
                    "or order already processed"),
            @ApiResponse(responseCode = "404", description = "Order, customer, or product not found")
    })
    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody UpdateOrderDTO updateOrder) {
        log.info("Updating order: {}", updateOrder);
        service.update(id, updateOrder);
    }

    @Operation(
            summary = "Add items to an order",
            description = "Adds new items to an existing order. The order must be in 'CREATED' status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Items added successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - duplicate products or order already processed"),
            @ApiResponse(responseCode = "404", description = "Order or product not found")
    })
    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.OK)
    public void addItems(@PathVariable Long id, @RequestBody List<@Valid OrderItemDTO> orderItems) {
        log.info("Adding items to order id: {}", id);
        service.addItems(id, orderItems);
    }

    @Operation(
            summary = "Update an item in an order",
            description = "Updates the quantity and price of a specific item in an order. The order must be in 'CREATED' status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - order already processed"),
            @ApiResponse(responseCode = "404", description = "Order or product not found")
    })
    @PutMapping("/{orderId}/items/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateItem(@PathVariable Long orderId, @PathVariable Long productId,
                           @RequestBody @Valid UpdateOrderItemDTO updateOrderItem) {
        service.updateItem(orderId, productId, updateOrderItem);
    }

    @Operation(
            summary = "Delete an item from an order",
            description = "Removes a specific item from an order. The order must be in 'CREATED' status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - order already processed"),
            @ApiResponse(responseCode = "404", description = "Order or product not found")
    })
    @DeleteMapping("/{orderId}/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long orderId, @PathVariable Long productId) {
        service.deleteItem(orderId, productId);
    }

    @Operation(
            summary = "Delete an order",
            description = "Deletes an existing order. Only orders in 'CREATED' status can be deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - order already processed"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long orderId) {
        service.delete(orderId);
    }

    @Operation(
            summary = "List orders with filters",
            description = "Retrieves a paginated list of orders, optionally filtered by status, customer ID, or product ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<OrderWithoutItems>> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long productId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<OrderWithoutItems> orders = service.listOrders(status, customerId, productId, pageable);

        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Get an order by ID",
            description = "Retrieves the details of an order by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrderById(id));
    }

}
