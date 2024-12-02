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
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
Test de selenium para probar: product-form.html
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ProductFormTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ManufacturerRepository manufacturerRepository;

    WebDriver driver;

    @BeforeEach
    void setUp() {
        productRepository.deleteAllInBatch();
        manufacturerRepository.deleteAllInBatch();
        driver = new ChromeDriver();
    }
    @AfterEach
    void tearDown() {
        driver.quit();
    }
    @Test
    @DisplayName("Comprobar inputs vacíos si es CREACIÓN")
    void checkCreation_EmptyInputs() {
        manufacturerRepository.saveAll(List.of(
                Manufacturer.builder().name("fabricante 1").build(),
                Manufacturer.builder().name("fabricante 2").build()
        ));

        driver.get("http://localhost:8080/productos/crear");

        var h1 = driver.findElement(By.tagName("h1"));
        assertEquals("Crear producto", h1.getText());

        // comprobar inputs vacíos
        var inputName = driver.findElement(By.id("name"));
        assertTrue(inputName.getAttribute("value").isEmpty());

        var inputPrice = driver.findElement(By.id("price"));
        assertTrue(inputPrice.getAttribute("value").isEmpty());

        var inputQuantity = driver.findElement(By.id("quantity"));
        assertTrue(inputQuantity.getAttribute("value").isEmpty());

        var inputActive = driver.findElement(By.id("active"));
        assertEquals("true", inputActive.getAttribute("value"));

        // selector de manufacturer, convertimos de WebElement a Select
        Select manufacturerSelect = new Select(driver.findElement(By.id("manufacturer")));
        assertFalse(manufacturerSelect.isMultiple());
        assertEquals(3, manufacturerSelect.getOptions().size());
        assertEquals("", manufacturerSelect.getOptions().get(0).getText());
        assertEquals("FABRICANTE 1", manufacturerSelect.getOptions().get(1).getText());
        assertEquals("FABRICANTE 2", manufacturerSelect.getOptions().get(2).getText());
    }

    @Test
    @DisplayName("Comprobar que el formulario aparece relleno al editar un producto")
    void checkEdition_FilledInputs() {
        var manufacturers = manufacturerRepository.saveAll(List.of(
                Manufacturer.builder().name("fabricante 1").build(), // 0
                Manufacturer.builder().name("fabricante 2").build() // 1
        ));
        Manufacturer manufacturer2 = manufacturers.getLast();

        Product product = Product.builder()
                .name("prod1")
                .price(14.22)
                .quantity(4)
                .active(true)
                .manufacturer(manufacturer2) // fabricante 2
                .build();
        productRepository.save(product);

        driver.get("http://localhost:8080/productos/editar/" + product.getId());

        // comprobar inputs rellenos
        var inputName = driver.findElement(By.id("name"));
        assertEquals("prod1", inputName.getAttribute("value"));

        var inputPrice = driver.findElement(By.id("price"));
        assertEquals("14.22", inputPrice.getAttribute("value"));

        var inputQuantity = driver.findElement(By.id("quantity"));
        assertEquals("4", inputQuantity.getAttribute("value"));

        var inputActive = driver.findElement(By.id("active"));
        assertEquals("true", inputActive.getAttribute("value"));

        // selector de manufacturer, convertimos de WebElement a Select
        Select manufacturerSelect = new Select(driver.findElement(By.id("manufacturer")));
        assertFalse(manufacturerSelect.isMultiple());
        assertEquals(3, manufacturerSelect.getOptions().size());
        assertEquals(
                String.valueOf(manufacturer2.getId()),  // id del fabricante en string, por ejemplo: "2"
                manufacturerSelect.getFirstSelectedOption().getAttribute("value")
        );
        assertEquals(
                manufacturer2.getName(), // nombre del fabricante: "fabricante 2"
                manufacturerSelect.getFirstSelectedOption().getText()
        );
    }

    @Test
    @DisplayName("Entrar en el formulario y crear un nuevo producto y enviar")
    void crearNuevoProductoYEnviar() {
        manufacturerRepository.saveAll(List.of(
                Manufacturer.builder().name("fabricante 1").build(),
                Manufacturer.builder().name("fabricante 2").build()
        ));

        driver.get("http://localhost:8080/productos/crear");

        var inputName = driver.findElement(By.id("name"));
        inputName.sendKeys("Producto thymeleaf");
        var inputPrice = driver.findElement(By.id("price"));
        inputPrice.sendKeys("55");
        var inputQuantity = driver.findElement(By.id("quantity"));
        inputQuantity.sendKeys("15");
        var inputActive = driver.findElement(By.id("active"));
        inputActive.click();
        Select manufacturerSelect = new Select(driver.findElement(By.id("manufacturer")));
        manufacturerSelect.selectByVisibleText("FABRICANTE 2");

        driver.findElement(By.id("btnSend")).click();

        assertEquals("http://localhost:8080/productos", driver.getCurrentUrl());

        List<WebElement> tableRows = driver.findElements(By.cssSelector("#productList tbody tr"));
        assertEquals(1, tableRows.size()); // COMPROBAR QUE SE HA CREADO UN PRODUCTO

        var productSaved = productRepository.findAll().getFirst();
        assertEquals("Producto thymeleaf", productSaved.getName());
        assertEquals(55, productSaved.getPrice());
        assertEquals(15, productSaved.getQuantity());
        assertEquals(true, productSaved.getActive());
        assertEquals("fabricante 2", productSaved.getManufacturer().getName());
    }

    @Test
    @DisplayName("Entrar en el formulario y editar un producto existente y enviar")
    void editarProductYEnviar() {

        var manufacturers = manufacturerRepository.saveAll(List.of(
                Manufacturer.builder().name("fabricante 1").build(), // 0
                Manufacturer.builder().name("fabricante 2").build() // 1
        ));
        Manufacturer manufacturer2 = manufacturers.getLast();

        Product product = Product.builder()
                .name("prod1")
                .price(14.22)
                .quantity(4)
                .active(false)
                .manufacturer(manufacturer2) // fabricante 2
                .build();
        productRepository.save(product);

        driver.get("http://localhost:8080/productos/editar/" + product.getId());

        // modificar campos desde selenium
        var inputName = driver.findElement(By.id("name"));
        inputName.clear();
        inputName.sendKeys("prod 1 modificado");
        var inputPrice = driver.findElement(By.id("price"));
        inputPrice.clear();
        inputPrice.sendKeys("55,43");
        var inputQuantity = driver.findElement(By.id("quantity"));
        inputQuantity.clear();
        inputQuantity.sendKeys("15");
        var inputActive = driver.findElement(By.id("active"));
        inputActive.click();
        Select manufacturerSelect = new Select(driver.findElement(By.id("manufacturer")));
        manufacturerSelect.selectByVisibleText("fabricante 1");
        driver.findElement(By.id("btnSend")).click();

        // Obtener producto de base de datos y comprobar campos modificados
        assertEquals("http://localhost:8080/productos", driver.getCurrentUrl());


        var productSaved = productRepository.findAll().getFirst();
        assertEquals("prod 1 modificado", productSaved.getName());
        assertEquals(55.43, productSaved.getPrice());
        assertEquals(15, productSaved.getQuantity());
        assertEquals(true, productSaved.getActive());
        assertEquals("fabricante 1", productSaved.getManufacturer().getName());

    }

    @Test
    @DisplayName("Enviar valores erróneos y verificar validaciones del formulario HTML")
    void create_InvalidValues() {
        driver.get("http://localhost:8080/productos/crear");

        var inputName = driver.findElement(By.id("name"));
        inputName.sendKeys("Producto thymeleaf");

        var inputPrice = driver.findElement(By.id("price"));
        inputPrice.sendKeys("600");

        var inputQuantity = driver.findElement(By.id("quantity"));
        inputQuantity.sendKeys("30");

        driver.findElement(By.id("btnSend")).click();

        assertEquals("http://localhost:8080/productos/crear", driver.getCurrentUrl());
        assertEquals(0, productRepository.count());

    }

    @Test
    @DisplayName("Comprobar id read only no deja editarlo")
    void checkIdReadOnly() {
        Product product = Product.builder()
                .name("prod1")
                .price(14.22)
                .quantity(4)
                .active(false)
                .build();
        productRepository.save(product);
        driver.get("http://localhost:8080/productos/editar/" + product.getId());

        var inputId = driver.findElement(By.id("id"));
        assertEquals(String.valueOf(product.getId()), inputId.getAttribute("value"));

        inputId.sendKeys("3");
        assertEquals(String.valueOf(product.getId()), inputId.getAttribute("value"));

        assertThrows(InvalidElementStateException.class, () -> inputId.clear());
        assertEquals(String.valueOf(product.getId()), inputId.getAttribute("value"));
    }

    /*
    Si el formulario tiene validaciones de JavaScript que muestran errores en mensajes HTML personalizados
    se pueden probar con Selenium
     */
}
