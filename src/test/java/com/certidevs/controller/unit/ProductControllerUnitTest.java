package com.certidevs.controller.unit;

import com.certidevs.controller.ProductController;
import com.certidevs.model.Manufacturer;
import com.certidevs.model.Product;
import com.certidevs.repository.ManufacturerRepository;
import com.certidevs.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

/**
 *        doCallRealMethod().when(model).addAttribute(any(), any());
 *         doCallRealMethod().when(model).containsAttribute(any());
 *
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerUnitTest {

    // Clase a testear
    @InjectMocks
    private ProductController productController;
    // dependencias a Mock-ear
    @Mock
    private ProductRepository productRepository; // Spring crea el objeto productRepository y lo inyecta aquí
    @Mock
    private ManufacturerRepository manufacturerRepository;
    @Mock
    private Model model;

    @Test
    void findAll() {
        // 1. configurar mocks
        when(productRepository.findAll()).thenReturn(List.of(
                Product.builder().id(1L).build(),
                Product.builder().id(2L).build(),
                Product.builder().id(3L).build()
        ));

        // 2. invocar metodo a testear
        String view = productController.findAll(model);

        // 3. verificaciones
        verify(productRepository).findAll();
        assertEquals("product-list", view);
    }

    /*
        @GetMapping("productos/{id}")
    public String findById(@PathVariable Long id, Model model) {
        Optional<Product> productOptional = productRepository.findById(id);
        productOptional.ifPresent(product -> model.addAttribute("producto", product));
        return "product-detail";
    }
     */
    @Test
    @DisplayName("Caso en el que el producto sí existe")
    void findById_ProductExists() {
        Product producto = Product.builder().id(1L).name("Producto 1").build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(producto));

        String view = productController.findById(1L, model);

        assertEquals("product-detail", view);
        verify(productRepository).findById(1L);
        verify(productRepository, never()).findAll();
        verify(model).addAttribute("product", producto);
    }

    @Test
    @DisplayName("Caso en el que el producto NO existe")
    void findById_ProductNotExists() {

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        String view = productController.findById(1L, model);

        assertEquals("product-detail", view);
        verify(productRepository).findById(1L);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    void findById2_notExist() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> productController.findById2(1L, model));

        // COMPROBACIONES SI NO SE LANZA EXCEPCIÓN :
//        String view = productController.findById2(1L, model);
//
//        assertEquals("error", view);
//        verify(productRepository).findById(1L);
//        verify(model, never()).addAttribute(eq("product"), any());
//        verify(model).addAttribute("message", "Producto no encontrado");

    }

    @Test
    void obtenerFormularioParaNuevoProducto() {

        List<Manufacturer> manufacturers = List.of(
                Manufacturer.builder().name("Fabricante 1").build(),
                Manufacturer.builder().name("Fabricante 2").build()
        );
        when(manufacturerRepository.findAll()).thenReturn(manufacturers);

        String view = productController
                .obtenerFormularioParaNuevoProducto(model);


        assertEquals("product-form", view);
        verify(model).addAttribute(eq("product"), any(Product.class));
        verify(model).addAttribute("manufacturers", manufacturers);

    }

    @Test
    void obtenerFormularioParaEditarProducto_Exists() {

        Product producto = Product.builder().id(1L).name("Producto 1").build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(producto));

        String view = productController.obtenerFormularioParaEditarProducto(1L, model);

        assertEquals("product-form", view);
        verify(productRepository).findById(1L);
        verify(model).addAttribute("product", producto);
    }

    /**
     * Metodo {@link ProductController#obtenerFormularioParaEditarProducto(Long, Model)}
     */
    @Test
    void obtenerFormularioParaEditarProducto_NotExists() {

        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        List<Manufacturer> manufacturers = List.of(
                new Manufacturer(),
                new Manufacturer()
        );
        when(manufacturerRepository.findAll()).thenReturn(manufacturers);

        String view = productController.obtenerFormularioParaEditarProducto(1L, model);

        assertEquals("product-form", view);
        verify(model, never()).addAttribute(eq("product"), any());
        verify(model).addAttribute("manufacturers", manufacturers);
    }

    @Test
    void guardarProducto_CrearProductoNuevo() {

        Product product = new Product();

        String view = productController.guardarProducto(product);

        assertEquals("redirect:/productos", view);
        verify(productRepository).save(product);

    }

    @Test
    void guardarProducto_EditarProductoExistente() {

        Product product = Product.builder()
                .id(1L).name("Xiaomi 13").build();
    }
}