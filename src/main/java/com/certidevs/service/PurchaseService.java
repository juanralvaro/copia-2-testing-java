package com.certidevs.service;

import com.certidevs.model.Product;
import com.certidevs.model.Purchase;
import com.certidevs.repository.ProductRepository;
import com.certidevs.repository.PurchaseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class PurchaseService {

    private PurchaseRepository purchaseRepository; // Repositorio para operaciones CRUD de Purchase
    private ProductRepository productRepository; // Repositorio para operaciones CRUD de Product

    // Constructor manual (sin el @RequiredArgsConstructor)
    /*public PurchaseService(PurchaseRepository purchaseRepository, ProductRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
    }*/

    public Purchase getPurchaseById(Long id) {
        return purchaseRepository.findById(id) // Busca la compra por ID
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada."));
    }

    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll(); // Devuelve todas las compras
    }

    public List<Purchase> getPurchasesByEmail(String email) {
        return purchaseRepository.findByEmail(email); // Devuelve compras filtradas por email
    }

    @Transactional // Asegura que todas las operaciones se completan exitosamente o ninguna de ellas se aplique
    public Purchase makePurchase(String email, Long productId, Integer quantity) {
        // Validar que la cantidad sea positiva
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        // Buscar el productor por ID
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        // Verificar si hay suficiente stock
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente para el producto seleccionado.");
        }

        // Calcular el precio total sin descuento
        Double totalPrice = product.getPrice() * quantity;

        // Aplicar el descuento del 10% si la cantidad es mayor o igual a 10
        if (quantity >= 10) {
            // totalPrice = totalPrice * 0.9;
            totalPrice *= 0.9;
        }

        // Crear la compra
        Purchase purchase = Purchase.builder()
                .email(email)
                .product(product)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .purchaseDate(LocalDateTime.now())
                .build();

        // Guardar la compra en base de datos
        Purchase savedPurchase = purchaseRepository.save(purchase); // Guarda y devuelve la compra guardada

        // Actualizar el stock del producto restando la cantidad comprada
        product.setQuantity(product.getQuantity() - quantity); // Actualiza el stock del producto
        productRepository.save(product); // Guarda los cambios en el producto

        return savedPurchase; // Devuelve la compra realizada
    }

    @Transactional
    public void cancelPurchase(Long purchaseId) {
        // Buscar la compra por ID
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada.")); // Lanza una excepción si no existe

        // Obtener el product asociado a la compra
        Product product = purchase.getProduct(); // Obtiene el producto de la compra

        // Restaurar el stock del producto sumando la cantidad comprada
        product.setQuantity(product.getQuantity() + purchase.getQuantity()); // Restaura el stock
        productRepository.save(product); // Guarda los cambios en el producto

        // Eliminar la compra de la base de datos
        purchaseRepository.delete(purchase); // Elimina la compra
    }

//    public List<Purchase> getPurchasesBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
//        return purchaseRepository.findByPurchaseDateBetween(startDate, endDate);
//    }
}
