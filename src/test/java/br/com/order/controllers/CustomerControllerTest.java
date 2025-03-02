package br.com.order.controllers;

import br.com.order.application.customer.CreateCustomerDTO;
import br.com.order.application.customer.Customer;
import br.com.order.application.customer.CustomerRepository;
import br.com.order.application.customer.UpdateCustomerDTO;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(repository);
    }

    @Test
    @DisplayName("Given a valid CreateCustomerDTO, should create a customer and return 201")
    void create_WhenValidDto_ShouldReturnCreated() throws Exception {
        CreateCustomerDTO dto = new CreateCustomerDTO("John Doe", "john@example.com");
        Customer customer = new Customer("John Doe", "john@example.com");
        when(repository.save(any(Customer.class))).thenReturn(customer);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        verify(repository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Given a valid ID and UpdateCustomerDTO, should update the customer and return 200")
    void update_WhenValidIdAndDto_ShouldReturnOk() throws Exception {
        Long id = 1L;
        UpdateCustomerDTO dto = new UpdateCustomerDTO("Jane Doe", "jane@example.com");
        Customer existingCustomer = new Customer("John Doe", "john@example.com");
        existingCustomer.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existingCustomer));
        when(repository.save(any(Customer.class))).thenReturn(existingCustomer);

        mockMvc.perform(put("/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(existingCustomer);
    }

    @Test
    @DisplayName("Given a non-existent ID in the update, it should return 404 Not Found")
    void update_WhenIdNotFound_ShouldReturnNotFound() throws Exception {
        Long id = 1L;
        UpdateCustomerDTO dto = new UpdateCustomerDTO("Jane Doe", "jane@example.com");
        when(repository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
        verify(repository, times(1)).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Given a valid ID, should delete the client and return 204")
    void delete_WhenValidId_ShouldReturnNoContent() throws Exception {
        Long id = 1L;
        Customer customer = new Customer("John Doe", "john@example.com");
        customer.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(customer));
        doNothing().when(repository).delete(customer);

        mockMvc.perform(delete("/customers/{id}", id))
                .andExpect(status().isNoContent());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).delete(customer);
    }

    @Test
    @DisplayName("Given a valid ID, it should return the CustomerDTO with 200")
    void findById_WhenValidId_ShouldReturnCustomerDto() throws Exception {
        Long id = 1L;
        Customer customer = new Customer("John Doe", "john@example.com");
        customer.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("John Doe"));
        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Given a search term and pageable, should return a CustomerDTO page with 200")
    void findBySearch_WhenValidSearchAndPageable_ShouldReturnPageOfCustomerDto() throws Exception {
        String search = "John";
        Pageable pageable = PageRequest.of(0, 10);
        Customer customer = new Customer("John Doe", "john@example.com");
        customer.setId(1L);
        Page<Customer> customerPage = new PageImpl<>(List.of(customer), pageable, 1);
        when(repository.findByNameContainsIgnoreCase(search, pageable)).thenReturn(customerPage);

        mockMvc.perform(get("/customers")
                        .param("search", search)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));
        verify(repository, times(1)).findByNameContainsIgnoreCase(search, pageable);
    }
}
