package br.com.order.application.customer;

import br.com.order.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements ICustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Transactional
    @Override
    public void create(CreateCustomerDTO dto) {
        log.info("Creating customer: {}", dto);
        repository.save(mapper.create(dto));
    }

    @Transactional
    @Override
    public void update(Long id, UpdateCustomerDTO dto) {
        log.info("Updating customer: {}", dto);
        Customer customer = repository.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
        mapper.update(customer, dto);
        repository.save(customer);
    }

    @Transactional
    @Override
    public CustomerDTO findById(Long id) {
        log.info("Finding customer: {}", id);
        return repository.findById(id)
                .map(mapper::customerToCustomerDTO)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        log.info("Deleting customer: {}", id);
        Customer customer = repository.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
        repository.delete(customer);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CustomerDTO> findBySearch(String search, Pageable pageable) {
        log.info("Finding customers by search: {}", search);
        return repository.findByNameContainsIgnoreCase(search, pageable)
                .map(mapper::customerToCustomerDTO);
    }
}


