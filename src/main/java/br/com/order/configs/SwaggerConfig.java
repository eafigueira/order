package br.com.order.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Order",
        description = "API documentation for Order microservice",
        version = "0.0.1",
        contact = @Contact(
            name = "Mouts",
            url = "https://www.mouts.info/"
        )
    )
)
public class SwaggerConfig {
}
