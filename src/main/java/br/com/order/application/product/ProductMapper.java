package br.com.order.application.product;

import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product create(CreateProductDTO dto) {
        Product product = new Product();
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        return product;
    }

    public void update(Product product, UpdateProductDTO dto) {
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
    }

    public ProductDTO productToProductDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
