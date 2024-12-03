package com.certidevs.selenium.product;

import com.certidevs.model.Product;
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
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
Test funcional/UI de Selenium del listado de productos product-list.html

Requiere la dependencia selenium

Al poner DEFINED_PORT el propio test inicia la aplicación de Spring Boot y ejecuta los tests con navegador
NO HACE FALTA INICIAR LA APLICACIÓN MANUALMENTE DESDE EL MAIN
 */
// arranca en el puerto de application.properties para poder testear
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ProductListTest {

    @Autowired
    private ProductRepository productRepository;

    WebDriver driver;

    @BeforeEach // Se ejecuta al comienzo de cada test
    void setUp(){
        productRepository.deleteAll();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // para que no se abra el navegador
        options.addArguments("--disable-gpu"); // Deshabilita la aceleración de hardware
        options.addArguments("--window-size=1920,1080"); // Tamaño de la ventana
        options.addArguments("--no-sandbox"); // Bypass OS security model, requerido en entornos sin GUI
        options.addArguments("--disable-dev-shm-usage"); // Deshabilita el uso de /dev/shm manejo de memoria compartida
        driver = new ChromeDriver(options);
        driver.get("http://localhost:8080/productos");
    }

    @AfterEach // Se ejecuta al final de cada test
    void tearDown(){
        driver.quit();
    }

    @Test
    @DisplayName("Comprobar etiqueta title")
    void title() {
        String title = driver.getTitle();
        System.out.println(title);
        assertEquals("Product List", title);
    }

    @Test
    @DisplayName("Comprobar la etiqueta <h1>")
    void h1(){
        WebElement h1 = driver.findElement(By.tagName("h1"));
        assertEquals("Lista de productos", h1.getText());
    }

    @Test
    @DisplayName("Comprobar que existe el botón de Crear nuevo producto y su texto")
    void buttonCreateProduct(){
        WebElement createButton = driver.findElement(By.id("btnCreateProduct"));
        assertEquals("Crear nuevo producto", createButton.getText());

        createButton.click();

        assertEquals("http://localhost:8080/productos/crear", driver.getCurrentUrl());
    }

    @Test
    @DisplayName("Comprobar tabla vacía con texto cuando no hay datos")
    void tableEmpty() {
        // Comprobar que existe párrafo de "No hay productos"
        WebElement noProductsMessage = driver.findElement(By.id("productsEmpty"));
        assertEquals("No hay productos.", noProductsMessage.getText());

        // Comprobar que no existe la tabla productos
        // WebElement productsTable = driver.findElement(By.id("productList"));
        assertThrows(
                NoSuchElementException.class,
                () -> driver.findElement(By.id("productList"))
        );
    }

    @Test
    @DisplayName("Comprobar tabla con productos")
    void tableWithProducts(){
        productRepository.saveAll(List.of(
                Product.builder().name("prod1").price(10d).active(true).quantity(1).build(),
                Product.builder().name("prod2").price(20d).active(false).quantity(2).build(),
                Product.builder().name("prod3").price(30d).active(true).quantity(3).build()
        ));

        //Al insertar nuevos productos debemos refrescar la pantalla para que los traiga
        driver.navigate().refresh(); // Simular F5

        WebElement productList = driver.findElement(By.id("productList"));
        assertTrue(productList.isDisplayed());
    }

    @Test
    @DisplayName("Comprobar las columnas de la tabla")
    void tableWithProducts_Columns() {
        productRepository.saveAll(List.of(
                Product.builder().name("prod1").price(10d).active(true).quantity(1).build(),
                Product.builder().name("prod2").price(20d).active(false).quantity(2).build(),
                Product.builder().name("prod3").price(30d).active(true).quantity(3).build()
        ));

        //Al insertar nuevos productos debemos refrescar la pantalla para que los traiga
        driver.navigate().refresh(); // Simular F5

        WebElement productList = driver.findElement(By.id("productList"));
        List<WebElement> headers = productList.findElements(By.tagName("th"));
        assertEquals(7, headers.size());
        assertEquals("ID", headers.get(0).getText());
        assertEquals("TÍTULO", headers.get(1).getText());
        assertEquals("PRECIO (€)", headers.get(2).getText());
        assertEquals("CANTIDAD", headers.get(3).getText());
        assertEquals("ACTIVO", headers.get(4).getText());
        assertEquals("FABRICANTE", headers.get(5).getText());
        assertEquals("ACCIONES", headers.get(6).getText());

    }

    @Test
    @DisplayName("Comprobar las filas de la tabla y sus datos - sin poner ids en los HTML en los <td>")
    void tableWithProducts_Rows(){
        productRepository.saveAll(List.of(
                Product.builder().name("prod1").price(10d).active(true).quantity(1).build(),
                Product.builder().name("prod2").price(20d).active(false).quantity(2).build(),
                Product.builder().name("prod3").price(30d).active(true).quantity(3).build()
        ));

        //Al insertar nuevos productos debemos refrescar la pantalla para que los traiga
        driver.navigate().refresh(); // Simular F5

        WebElement productList = driver.findElement(By.id("productList"));
        List<WebElement> rows = productList.findElements(By.tagName("tr"));
        assertEquals(4, rows.size());

        // Utilizando un selector más específico para obtener las filas del tbody
        List<WebElement> tableRows = driver.findElements(By.cssSelector("#productList tbody tr"));
        assertEquals(3, tableRows.size());

        WebElement firstRow = tableRows.getFirst();
        var firstRowDatas = firstRow.findElements(By.tagName("td"));

        assertEquals("prod1", firstRowDatas.get(1).getText());

    }

    @Test
    @DisplayName("Comprobar las filas de la tabla y sus datos - con ids dinámicos en los HTML")
    void tableWithProducts_rows_ids(){
        Product product = productRepository.save(
                Product.builder().name("prod1").price(10d).active(true).quantity(5).build());

        //Al insertar nuevos productos debemos refrescar la pantalla para que los traiga
        driver.navigate().refresh(); // Simular F5

        WebElement id = driver.findElement(By.id("productId_" + product.getId()));
        assertEquals(product.getId(), Long.valueOf(id.getText()));

        WebElement name = driver.findElement(By.id("productName_" + product.getId()));
        assertEquals("prod1", name.getText());

//        WebElement price = driver.findElement(By.id("productPrice_" + product.getId()));
//        assertEquals("10.0 €", price.getText());

        WebElement quantity = driver.findElement(By.id("productQuantity_" + product.getId()));
        assertEquals("5", quantity.getText());

        WebElement active = driver.findElement(By.id("productActiveTrue_" + product.getId()));
        assertEquals("Disponible", active.getText());

        assertThrows(
                NoSuchElementException.class,
                () -> driver.findElement(By.id("productActiveFalse_" + product.getId()))
        );

    }

    @Test
    void tableWithProducts_actionButtons_view(){
        Product product = productRepository.save(
                Product.builder().name("prod1").price(10d).active(true).quantity(5).build());

        //Al insertar nuevos productos debemos refrescar la pantalla para que los traiga
        driver.navigate().refresh(); // Simular F5

        var viewButton = driver.findElement(By.id("productActionView_" + product.getId()));

        assertEquals("Ver", viewButton.getText());

        viewButton.click();

        assertEquals("http://localhost:8080/productos/" + product.getId(), driver.getCurrentUrl());

    }

    @Test
    void tableWithProducts_actionButtons_edit(){
        Product product = productRepository.save(
                Product.builder().name("prod1").price(10d).active(true).quantity(5).build());

        //Al insertar nuevos productos debemos refrescar la pantalla para que los traiga
        driver.navigate().refresh(); // Simular F5

        var viewButton = driver.findElement(By.id("productActionEdit_" + product.getId()));

        assertEquals("Editar", viewButton.getText());

        viewButton.click();

        assertEquals("http://localhost:8080/productos/editar/" + product.getId(), driver.getCurrentUrl());

    }

    @Test
    void tableWithProducts_actionButtons_delete(){
        Product product = productRepository.save(
                Product.builder().name("prod1").price(10d).active(true).quantity(5).build());

        //Al insertar nuevos productos debemos refrescar la pantalla para que los traiga
        driver.navigate().refresh(); // Simular F5

        var viewButton = driver.findElement(By.id("productActionDelete_" + product.getId()));

        assertEquals("Borrar", viewButton.getText());

        viewButton.click();

        assertEquals("http://localhost:8080/productos", driver.getCurrentUrl());

        WebElement noProductsMessage = driver.findElement(By.id("productsEmpty"));
        assertEquals("No hay productos.", noProductsMessage.getText());

        // Comprobar que no existe la tabla productos
        // WebElement productsTable = driver.findElement(By.id("productList"));
        assertThrows(
                NoSuchElementException.class,
                () -> driver.findElement(By.id("productList"))
        );

    }



}
