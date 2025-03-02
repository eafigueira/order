package br.com.order.application.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCustomerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2444515578643081972L;

    @Size(message = "Name cannot be greater than 150", max = 150)
    @NotBlank(message = "Name is required")
    private String name;
    @Size(message = "Phone cannot be greater than 25", max = 25)
    @NotBlank(message = "Phone is required")
    private String phone;
}