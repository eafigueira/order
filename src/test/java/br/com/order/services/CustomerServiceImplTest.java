package br.com.order.services;

import br.com.order.application.customer.*;
import br.com.order.exceptions.NotFoundException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerMapper mapper;

    @InjectMocks
    private CustomerServiceImpl service;

    @Test
    @DisplayName("Given a valid CreateCustomerDTO, should create a customer successfully")
    void create_WhenValidDto_ShouldCreateCustomerSuccessfully() {
        CreateCustomerDTO dto = new CreateCustomerDTO("John Doe", "john@example.com");
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setPhone(dto.getPhone());
        when(mapper.create(dto)).thenReturn(customer);

        service.create(dto);

        verify(repository, times(1)).save(customer);
        verify(mapper, times(1)).create(dto);
    }

    @Test
    @DisplayName("Given a valid ID and UpdateCustomerDTO, should update the customer successfully")
    void update_WhenValidIdAndDto_ShouldUpdateCustomerSuccessfully() {
        Long id = 1L;
        UpdateCustomerDTO dto = new UpdateCustomerDTO("Jane Doe", "jane@example.com");
        Customer existingCustomer = new Customer("John Doe", "john@example.com");
        existingCustomer.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existingCustomer));

        service.update(id, dto);

        verify(repository, times(1)).findById(id);
        verify(mapper, times(1)).update(existingCustomer, dto);
        verify(repository, times(1)).save(existingCustomer);
    }

    @Test
    @DisplayName("Given a non-existent ID in update, should throw NotFoundException")
    void update_WhenIdNotFound_ShouldThrowNotFoundException() {
        Long id = 1L;
        UpdateCustomerDTO dto = new UpdateCustomerDTO("Jane Doe", "jane@example.com");
        when(repository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.update(id, dto));
        assertEquals("Customer not found", exception.getMessage());
        verify(repository, times(1)).findById(id);
        verify(mapper, never()).update(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Given a valid ID, should return the corresponding CustomerDTO")
    void findById_WhenValidId_ShouldReturnCustomerDto() {
        Long id = 1L;
        Customer customer = new Customer("John Doe", "john@example.com");
        customer.setId(id);
        CustomerDTO customerDTO = new CustomerDTO(id, "John Doe", "john@example.com", LocalDateTime.now(), LocalDateTime.now());
        when(repository.findById(id)).thenReturn(Optional.of(customer));
        when(mapper.customerToCustomerDTO(customer)).thenReturn(customerDTO);

        CustomerDTO result = service.findById(id);

        assertNotNull(result);
        assertEquals(customerDTO, result);
        verify(repository, times(1)).findById(id);
        verify(mapper, times(1)).customerToCustomerDTO(customer);
    }

    @Test
    @DisplayName("Given a non-existent ID in findById, should throw NotFoundException")
    void findById_WhenIdNotFound_ShouldThrowNotFoundException() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.findById(id));
        assertEquals("Customer not found", exception.getMessage());
        verify(repository, times(1)).findById(id);
        verify(mapper, never()).customerToCustomerDTO(any());
    }

    @Test
    @DisplayName("Given a valid ID, should delete the customer successfully")
    void delete_WhenValidId_ShouldDeleteCustomerSuccessfully() {
        Long id = 1L;
        Customer customer = new Customer("John Doe", "john@example.com");
        customer.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(customer));

        service.delete(id);

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).delete(customer);
    }

    @Test
    @DisplayName("Given a non-existent ID in delete, should throw NotFoundException")
    void delete_WhenIdNotFound_ShouldThrowNotFoundException() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.delete(id));
        assertEquals("Customer not found", exception.getMessage());
        verify(repository, times(1)).findById(id);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Given a search term and pageable, should return a page of CustomerDTO")
    void findBySearch_WhenValidSearchAndPageable_ShouldReturnPageOfCustomerDto() {
        String search = "John";
        Pageable pageable = PageRequest.of(0, 10);
        Customer customer = new Customer("John Doe", "john@example.com");
        customer.setId(1L);
        CustomerDTO customerDTO = new CustomerDTO(1L, "John Doe", "john@example.com", LocalDateTime.now(), LocalDateTime.now());
        Page<Customer> customerPage = new PageImpl<>(List.of(customer), pageable, 1);
        when(repository.findByNameContainsIgnoreCase(search, pageable)).thenReturn(customerPage);
        when(mapper.customerToCustomerDTO(customer)).thenReturn(customerDTO);

        Page<CustomerDTO> result = service.findBySearch(search, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(customerDTO, result.getContent().get(0));
        verify(repository, times(1)).findByNameContainsIgnoreCase(search, pageable);
        verify(mapper, times(1)).customerToCustomerDTO(customer);
    }
}