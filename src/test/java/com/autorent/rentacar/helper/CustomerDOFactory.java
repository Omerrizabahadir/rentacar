package com.autorent.rentacar.helper;

import com.autorent.rentacar.model.Customer;

public class CustomerDOFactory {
    public Customer getCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail("test@gmail.com");
        customer.setFirstName("test_firstName");
        customer.setLastName("test_lastName");
        customer.setPassword("Passw0rd!");
        customer.setRoles("ROLE_USER");

        return customer;
    }
}
