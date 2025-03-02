package br.com.order.application.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICustomerService {
    void create(CreateCustomerDTO dto);

    void update(Long id, UpdateCustomerDTO dto);

    CustomerDTO findById(Long id);

    void delete(Long id);

    Page<CustomerDTO> findBySearch(String search, Pageable pageable);
}
