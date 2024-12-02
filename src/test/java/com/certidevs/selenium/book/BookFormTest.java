package com.certidevs.selenium.book;

import com.certidevs.model.Book;
import com.certidevs.model.Category;
import com.certidevs.repository.BookRepository;
import com.certidevs.repository.CategoryRepository;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // ocupa un puerto aleatorio
public class BookFormTest {

    // Obtiene el puerto aleatorio
    @LocalServerPort
    int port;

    WebDriver driver;

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();

        var categories = categoryRepository.saveAll(List.of(
                Category.builder().name("cat1").build(),
                Category.builder().name("cat2").build(),
                Category.builder().name("cat3").build(),
                Category.builder().name("cat4").build()
        ));
        Book book1 = new Book();
        book1.setTitle("book1");
        book1.setPrice(20d);
        book1.getCategories().add(categories.get(0)); // cat1
        book1.getCategories().add(categories.get(2)); // cat3
        bookRepository.save(book1);

        Book book2 = Book.builder().title("book2").price(30d)
                .categories(new LinkedHashSet<>(Set.of(
                        categories.get(0), // cat1
                        categories.get(3) // cat4
                ))).build();
        bookRepository.save(book2);


        driver = new ChromeDriver();
        driver.get("http://localhost:" + port + "/libros");
        driver.manage().window().maximize();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    @DisplayName("Entrar a book-form y crear un nuevo libro y guardarlo")
    void bookFormEmpty() {
        driver.get("http://localhost:" + port + "/libros/crear");
        assertEquals("Crear libro", driver.findElement(By.tagName("h1")).getText());

        driver.findElement(By.id("title")).sendKeys("Libro Selenium");
        driver.findElement(By.id("price")).sendKeys("44.12");

        Select categoriesSelect = new Select(driver.findElement(By.id("categories")));
        List<WebElement> categories = categoriesSelect.getOptions();
        assertEquals(4, categories.size());
        // comprobar que las categorías tienen un text que empieza por "cat": cat1, cat2, cat3, cat4

        // opción 1
        categories.forEach(category -> assertTrue(category.getText().startsWith("cat")));

        // opción 2
        for (WebElement category : categories) {
            assertTrue(category.getText().startsWith("cat"));
        }

        // opción 3
        categories.stream()
                .map(c -> c.getText())
                .forEach(text -> assertTrue(text.startsWith("cat")));


        categoriesSelect.selectByVisibleText("cat1");
        categoriesSelect.selectByVisibleText("cat4");

        var selectedCategories = categoriesSelect.getAllSelectedOptions();
        assertEquals(2, selectedCategories.size());
        assertEquals("cat1", selectedCategories.get(0).getText());
        assertEquals("cat4", selectedCategories.get(1).getText());

        driver.findElement(By.id("btnSend")).click();

        assertEquals("http://localhost:" + port + "/libros", driver.getCurrentUrl());

        //buscar el libro en base de datos y comprobar la creación
        Book savedBook = bookRepository.findBookEagerByTitle("Libro Selenium").orElseThrow();
        assertEquals("Libro Selenium", savedBook.getTitle());
        assertEquals(44.12, savedBook.getPrice());
        assertEquals(2, savedBook.getCategories().size());
        List<String> categoryNames = savedBook.getCategories().stream().map(c -> c.getName()).toList();
        assertTrue(categoryNames.contains("cat1"));
        assertTrue(categoryNames.contains("cat4"));

    }
    @Test
    @DisplayName("Comprobar que en edición las categorías están preseleccionadas correctamente")
    void checkPreFilledCategoriesSelector(){
        Book book = bookRepository.findBookEagerByTitle("book1").orElseThrow();
        driver.get("http://localhost:" + port + "/libros/editar/" + book.getId());

        // Comprobar que aparecen preseleccionadas dos categorías
        Select categoriesSelect = new Select(driver.findElement(By.id("categories")));
        var selectedCategories = categoriesSelect.getAllSelectedOptions();
        assertEquals(2, selectedCategories.size());
        assertEquals("cat1", selectedCategories.get(0).getText());
        assertEquals("cat3", selectedCategories.get(1).getText());

    }
}