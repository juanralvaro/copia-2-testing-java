package com.certidevs.controller;

import com.certidevs.model.Customer;
import com.certidevs.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.java.Log;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@Slf4j
@AllArgsConstructor
@RestController
public class CustomerController {

    public static final double IVA_21 = 1.21;

    private CustomerRepository customerRepository;

    // Métodos GET
    // Método que nos devuelva un saludo

    @GetMapping("welcome") // localhost:8080/welcome
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Bienvenidos a un controlador de Spring");
    }

    // Método que captura un parámetro de la URL
    @GetMapping("user") // localhost:8080/user?name=Daniela
    public ResponseEntity<String> getUserName(@RequestParam String name) {
        return ResponseEntity.ok("Welcome user " + name);
    }

    // Métodos CRUD
    // Método que nos devuelva todos los clientes
    @GetMapping("customers") // localhost:8080/customers
    public ResponseEntity<List<Customer>> findAll() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    // Método que devuelva un cliente por su ID
    @GetMapping("customers/{id}") // localhost:8080/customers/1
    public ResponseEntity<Customer> findById(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(customer -> {
                    return ResponseEntity.ok(customer);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Método POST
    // Método que nos permite crear un nuevo cliente

    @PostMapping("customers")
    public ResponseEntity<Customer> create(@RequestBody Customer customer) {
        if(customer.getId() != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        customerRepository.save(customer); // obtiene un ID
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PostMapping("customers/filter")
    public ResponseEntity<List<Customer>> findByFilter(@RequestBody Customer customer) {
        var customers = customerRepository.findAll(Example.of(customer));

        return ResponseEntity.ok(customers);
    }

    // Método PUT
    // Método que nos permite crear un nuevo cliente

    @PutMapping("customers")
    public ResponseEntity<Customer> edit(@RequestBody Customer customer) {
        if(customer.getId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        customerRepository.save(customer); // obtiene un ID
        return ResponseEntity.ok(customer);
    }

    // Método que nos devuelve clientes con un salario incrementado en un 10%
    @GetMapping("customers-salary-modified") // localhost:8080/customers-salary-modified
    public ResponseEntity<List<Customer>> findAllWithSalaryModified() {
        var customers = customerRepository.findAll();

        customers.forEach(c -> c.setSalary(c.getSalary() * 1.10));

        //customers.forEach(c -> c.setSalary(c.getSalary() * IVA_21));

        return ResponseEntity.ok(customers);
    }

    // Método PATCH
    // Actualización parcial de un
    @PatchMapping(value = "customers/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<Customer> partialUpdate(
            @PathVariable Long id, @RequestBody Customer customer
    ) {
    // Validación inicial
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

// Buscar el cliente por ID y realizar la actualización parcial
        return customerRepository.findById(id)
                .map(existingCustomer -> {
                    if (customer.getName() != null) existingCustomer.setName(customer.getName());
                    if (customer.getEmail() != null) existingCustomer.setEmail(customer.getEmail());
                    if (customer.getPhone() != null) existingCustomer.setPhone(customer.getPhone());
                    if (customer.getAge() != null) existingCustomer.setAge(customer.getAge());
                    if (customer.getSalary() != null) existingCustomer.setSalary(customer.getSalary());
                    if (customer.getActive() != null) existingCustomer.setActive(customer.getActive());
                    customerRepository.save(existingCustomer);
                    return ResponseEntity.ok(existingCustomer);
                })
                // Si no se encuentra el cliente, se devuelve un 404
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    // Método DELETE
    // Método que nos permite eliminar un cliente

    @DeleteMapping("customers/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        try {
            customerRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }

    // Método para eliminar múltiples clientes cuyo ID esté en la lista

    @DeleteMapping("customers")
    public ResponseEntity<Void> deleteAll(@RequestBody List<Long> ids) {
        try {
            customerRepository.deleteAllByIdInBatch(ids);
            return ResponseEntity.noContent().build(); //204
        } catch (Exception e) {
            log.error("Error al eliminar un cliente", e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error al eliminar un cliente");
        }

    }
}
