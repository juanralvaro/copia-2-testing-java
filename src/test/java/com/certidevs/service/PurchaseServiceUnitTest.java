package com.certidevs.service;

import com.certidevs.model.Product;
import com.certidevs.model.Purchase;
import com.certidevs.repository.ProductRepository;
import com.certidevs.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Anotación para habilitar Mockito en JUnit 5
class PurchaseServiceUnitTest {

    @Mock // Mock del repositorio de Purchase
    private PurchaseRepository purchaseRepository;

    @Mock // Mock del repositorio de Product
    private ProductRepository productRepository;

    @InjectMocks // Inyecta los mocks en el servicio PurchaseService
    private PurchaseService purchaseService;

    private Product product; // Producto de prueba
    private Purchase purchase; // Compra de prueba

    @BeforeEach // Método que se ejecuta antes de cada test
    void setUp() {
        // Inicializar un producto de prueba
        product = Product.builder()
                .id(1L)
                .name("Producto Test")
                .price(100.0)
                .quantity(20)
                .build();

        // Inicializar una compra de prueba
        purchase = Purchase.builder()
                .id(1L)
                .email("test@example.com")
                .product(product)
                .quantity(2)
                .totalPrice(200.0)
                .purchaseDate(LocalDateTime.now())
                .build();
    }

    // Métodos de prueba aquí

