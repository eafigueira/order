package br.com.order.application.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateProductDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5940246337598569782L;
    @Size(message = "The SKU must be between 5 and 50 characters long.", min = 5, max = 50)
    @NotBlank(message = "The SKU is required.")
    private String sku;
    @Size(message = "Name cannot be greater than 150 ", max = 150)
    @NotBlank(message = "Name is required")
    private String name;
    @NotNull(message = "Price cannot be null")
    private BigDecimal price;
}