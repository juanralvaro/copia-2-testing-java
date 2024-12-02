package com.certidevs.selenium.pom;

import lombok.Builder;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

/*
POM de Manufacturer List, es decir, esta clase tiene todos los WebElement importantes
que se van a testear en ManufacturerList
 */
// page_url = http://localhost:8080/manufacturers
public class ManufacturerListPage {

    private WebDriver driver;

    @FindBy(tagName = "h1")
    public WebElement h1;

    @FindBy(id = "createButton")
    public WebElement createButton;

    @FindBy(id = "manufacturersTable")
    public WebElement manufacturersTable;

    @FindBy(id = "manufacturersEmpty")
    public WebElement manufacturersEmpty;

    public ManufacturerListPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    // Grid / Tabla con un número X de fabricantes:
    public WebElement getManufacturerName(Long id){
        return driver.findElement(By.id("manufacturerName_" + id));
    }

    public List<WebElement> getManufacturerNames(){
        return driver.findElements(By.cssSelector("[id^='manufacturerName_']"));
    }

    public void clickViewButton(Long manufacturerId) {
        driver.findElement(
                By.id("manufacturerActionView_" + manufacturerId)
        ).click();
    }

    /**
     * Opcional: crear un objeto ManufacturerCard para obtener todos los webElements agrupados en un mismo objeto
     * Método que obtiene un objeto card con todos los webElements del fabricante
     * image, name, description, year, viewButton, editButton, deleteButton
     * @param manufacturerId
     * return ManufacturerCard objeto para agrupar todos los webElements
     */

    public ManufacturerCard getManufacturer(Long manufacturerId) {

        var image = driver.findElement(By.id("manufacturerImageUrl_" + manufacturerId));
        var name = driver.findElement(By.id("manufacturerName_" + manufacturerId));
        var descr = driver.findElement(By.id("manufacturerDescription_" + manufacturerId));
        var year = driver.findElement(By.id("manufacturerYear_" + manufacturerId));
        var viewButton = driver.findElement(By.id("manufacturerActionView_" + manufacturerId));
        var editButton = driver.findElement(By.id("manufacturerActionEdit_" + manufacturerId));
        var deleteButton = driver.findElement(By.id("manufacturerActionDelete_" + manufacturerId));
        return ManufacturerCard.builder()
                .image(image)
                .name(name)
                .description(descr)
                .year(year)
                .viewButton(viewButton)
                .editButton(editButton)
                .deleteButton(deleteButton)
                .build();
    }

    // OPCIONAL:
    @Builder
    @Getter
    public static class ManufacturerCard {
        private WebElement image;
        private WebElement name;
        private WebElement description;
        private WebElement year;
        private WebElement viewButton;
        private WebElement editButton;
        private WebElement deleteButton;
    }
}






// Atributos: WebElement concretos de la página
// botón crear
// tabla
// emptyList

// métodos: acciones concretas
// clic en crear
// devolver boolean de si tabla está visible
// devolver boolean de si emptyList está visible
