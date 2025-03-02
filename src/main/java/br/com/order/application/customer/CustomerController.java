package br.com.order.application.customer;

import br.com.order.application.base.ErrorDTO;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@OpenAPIDefinition(
        info = @Info(
                title = "Customer API",
                version = "1.0.0",
                description = "Customer API"
        )
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {
    private final ICustomerService service;

    @Operation(
            summary = "Create a new customer",
            description = "Create a new customer",
            tags = {"Customer"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Customer created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request. Check the request body.",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody @Valid CreateCustomerDTO dto) {
        service.create(dto);
    }

    @Operation(
            summary = "Update an existing customer",
            description = "Update a customer by its ID",
            tags = {"Customer"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request. Check the request body.",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
            }
    )
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @Valid @RequestBody UpdateCustomerDTO dto) {
        service.update(id, dto);
    }

    @Operation(
            summary = "Delete a customer",
            description = "Delete a customer by its ID",
            tags = {"Customer"},
            responses = {
                    @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Customer not found",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
            }
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @Operation(
            summary = "Get a customer by ID",
            description = "Retrieve a customer by its ID",
            tags = {"Customer"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer found",
                            content = @Content(schema = @Schema(implementation = CustomerDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
            }
    )
    @GetMapping("/{id}")
    public CustomerDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(
            summary = "Search customers",
            description = "Search customers with optional search term and pagination",
            tags = {"Customer"},
            parameters = {
                    @Parameter(name = "search", description = "Search term", required = false),
                    @Parameter(name = "page", description = "Page number", required = false),
                    @Parameter(name = "size", description = "Page size", required = false),
                    @Parameter(name = "sort", description = "Sort criteria", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customers retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping
    public Page<CustomerDTO> findBySearch(@RequestParam(required = false, defaultValue = "") String search,
                                          @Schema(hidden = true) Pageable pageable) {
        return service.findBySearch(search, pageable);
    }
}
