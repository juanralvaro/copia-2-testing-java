package com.certidevs.selenium.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/*
Test para comprobar la navegación de la Navbar y el Footer
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class NavTest {

    WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        driver.get("http://localhost:8080/productos");
        driver.manage().window().maximize(); // asegura el modo escritorio
    }

    @Test
    @DisplayName("Comprobar logo navbar con link a products")
    void checkLogoWithLink() {
        var logo = driver.findElement(By.cssSelector("a.navbar-brand > img"));
        assertTrue(logo.isDisplayed());

        driver.findElement(By.id("homeLink")).click();
        assertEquals("http://localhost:8080/productos", driver.getCurrentUrl());

    }

    @Test
    @DisplayName("Comprobar menú de fabricantes")
    void navigateToManufacturers() {
        driver.findElement(By.id("manufacturersLink")).click();
        assertEquals("http://localhost:8080/manufacturers", driver.getCurrentUrl());

    }

    @Test
    @DisplayName("Comprobar navbar colapsada en móvil con espera - NECESARIO BOOTSTRAP")
    void checkMobileNavbar() {
        driver.manage().window().setSize(new Dimension(390, 900));
        assertFalse(driver.findElement(By.id("manufacturersLink")).isDisplayed());

        // CUIDADO: esta acción puede tardar más de un segundo por lo que es conveniente
        // introducir una espera tras ella
        driver.findElement(By.cssSelector("button.navbar-toggler")).click();

        new WebDriverWait(driver, Duration.ofSeconds(3))
                .until(driver -> driver.findElement(By.id("manufacturersLink")).isDisplayed());

        assertTrue(driver.findElement(By.id("manufacturersLink")).isDisplayed());

        driver.findElement(By.id("manufacturersLink")).click();
        assertEquals("http://localhost:8080/manufacturers", driver.getCurrentUrl());
    }

}
