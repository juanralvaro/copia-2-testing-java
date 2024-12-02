package com.certidevs.controller.integration;

import com.certidevs.model.Manufacturer;
import com.certidevs.model.Product;
import com.certidevs.repository.ManufacturerRepository;
import com.certidevs.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
Testing integración completa ProductController MVC con modelo y vista
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // cada test se ejecuta en una transacción que se revierte al acabar para dejar la db limpia
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // repos reales por lo que carga la base de datos completa, H2 en memoria configurado en application.properties de test/resources
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        manufacturerRepository.deleteAll();
    }

    @Test
    void findAll() throws Exception {

        productRepository.saveAll(List.of(
                Product.builder().name("Microfono").price(30d).build(),
                Product.builder().name("Mesa").price(15d).build()
        ));
        System.out.println("findAll products: " + productRepository.count());
        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-list"))
                .andExpect(model().attributeExists("productos"))
                .andExpect(model().attribute("productos", hasSize(2)));
    }

    @Test
    void findById() throws Exception {
        var product = productRepository.save(Product.builder().name("Microfono").price(30d).build());

        System.out.println("findById products: " + productRepository.count());
        System.out.println("findById producto guardado: " + product.getId());

        mockMvc.perform(get("/productos/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("product-detail"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    void findById2_NotExist() throws Exception {

        mockMvc.perform(get("/productos2/99999"))
                // .andExpect(status().isBadRequest()) // error concreto: 400
                .andExpect(status().is4xxClientError()) // error genérico de tipo 4xx: 400, 401, 402, 403, 404....
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attributeDoesNotExist("product"));

    }

    @Test
    void obtenerFormularioParaNuevoProducto() throws Exception {

        // crear manufacturers demo
        manufacturerRepository.saveAll(List.of(
                Manufacturer.builder().name("Fabricante 1").build(),
                Manufacturer.builder().name("Fabricante 2").build()
        ));

        // lanzar petición http y comprobar producto vacío y manufacturers
        mockMvc.perform(get("/productos/crear"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("product", "manufacturers"))
                //.andExpect(model().attribute("product", hasProperty("id", nullValue())))
                //.andExpect(model().attribute("product", hasProperty("name", nullValue())))
                .andExpect(model().attribute("product", allOf(
                        hasProperty("id", nullValue()),
                        hasProperty("name", nullValue())
                )))
                .andExpect(model().attribute("manufacturers", allOf(
                        hasSize(2),
                        containsInAnyOrder(
                                hasProperty("name", is("FABRICANTE 2")),
                                hasProperty("name", is("FABRICANTE 1"))
                        )

                )))
                .andExpect(view().name("product-form"));

    }

    @Test
    void obtenerFormularioParaEditarProducto() throws Exception {

        // crear datos demo: product y manufacturers
        Product product = Product.builder()
                .name("Ratón Logitech").build();
        productRepository.save(product);

        manufacturerRepository.saveAll(List.of(
                Manufacturer.builder().name("Fabricante 1").build(),
                Manufacturer.builder().name("Fabricante 2").build(),
                Manufacturer.builder().name("Fabricante 3").build()
        ));

        mockMvc.perform(get("/productos/editar/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("product-form"))
                .andExpect(model().attributeExists("product", "manufacturers"))
                .andExpect(model().attribute("product", allOf(
                        hasProperty("id", is(product.getId())),
                        hasProperty("name", is(product.getName()))
                )))
                .andExpect(model().attribute("manufacturers", allOf(
                        hasSize(3),
                        containsInAnyOrder(
                                hasProperty("name", is("Fabricante 1")),
                                hasProperty("name", is("Fabricante 2")),
                                hasProperty("name", is("Fabricante 3"))
                        )
                )));


    }

    @Test
    @DisplayName("Editar un producto existente")
    void guardarProducto_exists() throws Exception {
        Product product = Product.builder().name("Ratón Logitech").build();
        productRepository.save(product);

        Manufacturer manufacturer = Manufacturer.builder().name("ElGato").build();
        manufacturerRepository.save(manufacturer);

        // Simular que editamos un producto existente
        // Simular que enviamos datos desde un formulario por POST
        mockMvc.perform(
                        post("/productos")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                // Estos son datos que se envían al controlador
                                // Y el controlador los lee y crea un objeto Product con ellos
                                // Para ello usa una librería que se llama Jackson
                                .param("id", String.valueOf(product.getId()))
                                .param("name", "Ratón Logitech Modificado")
                                .param("price", "34.32")
                                .param("quantity", "5")
                                // Enviar el id del manufacturer para asociar el manufacturer al producto
                                .param("manufacturer", String.valueOf(manufacturer.getId()))

                ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        // Como no tenemos mocks no podemos hacer verify
        // Pero sí tenemos el repositorio real y podemos hacer consultas y asserts
        Optional<Product> savedProductOptional = productRepository.findById(product.getId());
        assertTrue(savedProductOptional.isPresent());

        Product savedProduct = savedProductOptional.get();

        assertEquals(product.getId(), savedProduct.getId());
        assertEquals("Ratón Logitech Modificado", savedProduct.getName());
        assertEquals(34.32, savedProduct.getPrice());
        assertEquals(manufacturer.getId(), savedProduct.getManufacturer().getId());
        assertEquals(manufacturer.getName(), savedProduct.getManufacturer().getName());
    }

    @Test
    void guardarProducto_notExists() throws Exception {

        Manufacturer manufacturer = Manufacturer.builder().name("ElGato").build();
        manufacturerRepository.save(manufacturer);

        // Simular que enviamos datos desde un formulario por POST
        mockMvc.perform(
                        post("/productos")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                // Estos son datos que se envían al controlador
                                // Y el controlador los lee y crea un objeto Product con ellos
                                // Para ello usa una librería que se llama Jackson
                                .param("name", "Nuevo producto")
                                .param("price", "34.32")
                                .param("quantity", "5")
                                // Enviar el id del manufacturer para asociar el manufacturer al producto
                                .param("manufacturer", String.valueOf(manufacturer.getId()))

                ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        List<Product> products = productRepository.findAll();
        assertEquals(1, products.size());

        assertEquals("Nuevo producto", products.get(0).getName());
    }

    @Test
    void borrarProducto() {
    }
}