package com.micro.ecommerce.service;

import com.micro.ecommerce.dto.CustomerRequest;
import com.micro.ecommerce.dto.CustomerResponse;
import com.micro.ecommerce.entity.Customer;
import com.micro.ecommerce.exception.CustomerNotFoundException;
import com.micro.ecommerce.mapper.CustomerMapper;
import com.micro.ecommerce.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

     public String createCustomer(CustomerRequest customerRequest) {
        var customer = customerRepository.save(customerMapper.toCustomer(customerRequest));
        return customer.getId();
    }

    public void updateCustomer(CustomerRequest customerRequest) {
        var customer = customerRepository.findById(customerRequest.id())
                .orElseThrow(() -> new CustomerNotFoundException(
                        format("Cannot update customer:: No such customer: %s", customerRequest.id())
                ));
        mergerCustomer(customer, customerRequest);
        customerRepository.save(customer);
    }

    private void mergerCustomer(Customer customer, CustomerRequest customerRequest) {
        if (StringUtils.isNotBlank(customerRequest.firstName())) {
            customer.setFirstname(customerRequest.firstName());
        }
        if (StringUtils.isNotBlank(customerRequest.lastName())) {
            customer.setFirstname(customerRequest.lastName());
        }
        if (StringUtils.isNotBlank(customerRequest.email())) {
            customer.setFirstname(customerRequest.email());
        }
        if (customerRequest.address() != null){
            customer.setAddress(customerRequest.address());
        }
    }

    public List<CustomerResponse> findAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::fromCustomer)
                .collect(Collectors.toList());
    }

    public Boolean existsById(String customerId) {
        return customerRepository.findById(customerId)
                .isPresent();
    }

    public CustomerResponse findById(String customerId) {
         return customerRepository.findById(customerId)
                 .map(customerMapper::fromCustomer)
                 .orElseThrow(() -> new CustomerNotFoundException(
                         format("Cannot find customer:: No such customer: %s", customerId)
                 ));
    }

    public void deleteCustomer(String customerId) {
         customerRepository.deleteById(customerId);
    }
}
