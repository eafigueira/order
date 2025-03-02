package br.com.order.application.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6225807608271488016L;
    @NotNull(message = "Product is required")
    private Long productId;
    @Positive(message = "Quantity should be positive")
    private Integer quantity;
    @Min(value = 0, message = "Price should be positive")
    private BigDecimal price;
}
