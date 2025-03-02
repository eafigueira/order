package br.com.order.application.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {
    void create(CreateProductDTO dto);

    void update(Long id, UpdateProductDTO dto);

    ProductDTO findById(Long id);

    void delete(Long id);

    Page<ProductDTO> findBySearch(String search, Pageable pageable);
}
