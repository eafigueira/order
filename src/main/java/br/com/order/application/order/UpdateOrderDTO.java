package br.com.order.application.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Negative;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderDTO {
    private List<@Valid OrderItemDTO> items;
    private Long customerId;
    @Negative(message = "Discount should be positive")
    private BigDecimal discount;
    private OrderStatus status;
}
