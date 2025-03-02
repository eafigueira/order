package br.com.order.application.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateProductDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6753314556490275641L;
    @Size(message = "Sku cannot be greater than 50", min = 5, max = 50)
    @NotBlank(message = "Sku is required")
    private String sku;
    @Size(message = "Name cannot be greater than 150 ", max = 150)
    @NotBlank(message = "Name is required")
    private String name;
    @Positive(message = "Price should be positive")
    private BigDecimal price;
}