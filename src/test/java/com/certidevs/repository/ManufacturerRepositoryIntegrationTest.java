package com.certidevs.repository;

import com.certidevs.dto.ManufacturerWithAddressDTO;
import com.certidevs.dto.ManufacturerWithProductDataDTO;
import com.certidevs.model.Address;
import com.certidevs.model.Manufacturer;
import com.certidevs.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ManufacturerRepositoryIntegrationTest {

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Prueba del método findByYear")
    void findByYear() {
        // 1. Preparar los datos de prueba: crear fabricantes con diferentes años
        Manufacturer manufacturer1 = Manufacturer.builder()
                .name("Fabricante A")
                .year(2020)
                .build();

        Manufacturer manufacturer2 = Manufacturer.builder()
                .name("Fabricante B")
                .year(2021)
                .build();

        Manufacturer manufacturer3 = Manufacturer.builder()
                .name("Fabricante C")
                .year(2022)
                .build();

        //2. Guardar todos los fabricantes
        manufacturerRepository.saveAll(List.of(manufacturer1, manufacturer2, manufacturer3));

        // 3. Ejecutar el método a probar
        List<Manufacturer> manufacturers2020 = manufacturerRepository.findByYear(2020);

        // 4. Verificar el resultado
        assertEquals(2, manufacturers2020.size(), "Debería haber 2 fabricantes del año 2020");
        assertTrue(manufacturers2020.contains(manufacturer1));
        assertTrue(manufacturers2020.contains(manufacturer3));

    }

    @Test
    @DisplayName("Prueba del método findByNameIgnoreCase")
    void findByNameIgnoreCase() {
        // 1. Preparar los datos de prueba: crear fabricantes con nombres en diferentes casos
        Manufacturer manufacturer1 = Manufacturer.builder()
                .name("Fabricante X")
                .build();

        Manufacturer manufacturer2 = Manufacturer.builder()
                .name("fabricante x")
                .build();

        Manufacturer manufacturer3 = Manufacturer.builder()
                .name("Fabricante y")
                .build();

        // 2. Guardar los fabricantes en la base de datos
        manufacturerRepository.saveAll(List.of(manufacturer1, manufacturer2, manufacturer3));
        
        // 3. Ejecutar el método a probar con nombres en mayúsculas
        List<Manufacturer> manufacturers = manufacturerRepository.findByNameIgnoreCase("FABRICANTE X");

        // 4. Verificar el resultado
        assertEquals(2, manufacturers.size(), "Debería haber 2 fabricantes con nombre 'Fabricante X'");
        assertTrue(manufacturers.contains(manufacturer1));
        assertTrue(manufacturers.contains(manufacturer2));

    }

    @Test
    @DisplayName("Prueba del método existsByName")
    void existsByName() {
        // 1. Preparar los datos de prueba: crear fabricantes con nombres en diferentes casos
        Manufacturer manufacturer = Manufacturer.builder()
                .name("Existente")
                .build();

        // 2. Guardarlo en la base de datos
        manufacturerRepository.save(manufacturer);

        // 3. Verificar que el fabricante existe
        boolean exists = manufacturerRepository.existsByName("Existente");
        assertTrue(exists, "El fabricante 'Existente' debería existir en la base de datos");

        // 4. Verificar que un fabricante no existente nos devuelva false
        boolean notExists = manufacturerRepository.existsByName("NoExistente");
        assertFalse(notExists, "El fabricante 'NoExistente' no debería existir en la base de datos");
    }

    @Test
    @DisplayName("Prueba del método findByName con Query")
    void findByName() {
        // 1. Preparar los datos de prueba: crear fabricantes con nombres en diferentes casos
        Manufacturer manufacturer1 = Manufacturer.builder()
                .name("Fabricante Z")
                .build();

        Manufacturer manufacturer2 = Manufacturer.builder()
                .name("Fabricante Z")
                .build();

        Manufacturer manufacturer3 = Manufacturer.builder()
                .name("Fabricante W")
                .build();

        // 2. Guardar los fabricantes en la base de datos
        manufacturerRepository.saveAll(List.of(manufacturer1, manufacturer2, manufacturer3));

        // 3. Ejecutar la consulta a probar
        List<Manufacturer> manufacturers = manufacturerRepository.findByName("Fabricante Z");

        // 4. Verificar el resultado
        assertEquals(2, manufacturers.size(), "Debería haber 2 fabricantes con nombre 'Fabricante Z'");
        assertTrue(manufacturers.contains(manufacturer1));
        assertTrue(manufacturers.contains(manufacturer2));

    }

    @Test
    @DisplayName("Prueba de findAllWithCalculatedProductsStats")
    void findAllWithCalculatedProductsStats() {
        // 1. Configuración de datos de prueba

        // Crear un fabricante
        Manufacturer manufacturer = Manufacturer.builder()
                .name("Manufacturer A")
                .description("Descripción del fabricante A")
                .year(2024)
                .imageUrl("http://example.com/manufacturer-a.png")
                .build();

        manufacturerRepository.save(manufacturer); // Guardar el fabricante en la base de datos

        // Crear tres productos asociados al fabricante creado
        Product product1 = Product.builder()
                .name("Producto 1")
                .price(50.0)
                .quantity(10)
                .active(true)
                .manufacturer(manufacturer)
                .build();

        Product product2 = Product.builder()
                .name("Producto 2")
                .price(50.0)
                .quantity(5)
                .active(true)
                .manufacturer(manufacturer)
                .build();

        Product product3 = Product.builder()
                .name("Producto 3")
                .price(50.0)
                .quantity(2)
                .active(false)
                .manufacturer(manufacturer)
                .build();

        // Guardar los productos en la base de datos

        productRepository.saveAll(List.of(product1, product2, product3));

        // 2. Ejecutar la consulta JPQL
        List<ManufacturerWithProductDataDTO> manufacturers =
                manufacturerRepository.findAllWithCalculatedProductsStats();

        // 3. Verificación de los resultados

        // Verificar que la lista de resultados no esté vacía
        assertNotNull(manufacturers, "La lista de fabricantes no debería ser nula");

        // Verificar que se haya devuelto exactamente un fabricante
        assertEquals(1, manufacturers.size(), "Debería haber exactamente un fabricante en los resultados");

        // Obtener el primer y único elemento de la lista
        ManufacturerWithProductDataDTO dto = manufacturers.get(0);

        // Verificar que el ID del fabricante coincide con el esperado
        assertEquals(manufacturer.getId(), dto.manufacturerId(),
                "El ID del fabricante debería coincidir con el ID creado");

        // Verificar que el nombre del fabricante coincide con el esperado
        assertEquals(manufacturer.getName(), dto.manufacturerName());

        // Verificar que la cantidad de productos es 3
        assertEquals(3, dto.productsCount(),
                "La cantidad de productos asociados al fabricante debería ser 3");

        // Verificar que la suma total de precios de productos es 150
        assertEquals(150.0, dto.productsSumTotalPrice(),
                0.001, "La suma total de precios de los productos debería de ser 150.0");
                //delta: tolerancia de error

        // Impresión de resultados (Opcional)
        System.out.println("Resultado de la consulta JPQL:");
        System.out.println("ID del fabricante: " + dto.manufacturerId());
        System.out.println("Nombre del fabricante: " + dto.manufacturerName());
        System.out.println("Cantidad de productos: " + dto.productsCount());
        System.out.println("Suma total de precios de productos: " + dto.productsSumTotalPrice());

    }

    @Test
    @DisplayName("Prueba del método findByAddress_Id")
    void findByAddress_Id() {
        // 1. Preparar los datos de prueba: crear Address y Manufacturer asociados
        Address address = Address.builder()
                .street("Calle Alfonso")
                .city("Zaragoza")
                .state("Aragón")
                .zipcode("50001")
                .build();

        Manufacturer manufacturer = Manufacturer.builder()
                .name("Bodega El Pilar")
                .address(address)
                .build();

        // Asociar el Manufacturer en el Address (para relación bidireccional)
        address.setManufacturer(manufacturer);

        // 2. Guardar el manufacturer (cascadeará y guardará el Address)
        manufacturerRepository.save(manufacturer);

        // 3. Ejecutar el método a probar
        Manufacturer foundManufacturer = manufacturerRepository.findByAddress_Id(address.getId());

        // 4. Verificar el resultado
        assertNotNull(foundManufacturer, "El manufacturer no debería ser nulo");
        assertEquals(manufacturer.getId(), foundManufacturer.getId(), "Los IDs deberían coincidir");
        assertEquals(address.getId(), foundManufacturer.getAddress().getId(), "Los IDs de Address deberían coincidir");
    }

    @Test
    @DisplayName("Prueba del método countByAddress_Zipcode")
    void countByAddress_Zipcode() {
        // 1. Preparar datos de prueba: crear Manufacturers con diferentes códigos postales
        Address address1 = Address.builder()
                .zipcode("28012")
                .state("Comunidad de Madrid")
                .street("Gran Vía")
                .city("Madrid")
                .build();

        Address address2 = Address.builder()
                .zipcode("28012")
                .state("Comunidad de Madrid")
                .street("Puerta del Sol")
                .city("Madrid")
                .build();

        Address address3 = Address.builder()
                .zipcode("50001")
                .state("Aragón")
                .street("Paseo de la Independencia")
                .city("Zaragoza")
                .build();

        Manufacturer manufacturer1 = Manufacturer.builder()
                .name("Electrodomésticos Madrid")
                .address(address1)
                .build();

        Manufacturer manufacturer2 = Manufacturer.builder()
                .name("Libros Madrid")
                .address(address2)
                .build();

        Manufacturer manufacturer3 = Manufacturer.builder()
                .name("Ropa Zaragoza")
                .address(address3)
                .build();

        // Asociar los Manufacturers en las Addresses (relación bidireccional)
        address1.setManufacturer(manufacturer1);
        address2.setManufacturer(manufacturer2);
        address3.setManufacturer(manufacturer3);

        // 2. Guardar los Manufacturers
        manufacturerRepository.saveAll(List.of(manufacturer1, manufacturer2, manufacturer3));

        // 3. Ejecutar el método a probar
        long countZipcode28012 = manufacturerRepository.countByAddress_Zipcode("28012");
        long countZipcode50001 = manufacturerRepository.countByAddress_Zipcode("50001");

        // 4. Verificar el resultado
        assertEquals(2, countZipcode28012, "Debería haber 2 Manufacturers con el código postal 28012");
        assertEquals(1, countZipcode50001, "Debería haber 1 Manufacturer con el código postal 50001");

    }

    @Test
    @DisplayName("Prueba de la consulta findManufacturersInCityWithProductStats")
    void findManufacturersInCityWithProductStats() {
// Crear fabricantes con direcciones y productos
        Address address1 = Address.builder()
                .street("Calle Alfonso")
                .city("Zaragoza")
                .state("Aragón")
                .zipcode("50001")
                .build();

        Address address2 = Address.builder()
                .street("Calle Amantes")
                .city("Teruel")
                .state("Aragón")
                .zipcode("44001")
                .build();

        Manufacturer manufacturer1 = Manufacturer.builder()
                .name("Muebles Zaragoza")
                .address(address1)
                .build();

        Manufacturer manufacturer2 = Manufacturer.builder()
                .name("Cerámica Teruel")
                .address(address2)
                .build();

        manufacturerRepository.saveAll(List.of(manufacturer1, manufacturer2));

// Crear productos para los fabricantes
        Product product1 = Product.builder()
                .name("Mesa de comedor")
                .price(150.0)
                .manufacturer(manufacturer1)
                .build();

        Product product2 = Product.builder()
                .name("Silla de madera")
                .price(75.0)
                .manufacturer(manufacturer1)
                .build();

        Product product3 = Product.builder()
                .name("Jarrón de cerámica")
                .price(60.0)
                .manufacturer(manufacturer2)
                .build();

        productRepository.saveAll(List.of(product1, product2, product3));

// Ejecutar la consulta para Zaragoza
        List<ManufacturerWithAddressDTO> resultZaragoza = manufacturerRepository.findManufacturersInCityWithProductStats("Zaragoza");

// Verificar los resultados para Zaragoza
        assertEquals(1, resultZaragoza.size(), "Debería haber 1 Manufacturer en Zaragoza");

        ManufacturerWithAddressDTO dtoZaragoza = resultZaragoza.get(0);
        assertEquals(manufacturer1.getId(), dtoZaragoza.manufacturerId(), "Deberían coincidir los IDs");
        assertEquals("Muebles Zaragoza", dtoZaragoza.manufacturerName(), "Deberían coincidir los nombres");
        assertEquals("Zaragoza", dtoZaragoza.city(), "Debería coincidir la ciudad");
        assertEquals(2L, dtoZaragoza.productsCount(), "La cuenta de productos debería ser 2");
        assertEquals(225.0, dtoZaragoza.totalProductPrice(), "La suma total de precios de productos debería ser 225.0");

    }

}