package br.com.order.application.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -119512220177216378L;
    @NotEmpty(message = "Items cannot be empty")
    @Size(min = 1, message = "The order must contain at least one item.")
    private List<@Valid OrderItemDTO> items;
    @NotNull(message = "Customer is required")
    private Long customerId;
    @NotNull(message = "Discount cannot be null")
    private BigDecimal discount;
}
