package com.certidevs.selenium.pom;
/*
POM de ManufacturerFormpage
Esta clase tiene todos los webElements a testear en esta pantalla
 */

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ManufacturerFormPage {
    @FindBy(id = "h1")
    private WebElement h1;
    @FindBy(id = "id")
    private WebElement id;
    @FindBy(id = "name")
    private WebElement name;
    @FindBy(id = "description")
    private WebElement description;
    @FindBy(id = "imageUrl")
    private WebElement imageUrl;
    @FindBy(id = "year")
    private WebElement year;
    // Address
    @FindBy(id = "street")
    private WebElement street;
    @FindBy(id = "city")
    private WebElement city;
    @FindBy(id = "state")
    private WebElement state;
    @FindBy(id = "zipcode")
    private WebElement zipcode;
    // Botones
    @FindBy(id = "saveNewButton")
    private WebElement saveNewButton;
    @FindBy(id = "saveEditButton")
    private WebElement saveEditButton;

    private WebDriver driver;
    public ManufacturerFormPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    public void fillInput(WebElement input, String text) {
        input.clear();
        input.sendKeys(text);
    }
}