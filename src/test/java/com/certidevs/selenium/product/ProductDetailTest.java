package com.certidevs.selenium.product;

import com.certidevs.model.Manufacturer;
import com.certidevs.model.Product;
import com.certidevs.repository.ManufacturerRepository;
import com.certidevs.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
Test de Selenium para probar: product-detail.html
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ProductDetailTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    WebDriver driver;

    @BeforeEach
    void setUp(){
        productRepository.deleteAllInBatch();
        manufacturerRepository.deleteAllInBatch();
        driver = new ChromeDriver();
    }

    @AfterEach
    void tearDown(){
        driver.quit();
    }

    @Test
    @DisplayName("Comprobar producto OK con todos los datos correctos")
    void productExistsWithAllDetails(){

        Manufacturer manufacturer = manufacturerRepository.save(Manufacturer.builder()
                .name("Fabricante 1")
                .year(2024)
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Producto de prueba")
                .price((30.43))
                .quantity(3)
                .active(true)
                .manufacturer(manufacturer) // IMPORTANTE: AGREGAMOS ASOCIACIÓN
                .build());

        // navegar al product-detail
        driver.get("http://localhost:8080/productos/" + product.getId());

        WebElement h1 = driver.findElement(By.tagName("h1"));
        assertEquals("Detalle producto " + product.getId(), h1.getText());

        assertEquals("Producto de prueba", driver.findElement(By.id("productTitle")).getText());

        assertEquals(
                product.getId().toString(),
                driver.findElement(By.id("product-id")).getText()
        );

        assertEquals(
                product.getPrice().toString(),
                driver.findElement(By.id("product-price")).getText()
        );

        assertEquals(
                product.getQuantity().toString(),
                driver.findElement(By.id("product-quantity")).getText()
        );

        var active = driver.findElement(By.id("product-active"));
        assertEquals("Disponible", active.getText());
        assertEquals("rgba(0, 128, 0, 1)", active.getCssValue("color"));

        // Si hay fabricante
        // con id en html:
        // WebElement manufacturerLink = driver.findElement(By.id("manufacturerLink"));

        //
        WebElement manufacturerLink = driver.findElement(By.cssSelector("a[href*='/manufacturers/']"));
        assertEquals("http://localhost:8080/manufacturers/" + manufacturer.getId(),
                manufacturerLink.getAttribute("href")
        );

        assertEquals("Fabricante 1", manufacturerLink.getText());

    }

    @Test
    @DisplayName("Comprobar active false y manufacturer null")
    void checkFalseAndNullValues(){

        Product product = productRepository.save(Product.builder()
                .name("Producto de prueba")
                .price((30.43))
                .quantity(3)
                .active(false)
                .build());

        driver.get("http://localhost:8080/productos/" + product.getId());


        WebElement active = driver.findElement(By.id("product-not-active"));
        assertEquals("No disponible", active.getText());
        assertEquals("rgba(255, 0, 0, 1)", active.getCssValue("color"));

        WebElement manufacturerEmpty = driver.findElement(By.id("manufacturerEmpty"));
        assertEquals("Sin fabricante", manufacturerEmpty.getText());

    }

    @Test
    @DisplayName("Comprobar acción editar, borrar, volver")
    void actionButtons() {
        Product product = productRepository.save(Product.builder()
                .name("Producto de prueba")
                .price((30.43))
                .quantity(3)
                .active(false)
                .build());

        driver.get("http://localhost:8080/productos/" + product.getId());

        // edit button
        var editBtn = driver.findElement(By.id("editButton"));
        assertEquals("Editar", editBtn.getText());
        assertEquals(
                "http://localhost:8080/productos/editar/" + product.getId(),
                editBtn.getAttribute("href")
        );

        editBtn.click(); // navega a la pantalla de editar
        assertEquals(
                "http://localhost:8080/productos/editar/" + product.getId(),
                driver.getCurrentUrl()

        );
        driver.navigate().back(); // Volvemos a la pantalla detalle para seguir testeando

        // back button - Probar Volver antes de borrar, ya que si borramos, deja de existir

        var backBtn = driver.findElement(By.id("backButton"));
        assertEquals("Volver a la lista", backBtn.getText());
        assertEquals(
                "http://localhost:8080/productos",
                backBtn.getAttribute("href")
        );

        backBtn.click(); // navega a la pantalla de editar
        assertEquals(
                "http://localhost:8080/productos",
                driver.getCurrentUrl()

        );
        driver.navigate().back(); // Volvemos a la pantalla detalle para seguir testeando


        // delete button
        var deleteBtn = driver.findElement(By.id("deleteButton"));
        assertEquals("Borrar", deleteBtn.getText());
        assertEquals(
                "http://localhost:8080/productos/borrar/" + product.getId(),
                deleteBtn.getAttribute("href")
        );

        deleteBtn.click(); // navega a la pantalla de editar
        assertEquals(
                "http://localhost:8080/productos",
                driver.getCurrentUrl()

        );

    }
    @Test
    @DisplayName("Comprobar producto no existe")
    void productNotExist(){
        driver.get("http://localhost:8080/productos/999");

        assertEquals("Producto no encontrado", driver.findElement(By.tagName("h1")).getText());

        assertEquals("No existe el producto", driver.findElement(By.id("productEmpty")).getText());

        assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("editButton")));
        assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("deleteButton")));
    }


}
