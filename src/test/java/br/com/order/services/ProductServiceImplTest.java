package br.com.order.services;

import br.com.order.application.product.*;
import br.com.order.exceptions.NotFoundException;
import br.com.order.exceptions.UniqueConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    @DisplayName("Given a valid CreateProductDTO with unique SKU, should create a product successfully")
    void create_WhenValidDtoAndUniqueSku_ShouldCreateProductSuccessfully() {
        CreateProductDTO dto = new CreateProductDTO("SKU123", "Product1", BigDecimal.TEN);
        Product product = new Product("SKU123", "Product1", BigDecimal.TEN);
        when(repository.existsBySku(dto.getSku())).thenReturn(false);
        when(mapper.create(dto)).thenReturn(product);

        service.create(dto);

        verify(repository, times(1)).existsBySku(dto.getSku());
        verify(mapper, times(1)).create(dto);
        verify(repository, times(1)).save(product);
    }

    @Test
    @DisplayName("Given a CreateProductDTO with existing SKU, should throw UniqueConstraintViolationException")
    void create_WhenSkuExists_ShouldThrowUniqueConstraintViolationException() {
        CreateProductDTO dto = new CreateProductDTO("SKU123", "Product1", BigDecimal.TEN);
        when(repository.existsBySku(dto.getSku())).thenReturn(true);

        UniqueConstraintViolationException exception = assertThrows(UniqueConstraintViolationException.class,
                () -> service.create(dto));
        assertEquals("SKU 'SKU123' already exists", exception.getMessage());
        verify(repository, times(1)).existsBySku(dto.getSku());
        verify(mapper, never()).create(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Given a valid ID and UpdateProductDTO with unchanged SKU, should update product successfully")
    void update_WhenValidIdAndUnchangedSku_ShouldUpdateProductSuccessfully() {
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO("SKU123", "Updated Product", BigDecimal.valueOf(15.0));
        Product existingProduct = new Product("SKU123", "Product1", BigDecimal.TEN);
        existingProduct.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existingProduct));

        service.update(id, dto);

        verify(repository, times(1)).findById(id);
        verify(repository, times(0)).existsBySku(dto.getSku());
        verify(mapper, times(1)).update(existingProduct, dto);
        verify(repository, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("Given a valid ID and UpdateProductDTO with new unique SKU, should update product successfully")
    void update_WhenValidIdAndNewUniqueSku_ShouldUpdateProductSuccessfully() {
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO("SKU456", "Updated Product", BigDecimal.valueOf(15.0));
        Product existingProduct = new Product("SKU123", "Product1", BigDecimal.TEN);
        existingProduct.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(repository.existsBySku(dto.getSku())).thenReturn(false);

        service.update(id, dto);

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).existsBySku(dto.getSku());
        verify(mapper, times(1)).update(existingProduct, dto);
        verify(repository, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("Given a valid ID and UpdateProductDTO with existing SKU, should throw UniqueConstraintViolationException")
    void update_WhenSkuExists_ShouldThrowUniqueConstraintViolationException() {
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO("SKU456", "Updated Product", BigDecimal.valueOf(15.0));
        Product existingProduct = new Product("SKU123", "Product1", BigDecimal.TEN);
        existingProduct.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(repository.existsBySku(dto.getSku())).thenReturn(true);

        UniqueConstraintViolationException exception = assertThrows(UniqueConstraintViolationException.class,
                () -> service.update(id, dto));
        assertEquals("SKU 'SKU456' already exists", exception.getMessage());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).existsBySku(dto.getSku());
        verify(mapper, never()).update(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Given a non-existent ID in update, should throw NotFoundException")
    void update_WhenIdNotFound_ShouldThrowNotFoundException() {
        Long id = 1L;
        UpdateProductDTO dto = new UpdateProductDTO("SKU123", "Updated Product", BigDecimal.valueOf(15.0));
        when(repository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.update(id, dto));
        assertEquals("Product not found", exception.getMessage());
        verify(repository, times(1)).findById(id);
        verify(repository, never()).existsBySku(any());
        verify(mapper, never()).update(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Given a valid ID, should return the corresponding ProductDTO")
    void findById_WhenValidId_ShouldReturnProductDto() {
        Long id = 1L;
        Product product = new Product("SKU123", "Product1", BigDecimal.TEN);
        product.setId(id);
        ProductDTO productDTO = new ProductDTO(id, "SKU123", "Product1", BigDecimal.TEN, LocalDateTime.now(), LocalDateTime.now());
        when(repository.findById(id)).thenReturn(Optional.of(product));
        when(mapper.productToProductDTO(product)).thenReturn(productDTO);

        ProductDTO result = service.findById(id);

        assertNotNull(result);
        assertEquals(productDTO, result);
        verify(repository, times(1)).findById(id);
        verify(mapper, times(1)).productToProductDTO(product);
    }

    @Test
    @DisplayName("Given a non-existent ID in findById, should throw NotFoundException")
    void findById_WhenIdNotFound_ShouldThrowNotFoundException() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.findById(id));
        assertEquals("Product not found", exception.getMessage());
        verify(repository, times(1)).findById(id);
        verify(mapper, never()).productToProductDTO(any());
    }

    @Test
    @DisplayName("Given a valid ID, should delete the product successfully")
    void delete_WhenValidId_ShouldDeleteProductSuccessfully() {
        Long id = 1L;
        Product product = new Product("SKU123", "Product1", BigDecimal.TEN);
        product.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(product));

        service.delete(id);

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Given a non-existent ID in delete, should throw NotFoundException")
    void delete_WhenIdNotFound_ShouldThrowNotFoundException() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.delete(id));
        assertEquals("Product not found", exception.getMessage());
        verify(repository, times(1)).findById(id);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Given a search term and pageable, should return a page of ProductDTO")
    void findBySearch_WhenValidSearchAndPageable_ShouldReturnPageOfProductDto() {
        String search = "Product";
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product("SKU123", "Product1", BigDecimal.TEN);
        product.setId(1L);
        ProductDTO productDTO = new ProductDTO(1L, "SKU123", "Product1", BigDecimal.TEN, LocalDateTime.now(), LocalDateTime.now());
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);
        when(repository.findByNameContainsIgnoreCaseOrSkuContainsIgnoreCase(search, search, pageable))
                .thenReturn(productPage);
        when(mapper.productToProductDTO(product)).thenReturn(productDTO);

        Page<ProductDTO> result = service.findBySearch(search, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(productDTO, result.getContent().get(0));
        verify(repository, times(1))
                .findByNameContainsIgnoreCaseOrSkuContainsIgnoreCase(search, search, pageable);
        verify(mapper, times(1)).productToProductDTO(product);
    }
}