package br.com.order.application.product;

import br.com.order.exceptions.NotFoundException;
import br.com.order.exceptions.UniqueConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Transactional
    @Override
    public void create(CreateProductDTO dto) {
        log.info("Creating product: {}", dto);

        if (repository.existsBySku(dto.getSku())) {
            throw new UniqueConstraintViolationException("SKU '" + dto.getSku() + "' already exists");
        }

        repository.save(mapper.create(dto));
    }

    @Transactional
    @Override
    public void update(Long id, UpdateProductDTO dto) {
        log.info("Updating product: {}", dto);
        Product product = repository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));

        if (!product.getSku().equalsIgnoreCase(dto.getSku()) && repository.existsBySku(dto.getSku())) {
            throw new UniqueConstraintViolationException("SKU '" + dto.getSku() + "' already exists");
        }

        mapper.update(product, dto);
        repository.save(product);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductDTO findById(Long id) {
        log.info("Finding product: {}", id);
        return repository.findById(id)
                .map(mapper::productToProductDTO)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        log.info("Deleting product: {}", id);
        Product product = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        repository.delete(product);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductDTO> findBySearch(String search, Pageable pageable) {
        log.info("Finding products by search: {}", search);
        return repository.findByNameContainsIgnoreCaseOrSkuContainsIgnoreCase(search, search, pageable)
                .map(mapper::productToProductDTO);
    }
}
