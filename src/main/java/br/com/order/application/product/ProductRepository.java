package br.com.order.application.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainsIgnoreCaseOrSkuContainsIgnoreCase(String name, String sku, Pageable pageable);

    boolean existsBySku(String sku);
}