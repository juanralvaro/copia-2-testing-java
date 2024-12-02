package com.certidevs.selenium.demoqa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/*
Prueba una aplicación web existente: https://www.demoqa.com/webtables

No necesita ejecutar la aplicación actual de Spring Boot
 */

public class WebTablesTest {


    WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        driver.get("https://demoqa.com/webtables");
        driver.manage().window().maximize();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    @DisplayName("Comprobar funcionamiento de la tabla")
    void viewTable(){

        driver.findElement(By.id("addNewRecordButton")).click();
        WebElement userForm = driver.findElement(By.id("userForm"));
        assertTrue(userForm.isDisplayed());


        driver.findElement(By.id("firstName")).sendKeys("Selenium");
        driver.findElement(By.id("lastName")).sendKeys("Selenium");
        driver.findElement(By.id("userEmail")).sendKeys("Selenium@test.com");
        driver.findElement(By.id("age")).sendKeys("30");
        driver.findElement(By.id("salary")).sendKeys("20000");
        driver.findElement(By.id("department")).sendKeys("Testing");
        driver.findElement(By.id("submit")).click();

        new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.invisibilityOf(userForm));

        assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("userForm")));

    }

    @Test
    @DisplayName("Comprobar a crear un elemento en la tabla")
    void viewTableModalToCreate(){

        driver.findElement(By.id("addNewRecordButton")).click();
        WebElement userForm = driver.findElement(By.id("userForm"));
        assertTrue(userForm.isDisplayed());


        driver.findElement(By.id("firstName")).sendKeys("Selenium FirstName");
        driver.findElement(By.id("lastName")).sendKeys("Selenium");
        driver.findElement(By.id("userEmail")).sendKeys("Selenium@test.com");
        driver.findElement(By.id("age")).sendKeys("30");
        driver.findElement(By.id("salary")).sendKeys("20000");
        driver.findElement(By.id("department")).sendKeys("Testing");
        driver.findElement(By.id("submit")).click();

        new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.invisibilityOf(userForm));

        assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("userForm")));

        //Preferible usar selectores por ID pero no hay en esta página para las filas.
        var createdRowFirstName = driver.findElement(By.xpath("//div[contains(@class, 'rt-td') and text() = 'Selenium FirstName']"));
        assertTrue(createdRowFirstName.isDisplayed());
    }

    @Test
    @DisplayName("Comprobar la búsqueda de la tabla")
    void searchBox(){
        driver.findElement(By.id("searchBox")).sendKeys("Alden");
        var filteredRow = driver.findElement(By.xpath("//div[contains(@class, 'rt-td') and text() = 'Alden']"));
        assertTrue(filteredRow.isDisplayed());

        assertThrows(
                NoSuchElementException.class,
                () -> driver.findElement(By.xpath("//div[contains(@class, 'rt-td') and text() = 'Cierra']"))
        );
    }
}
