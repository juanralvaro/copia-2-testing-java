package com.certidevs.selenium.certidevs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Test para comprobar el scroll de elementos que no están en el viewport
 */
public class CertiDevsScrollTest {

    WebDriver driver;



    @BeforeEach
    void setUp() {
        // --headless
        // --no-sandbox
        // --disable-dev-shm-usage
        driver = new ChromeDriver();
        driver.get("https://certidevs.com/curso-selenium");
        driver.manage().window().maximize();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    @DisplayName("Comprobar el scroll de la página")
    void testClickLesson() {

        // By.cssSelector("a[href*='/manufacturers/']")
//        WebElement lesson = driver.findElement(By.linkText("/tutorial-selenium-introduccion"));
//        WebElement lesson = driver.findElement(By.cssSelector("a[title='Introducción a Selenium']"));
//        assertTrue(lesson.isDisplayed());
//        lesson.click();
//        assertEquals(
//                "https://certidevs.com/tutorial-selenium-introduccion",
//                driver.getCurrentUrl()
//        );


        // Opción 1: Realizando scroll
        // el elemento no está en el viewport, es decir, está abajo, por tanto podría ser necesario hacer scroll para ubicarlo:
//        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true)", lesson);
//        lesson.click();
//        assertEquals(
//                "https://certidevs.com/tutorial-selenium-introduccion",
//                driver.getCurrentUrl()
//        );

        // Opción 2: Action
/*
        new Actions(driver).moveToElement(lesson).perform();
        lesson.click();
        assertEquals(
                "https://certidevs.com/tutorial-selenium-introduccion",
                driver.getCurrentUrl()
        );
*/
        // Opción 3: Action con wait
        WebElement lesson = driver.findElement(By.cssSelector("a[title='Introducción a Selenium']"));
        new WebDriverWait(driver, Duration.ofSeconds(300))
                .until(driver -> {
                    new Actions(driver).moveToElement(lesson).perform();
                    return lesson.isDisplayed();
                });
        lesson.click();
        assertEquals(
                "https://certidevs.com/tutorial-selenium-introduccion",
                driver.getCurrentUrl()
        );
    }
}