    @Test
    @DisplayName("Prueba del método getPurchaseById - Compra encontrada")
    void testGetPurchaseById_Found() {
        // Configurar el mock para devolver la compra cuando se busque por ID
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase)); // Mock de findById

        // Ejecutar el método
        Purchase result = purchaseService.getPurchaseById(1L); // Llama al método del servicio

        // Verificar el resultado
        assertNotNull(result, "La compra no debería ser nula.");
        assertEquals(1L, result.getId(), "El ID de la compra debería ser 1.");
        assertEquals("test@example.com", result.getEmail(), "El email debería ser 'test@example.com'.");

        // Verificar que el método findById haya sido llamado una vez
        verify(purchaseRepository, times(1)).findById(1L); // Verifica la interacción con el mock
    }

    @Test
    @DisplayName("Prueba del método getPurchaseById - Compra no encontrada")
    void testGetPurchaseById_NotFound() {
        // Configurar el mock para devolver vacío cuando se busque por ID
        when(purchaseRepository.findById(1L)).thenReturn(Optional.empty()); // Mock findById vacío

        // Ejecutar y verificar excepción
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            purchaseService.getPurchaseById(1L); // Llama al método que debería lanzar excepción
        }, "Se debería lanzar una excepción cuando la compra no es encontrada."); // Mensaje de fallo

        // Verificar el mensaje de la excepción
        assertEquals("Compra no encontrada.", exception.getMessage(), "El mensaje de la excepción debería ser 'Compra no encontrada.'.");

        // Verificar que el método findById haya sido llamado
        verify(purchaseRepository).findById(1L); // Verifica interacción con el mock
    }

    @Test
    @DisplayName("Prueba del método getAllPurchases")
    void testGetAllPurchases() {
        // Configurar el mock para devolver una lista de compras
        when(purchaseRepository.findAll()).thenReturn(Arrays.asList(purchase));

        // Ejecutar el método
        List<Purchase> result = purchaseService.getAllPurchases();

        // Verificar el resultado
        assertNotNull(result, "La lista de compras no debería ser nula.");
        assertEquals(1, result.size(), "Debería haber una compra en la lista.");
        assertEquals(purchase, result.get(0), "La compra en la lista debería ser igual a la compra de prueba.");

        // Verificar que el método findAll haya sido llamado
        verify(purchaseRepository).findAll(); // Verifica interacción con el mock
    }

    @Test
    @DisplayName("Prueba del método getPurchasesByEmail")
    void testGetPurchasesByEmail() {
        // Configurar el mock para devolver una lista de compras filtradas por email
        when(purchaseRepository.findByEmail("test@example.com")).thenReturn(Arrays.asList(purchase));

        // Ejecutar el método
        List<Purchase> result = purchaseService.getPurchasesByEmail("test@example.com");

        // Verificar el resultado
        assertNotNull(result, "La lista de compras no debería ser nula.");
        assertEquals(1, result.size(), "Debería haber una compra en la lista.");
        assertEquals(purchase, result.get(0), "La compra en la lista debería ser igual a la compra de prueba.");

        // Verificar que el método findByEmail haya sido llamado con el email correcto
        verify(purchaseRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Prueba del método makePurchase - Compra con descuento")
    void testMakePurchase_WithDiscount() {
        // Definir los parámetros de entrada
        String email = "test@example.com"; // Email del comprador
        Long productId = 1L; // ID del producto
        Integer quantity = 10; // Cantidad comprada (dispara el descuento)

        // Configurar el mock para devolver el producto cuando se busque por ID
        when(productRepository.findById(productId)).thenReturn(Optional.of(product)); // Mock findById

        // Configurar el mock para guardar la compra y devolver la misma compra
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mock save

        // Configurar el mock para guardar el producto actualizado
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mock save

        // Ejecutar el método
        Purchase result = purchaseService.makePurchase(email, productId, quantity);

        // Verificar el resultado
        assertNotNull(result, "La compra no debería ser nula.");
        assertEquals(email, result.getEmail(), "El email de la compra debería ser 'test@example.com'.");
        assertEquals(product, result.getProduct(), "El producto de la compra debería ser igual al producto de la prueba.");
        assertEquals(quantity, result.getQuantity(), "La cantidad de la compra debería ser 10.");
        assertEquals(900.0, result.getTotalPrice(), 0.001, "El precio total debería ser 900.0 (10 * 100 * 0.9).");
        assertNotNull(result.getPurchaseDate(), "La fecha de compra no debería ser nula.");

        // Verificar que el stock del producto se haya actualizado correctamente
        assertEquals(10, product.getQuantity(), "El stock del producto debería ser 10 (20 - 10).");

        // Verificar que los métodos del repositorio hayan sido llamados
        verify(productRepository).findById(productId); // Verifica que findById ha sido llamado
        verify(purchaseRepository).save(any(Purchase.class)); // Verifica que save de compra ha sido llamado
        verify(productRepository).save(any(Product.class)); // Verifica que save de producto ha sido llamado
    }

    @Test
    @DisplayName("Prueba del método makePurchase - Compra sin descuento")
    void testMakePurchase_WithoutDiscount() {
        // Definir los parámetros de entrada
        String email = "test@example.com";
        Long productId = 1L;
        Integer quantity = 5; // Cantidad comprada (no dispara el descuento)

        // Configurar el mock para devolver el producto cuando se busque por ID
        when(productRepository.findById(productId)).thenReturn(Optional.of(product)); // Mock findById

        // Configurar el mock para guardar la compra y devolver la misma compra
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mock save

        // Configurar el mock para guardar el producto actualizado
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mock save

        // Ejecutar el método
        Purchase result = purchaseService.makePurchase(email, productId, quantity);

        // Verificar el resultado
        assertNotNull(result, "La compra no debería ser nula.");
        assertEquals(email, result.getEmail(), "El email de la compra debería ser 'test@example.com'.");
        assertEquals(product, result.getProduct(), "El producto de la compra debería ser igual al producto de la prueba.");
        assertEquals(quantity, result.getQuantity(), "La cantidad de la compra debería ser igual a 5.");
        assertEquals(500.0, result.getTotalPrice(), 0.001, "El precio total debería ser 500.0 (5 * 100).");
        assertNotNull(result.getPurchaseDate(), "La fecha de compra no debería ser nula.");

        // Verificar que el stock del producto se haya actualizado correctamente
        assertEquals(15, product.getQuantity(), "El stock del producto debería ser 15 (20 - 5).");

        // Verificar que los métodos del repositorio hayan sido llamados
        verify(productRepository).findById(productId); // Verifica que findById ha sido llamado
        verify(purchaseRepository).save(any(Purchase.class)); // Verifica que save de compra ha sido llamado
        verify(productRepository).save(any(Product.class)); // Verifica que save de producto ha sido llamado
    }

    @Test
    @DisplayName("Prueba del método makePurchase - Stock insuficiente")
    void testMakePurchase_InsufficientStock() {
        // Definir los parámetros de entrada
        String email = "test@example.com";
        Long productId = 1L;
        Integer quantity = 25; // Cantidad que excede el stock actual de 20

        // Configurar el mock para devolver el producto cuando se busque por ID
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Ejecutar y verificar excepción
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            purchaseService.makePurchase(email, productId, quantity);
        }, "Se debería lanzar una excepción cuando el stock es insuficiente.");

        // Verificar el mensaje de la excepción
        assertEquals("Stock insuficiente para el producto seleccionado.", exception.getMessage(), "El mensaje de la excepción debería ser 'Se debería lanzar una excepción cuando el stock es insuficiente.'.");

        // Verificar que el método findById haya sido llamado pero que los métodos save no hayan sido llamados
        verify(productRepository).findById(productId); // Verifica que findById ha sido llamado
        verify(purchaseRepository, never()).save(any(Purchase.class)); // Verifica que save de compra nunca ha sido llamado
        verify(productRepository, never()).save(any(Product.class)); // Verifica que save de producto nunca ha sido llamado
    }

    @Test
    @DisplayName("Prueba del método cancelPurchase")
    void testCancelPurchase() {
        // Configurar el mock para devolver la compra cuando se busque por ID
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        // Ejecutar el método
        purchaseService.cancelPurchase(1L); // Llama al método del servicio

        // Verificar que el stock del producto se haya restaurado correctamente
        assertEquals(22, product.getQuantity(), "El stock del producto debería ser 22 (20 + 2).");

        // Verificar que la compra haya sido eliminada
        verify(purchaseRepository).delete(purchase); // Verifica que delete ha sido llamado
    }

    @Test
    @DisplayName("Prueba del método getPurchasesBetweenDates")
    void testGetPurchasesBetweenDates() {
        // Definir el rango de fechas para la prueba
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);

//        // Configurar el mock para devolver la lista de compras dentro del rango de fechas
//        when(purchaseRepository.findByPurchaseDateBetween(startDate, endDate)).thenReturn(Arrays.asList(purchase));

//        // Ejecutar el método
//        List<Purchase> result = purchaseService.getPurchasesBetweenDates(startDate, endDate); // Llama al método del servicio

//        // Verificar el resultado
//        assertNotNull(result, "La lista no debería ser nula.");
//        assertEquals(1, result.size(), "Debería haber una compra en la lista.");
//        assertEquals(purchase, result.get(0), "La compra en la lista debería ser igual a la compra de prueba.");

        // Verificar que el método findByPurchaseDateBetween haya sido llamado con las fechas correctas
//        verify(purchaseRepository).findByPurchaseDateBetween(startDate, endDate);
    }
}