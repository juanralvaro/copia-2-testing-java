package com.certidevs.controller.integration;

import com.certidevs.model.Customer;
import com.certidevs.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

/*
Testing integración completa de CustomerController API REST con json
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CustomerRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    // Asegurar que no haya datos antes de cada test
        customerRepository.deleteAll();
    }

    //Métodos GET
    @Test
    void findAll() throws Exception {

        customerRepository.saveAll(List.of(
                Customer.builder().name("Customer 1").email("customer1@gmail.com").salary(1000d).build(),
                Customer.builder().name("Customer 2").email("customer2@gmail.com").salary(1000d).build(),
                Customer.builder().name("Customer 3").email("customer3@gmail.com").salary(1000d).build()
        ));

        mockMvc.perform(get(
                        "/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Customer 1"))
                .andExpect(jsonPath("$[1].name").value("Customer 2"));
    }

    @Test
    void findById() throws Exception {

        var customer = Customer.builder().name("Customer 1").email("customer1@gmail.com").
                salary(1000d).build();
        customerRepository.save(customer); //obtiene un id de base de datos

        mockMvc.perform(get(
                        "/customers/" + customer.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Customer 1"))
                .andExpect(jsonPath("$.salary").value(1000d));
    }

    // Método GET donde no existe un cliente
    @Test
    void findById_NotFound() throws Exception {

        mockMvc.perform(get(
                        "/customers/{id}", 9999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); //404
    }

    // Métodos POST
    @Test
    void create() throws Exception {
        var customer = Customer.builder()
                .name("Customer 1")
                .email("customer1@gmail.com")
                .salary(1000d)
                .build();

        // Ejecutar una solicitud POST para enviar un nuevo cliente
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer))
                )
                .andExpect(status().isCreated()) // verificar que la respuesta sea 201, created
                // Verificamos que el campo "name" en la respuesta JSON tenga el valor de "Customer 1"
                .andExpect(jsonPath("$.name").value("Customer 1"));
    }

    @Test
    void create_badRequest() throws Exception {
        var customer = Customer.builder()
                .id(1L) // Especificar ID para simular el error
                .name("Customer 1")
                .email("customer1@gmail.com")
                .salary(1000d)
                .build();

        // Realizar la solicitud POST y esperar un error BadRequest
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer))
                )
                .andExpect(status().isBadRequest()); // verificar que la respuesta sea 400, BadRequest

    }

    // Métodos PUT
    @Test
    void update() throws Exception {
        // Simulación de creación de un cliente
        var customer = Customer.builder()
                .name("Customer 1")
                .email("customer1@gmail.com")
                .salary(1000d)
                .build();
        customerRepository.save(customer);

        // Simulación de modificación de un cliente
        customer.setName(customer.getName() + " Modificado"); //"Customer 1 Modificado"
        customer.setSalary(2000D);


        mockMvc.perform(put("/customers") // Ejecutar una solicitud PUT a la url "/customers"
                        .contentType(MediaType.APPLICATION_JSON) // Especificamos el tipo de contenido JSON
                        .content(objectMapper.writeValueAsString(customer)) // Convierte el cliente a JSON
                )
                .andExpect(status().isOk()) // verificar que la respuesta sea 201, created
                // Verificamos que el campo "name" en la respuesta JSON tenga el valor de "Customer 1"
                .andExpect(jsonPath("$.name").value("Customer 1 Modificado"));

        // Recuperar el cliente de la base de datos para confirmar los cambios
        var customerDB = customerRepository.findById(customer.getId()).orElseThrow();
        // Verificar que el nombre guardado es el de la base de datos
        assertEquals("Customer 1 Modificado", customerDB.getName());
    }

    @Test
    void update_BadRequest() throws Exception {
        // Simulación de creación de un cliente sin guardar en base de datos
        var customer = Customer.builder()
                .name("Customer 1")
                .email("customer1@gmail.com")
                .salary(1000d)
                .build();

        // Realizar la simulación de tipo PUT sin incluir un ID en el nombre del cliente
        mockMvc.perform(put("/customers") // Ejecutar una solicitud PUT a la url "/customers"
                        .contentType(MediaType.APPLICATION_JSON) // Especificamos el tipo de contenido JSON
                        .content(objectMapper.writeValueAsString(customer)) // Convierte el cliente a JSON
                ).andExpect(status().isBadRequest()); // verificar que la respuesta sea 201, created

    }


    //Método DELETE
    @Test
    void deleteBy_Id() throws Exception {
        // Simulación de creación de un cliente
        var customer = Customer.builder()
                .name("Customer 1")
                .email("customer1@gmail.com")
                .salary(1000d)
                .build();
        customerRepository.save(customer);

        // Ejecutar la solicitud DELETE para eliminar un cliente por su ID
        mockMvc.perform(delete("/customers/{id}", customer.getId())
                //Especificar el tipo de contenido JSON
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent()); // verificar que la respuesta sea 204, isNoContent

    }

    @Test
    @DisplayName("Buscar todos los clientes con salario incrementado en un 10%")
    void findAllWithSalaryModified() throws Exception {

        // Crear y guardar clientes en la base de datos
        customerRepository.saveAll(List.of(
                Customer.builder().name("Customer 1").email("customer1@gmail.com").salary(1000d).build(),
                Customer.builder().name("Customer 2").email("customer2@gmail.com").salary(2000d).build()
        ));

        // Simular una solicitud GET al endpoint /customers-salary-modified
        mockMvc.perform(get(
                        "/customers-salary-modified")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].salary").isNotEmpty())
                .andExpect(jsonPath("$[0].salary").value(1100d)) // primer cliente con salario incrementado un 10%
                .andExpect(jsonPath("$[1].salary").isNotEmpty())
                .andExpect(jsonPath("$[1].salary").value(2200d)); // segundo cliente con salario incrementado un 10%
    }

    @Test
    void findByFilter() throws Exception {
        // Crear y guardar clientes en la base de datos
        customerRepository.saveAll(List.of(
                Customer.builder().id(1L).name("Customer 1").email("customer1@gmail.com").salary(1000d).build(),
                Customer.builder().id(2L).name("Customer 2").email("customer2@gmail.com").salary(2000d).build(),
                Customer.builder().id(3L).name("Customer 3").email("customer2@gmail.com").salary(3000d).build()
        ));

        // Definir el filtro de clientes en JSON
        // Filtro de clientes con salario de 2000
        String filterJSON = """
                {
                    "salary": 2000.0
                }
                """;

        mockMvc.perform(post("/customers/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(filterJSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Customer 2"))
                .andExpect(jsonPath("$[0].salary").value(2000d));

    }

    @Test
    @DisplayName("Buscar clientes por filtro sin resultados")
    void findByFilter_NotFound() throws Exception {
        // Crear y guardar clientes en la base de datos
        customerRepository.saveAll(List.of(
                Customer.builder().id(1L).name("Customer 1").email("customer1@gmail.com").salary(1000d).build(),
                Customer.builder().id(3L).name("Customer 3").email("customer2@gmail.com").salary(3000d).build()
        ));

        // Definir el filtro de clientes en JSON
        // Filtro de clientes con salario de 2000
        String filterJSON = """
                {
                    "salary": 2000.0
                }
                """;

        mockMvc.perform(post("/customers/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filterJSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // se espera que no devuelva ningún cliente

    }


    @Test
    @DisplayName("Actualizar parcialmente un cliente")
    void partialUpdate_findById_OK() throws Exception{

        // Crear un cliente en base de datos
        Customer customerfromDB = Customer.builder()
                .name("Juan Pérez")
                .email("juan@gmail.com")
                .phone("123456")
                .salary(1000d)
                .age(30)
                .active(true)
                .build();
        customerRepository.save(customerfromDB);

        // Datos que se quieren actualizar
        String customerPatchJSON = """
                {
                    "name": "Juan Pérez Editado",
                    "salary": 1200.0,
                    "active": false
                }
                """;

        // Realizar la solicitud PATCH para actualizar parcialmente el cliente
        mockMvc.perform(patch("/customers/" + customerfromDB.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerPatchJSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Juan Pérez Editado"))
                .andExpect(jsonPath("$.salary").value(1200d))
                .andExpect(jsonPath("$.active").value(false));

        // Verificar que los datos se han guardado correctamente en la base de datos
        Customer updatedCustomer = customerRepository.findById(customerfromDB.getId())
                .orElseThrow(() -> new AssertionError("Cliente no encontrado en base de datos"));

        assertEquals("Juan Pérez Editado", updatedCustomer.getName());
        assertEquals(1200.0, updatedCustomer.getSalary());
        assertEquals(false, updatedCustomer.getActive());

    }

    @Test
    @DisplayName("Intentar actualizar parcialmente un cliente inexistente")
    void partialUpdate_findById_NotFound() throws Exception {
        // No creamos cliente en la base de datos
        // Definir los datos de actualización (aunque el cliente no exista)

        String customerPatchJSON = """
                {
                    "name": "Juan Pérez Editado",
                    "salary": 1200.0,
                    "active": false
                }
                """;

        // Realizar la solicitud PATCH para actualizar parcialmente el cliente
        mockMvc.perform(patch("/customers/{id}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerPatchJSON))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Eliminar clientes por lista de IDs")
    void deleteAllCustomersOk() throws Exception {

        List<Customer> customers = customerRepository.saveAll(List.of(
                Customer.builder().name("Customer 1").email("customer1@gmail.com").salary(1000d).build(),
                Customer.builder().name("Customer 2").email("customer2@gmail.com").salary(1000d).build(),
                Customer.builder().name("Customer 3").email("customer3@gmail.com").salary(1000d).build()
        ));

        // Obtener los IDs de los clientes guardados para eliminarlos
        List<Long> ids = customers.stream().map(Customer::getId).toList();

        // Convertir los IDs a JSON para enviarlos en la solicitud
        String idsJson = new ObjectMapper().writeValueAsString(ids);

        // Ejecutamos la solicitud DELETE para eliminar los clientes por lista de IDs
        mockMvc.perform(delete("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(idsJson))
                .andExpect(status().isNoContent()); // 204

        // Verificar que los clientes ya no existen en base de datos
        List<Customer> remainingCustomers = customerRepository.findAllById(ids);
        assertTrue(remainingCustomers.isEmpty());

    }

    @Test
    @DisplayName("Intentar eliminar con una solicitud vacía")
    void deleteAllCustomers_NoIds() throws Exception {
        mockMvc.perform(delete("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }
}