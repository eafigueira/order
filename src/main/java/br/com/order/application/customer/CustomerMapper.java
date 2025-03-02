package br.com.order.application.customer;

import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer create(CreateCustomerDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setPhone(dto.getPhone());
        return customer;
    }

    public void update(Customer customer, UpdateCustomerDTO dto) {
        customer.setName(dto.getName());
        customer.setPhone(dto.getPhone());
    }

    public CustomerDTO customerToCustomerDTO(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
