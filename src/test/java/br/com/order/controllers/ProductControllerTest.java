package br.com.order.controllers;

import br.com.order.application.product.CreateProductDTO;
import br.com.order.application.product.Product;
import br.com.order.application.product.ProductRepository;
import br.com.order.application.product.UpdateProductDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(repository);
    }

    @Test
    @DisplayName("Given a valid CreateProductDTO with unique SKU, should create a product and return 201")
    void create_WhenValidDtoAndUniqueSku_ShouldReturnCreated() throws Exception {
        CreateProductDTO dto = new CreateProductDTO("SKU123", "Product 1", BigDecimal.TEN);
        Product product = new Product("Product 1", "SKU123", BigDecimal.TEN);
        when(repository.existsBySku(dto.getSku())).thenReturn(false);
        when(repository.save(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        verify(repository, times(1)).existsBySku(dto.getSku());
        verify(repository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Given a CreateProductDTO with existing SKU, should return 409 Conflict")
    void create_WhenSkuExists_ShouldReturnConflict() throws Exception {
        CreateProductDTO dto = new CreateProductDTO("SKU123", "Product 1", BigDecimal.TEN);
        when(repository.existsBySku(dto.getSku())).thenReturn(true);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
        verify(repository, times(1)).existsBySku(dto.getSku());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Given a valid ID and UpdateProductDTO with unchanged SKU, it should update the product and return 200")
    void update_WhenValidIdAndUnchangedSku_ShouldReturnOk() throws Exception {
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO("SKU123", "Product Updated", BigDecimal.valueOf(15.0));
        Product existingProduct = new Product("Product 1", "SKU123", BigDecimal.TEN);
        existingProduct.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(repository.save(any(Product.class))).thenReturn(existingProduct);

        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("Given a valid ID and UpdateProductDTO with new and unique SKU, it should update the product and return 200")
    void update_WhenValidIdAndNewUniqueSku_ShouldReturnOk() throws Exception {
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO("SKU456", "Product Updated", BigDecimal.valueOf(15.0));
        Product existingProduct = new Product("Product 1", "SKU123", BigDecimal.TEN);
        existingProduct.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(repository.existsBySku(dto.getSku())).thenReturn(false);
        when(repository.save(any(Product.class))).thenReturn(existingProduct);

        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).existsBySku(dto.getSku());
        verify(repository, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("Given a valid ID and UpdateProductDTO with existing SKU, should return 409 Conflict")
    void update_WhenSkuExists_ShouldReturnConflict() throws Exception {
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO("SKU456", "Product Updated", BigDecimal.valueOf(15.0));
        Product existingProduct = new Product("Product 1", "SKU123", BigDecimal.TEN);
        existingProduct.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(repository.existsBySku(dto.getSku())).thenReturn(true);

        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).existsBySku(dto.getSku());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Given a non-existent ID in the update, it should return 404 Not Found")
    void update_WhenIdNotFound_ShouldReturnNotFound() throws Exception {
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO("SKU123", "Product Updated", BigDecimal.valueOf(15.0));
        when(repository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
        verify(repository, times(1)).findById(id);
        verify(repository, never()).existsBySku(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Given a valid ID, it should delete the product and return 204")
    void delete_WhenValidId_ShouldReturnNoContent() throws Exception {
        Long id = 1L;
        Product product = new Product("SKU123", "Product 1", BigDecimal.TEN);
        product.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(product));
        doNothing().when(repository).delete(product);

        // Act & Assert
        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNoContent());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Given a non-existent ID on delete, it should return 404 Not Found")
    void delete_WhenIdNotFound_ShouldReturnNotFound() throws Exception {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNotFound());
        verify(repository, times(1)).findById(id);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Given a valid ID, should return the ProductDTO with 200")
    void findById_WhenValidId_ShouldReturnProductDto() throws Exception {
        Long id = 1L;
        Product product = new Product("SKU123", "Product 1", BigDecimal.TEN);
        product.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Product 1"))
                .andExpect(jsonPath("$.sku").value("SKU123"))
                .andExpect(jsonPath("$.price").value(BigDecimal.TEN));
        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Given a non-existent ID in findById, it should return 404 Not Found")
    void findById_WhenIdNotFound_ShouldReturnNotFound() throws Exception {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound());
        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Given a search term and pageable, should return a ProductDTO page with 200")
    void findBySearch_WhenValidSearchAndPageable_ShouldReturnPageOfProductDto() throws Exception {
        String search = "Product";
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product("SKU123", "Product 1", BigDecimal.TEN);
        product.setId(1L);
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);
        when(repository.findByNameContainsIgnoreCaseOrSkuContainsIgnoreCase(search, search, pageable))
                .thenReturn(productPage);

        mockMvc.perform(get("/products")
                        .param("search", search)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Product 1"))
                .andExpect(jsonPath("$.content[0].sku").value("SKU123"))
                .andExpect(jsonPath("$.content[0].price").value(BigDecimal.TEN))
                .andExpect(jsonPath("$.totalElements").value(1));
        verify(repository, times(1))
                .findByNameContainsIgnoreCaseOrSkuContainsIgnoreCase(search, search, pageable);
    }
}
