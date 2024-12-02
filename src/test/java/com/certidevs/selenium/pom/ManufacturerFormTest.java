package com.certidevs.selenium.pom;

import com.certidevs.repository.ManufacturerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ManufacturerFormTest {

    @Autowired
    private ManufacturerRepository manufacturerRepo;
    private WebDriver driver;
    private ManufacturerFormPage page;

    @BeforeEach
    void setUp() {
        manufacturerRepo.deleteAllInBatch();

        driver = new ChromeDriver();
        driver.get("http://localhost:8080/manufacturers/new");
        driver.manage().window().maximize();
        page = new ManufacturerFormPage(driver);
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    // comprobar los campos vacíos cuando entras a crear
    // comprobar los campos rellenos cuando entras a editar
    // rellenar campos y enviar nueva creación
    // rellenar campos y enviar edición

}
