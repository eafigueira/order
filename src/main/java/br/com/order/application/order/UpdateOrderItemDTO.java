package br.com.order.application.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderItemDTO {
    @Positive(message = "Quantity should be positive")
    private Integer quantity;
    @Min(value = 0, message = "Price should be positive")
    private BigDecimal price;

}
