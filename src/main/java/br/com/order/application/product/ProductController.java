package br.com.order.application.product;

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
                title = "Product API",
                version = "1.0.0",
                description = "Product API"
        )
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final IProductService service;

    @Operation(
            summary = "Create a new product",
            description = "Create a new product",
            tags = {"Product"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Product created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request. Check the request body.",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict - SKU already exists",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody @Valid CreateProductDTO dto) {
        service.create(dto);
    }

    @Operation(
            summary = "Update an existing product",
            description = "Update a product by its ID",
            tags = {"Product"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request. Check the request body.",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Product not found",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict - SKU already exists",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
            }
    )
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @Valid @RequestBody UpdateProductDTO dto) {
        service.update(id, dto);
    }

    @Operation(
            summary = "Delete a product",
            description = "Delete a product by its ID",
            tags = {"Product"},
            responses = {
                    @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Product not found",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
            }
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @Operation(
            summary = "Get a product by ID",
            description = "Retrieve a product by its ID",
            tags = {"Product"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product found",
                            content = @Content(schema = @Schema(implementation = ProductDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Product not found",
                            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
            }
    )
    @GetMapping("/{id}")
    public ProductDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(
            summary = "Search products",
            description = "Search products with optional search term and pagination",
            tags = {"Product"},
            parameters = {
                    @Parameter(name = "search", description = "Search term"),
                    @Parameter(name = "page", description = "Page number"),
                    @Parameter(name = "size", description = "Page size"),
                    @Parameter(name = "sort", description = "Sort criteria")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Page.class)))
            }
    )
    @GetMapping
    public Page<ProductDTO> findBySearch(@RequestParam(required = false, defaultValue = "") String search,
                                         @Schema(hidden = true) Pageable pageable) {
        return service.findBySearch(search, pageable);
    }
}
