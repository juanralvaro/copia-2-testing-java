package com.certidevs.controller;

import com.certidevs.model.Manufacturer;
import com.certidevs.model.Product;
import com.certidevs.repository.ManufacturerRepository;
import com.certidevs.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@AllArgsConstructor
@Controller
public class ProductController {

    private ProductRepository productRepository; // Spring crea el objeto productRepository y lo inyecta aquí
    private ManufacturerRepository manufacturerRepository;

    // http://localhost:8080/productos
    @GetMapping("productos")
    public String findAll(Model model) {
        model.addAttribute("titulo", "Lista de productos");
        model.addAttribute("productos", productRepository.findAll());
        return "product-list"; // vista
    }

    // http://localhost:8080/productos/1
    @GetMapping("productos/{id}")
    public String findById(@PathVariable Long id, Model model) {
        Optional<Product> productOptional = productRepository.findById(id);
        // Opción 1: sin lambda:
//        if (productOptional.isPresent()) {
//            Product product = productOptional.get();
//            model.addAttribute("producto", product);
//        }
        // Opción 2: usando una lambda:
        productOptional.ifPresent(product -> model.addAttribute("product", product));
        return "product-detail";
    }
    @GetMapping("productos2/{id}")
    public String findById2(@PathVariable Long id, Model model) {
        return productRepository.findById(id)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product-detail";
                })
//                .orElse("error")
//                .orElseGet(() -> {
//                    model.addAttribute("message", "Producto no encontrado");
//                    return "error";
//                });
//                .orElseThrow();
                .orElseThrow(() ->
                        new NoSuchElementException("Producto no encontrado"));
    }

    // http://localhost:8080/productos/crear  Obtener el formulario vacío para poder crear un producto desde cero
    @GetMapping("productos/crear")
    public String obtenerFormularioParaNuevoProducto(Model model) {
        model.addAttribute("product", new Product());

        // CALCULO DEMO FICTICIO PARA PROBAR EN TESTING
        List<Manufacturer> manufacturers = manufacturerRepository.findAll().stream()
                .map(m -> {
                    m.setName(m.getName().toUpperCase()); // CAMBIA EL NOMBRE DEL MANUFACTURER A MAYÚSCULAS
                    return m;
                }).toList();

        model.addAttribute("manufacturers",manufacturers);

        return "product-form";
    }

    // http://localhost:8080/productos/editar/1  Obtener el formulario relleno
    @GetMapping("productos/editar/{id}")
    public String obtenerFormularioParaEditarProducto(@PathVariable Long id, Model model) {
        productRepository.findById(id)
                .ifPresent(product -> model.addAttribute("product", product));

        model.addAttribute("manufacturers", manufacturerRepository.findAll());

        return "product-form";
    }

    // POST /products  Guardar el producto
    @PostMapping("productos")
    public String guardarProducto(@ModelAttribute Product product) {
        boolean exists = false;
        if (product.getId() != null) {
            exists = productRepository.existsById(product.getId());
        }
        if (! exists) {
            // Crear un nuevo producto
            productRepository.save(product);
        } else {
            // Actualizar un producto existente
            productRepository.findById(product.getId()).ifPresent(productoDB -> {
//                productoDB.setName(product.getName());
//                productoDB.setPrice(product.getPrice());
//                productoDB.setQuantity(product.getQuantity());
//                productoDB.setActive(product.getActive());
                BeanUtils.copyProperties(product, productoDB);
                productRepository.save(productoDB);
            });
        }

        return "redirect:/productos";
    }

    // METODO BORRAR
    // http://localhost:8080/productos/borrar/1
    // http://localhost:8080/productos/borrar/2
    @GetMapping("productos/borrar/{id}")
    public String borrarProducto(@PathVariable Long id) {
        try {
            productRepository.deleteById(id);
            return "redirect:/productos";
        } catch (Exception e) {
            e.printStackTrace(); // Utilizar log.error
            return "error";
        }
    }

}
