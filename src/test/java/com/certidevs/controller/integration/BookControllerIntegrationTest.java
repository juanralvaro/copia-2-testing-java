package com.certidevs.controller.integration;

import com.certidevs.model.Book;
import com.certidevs.model.Category;
import com.certidevs.repository.BookRepository;
import com.certidevs.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/*
Testing integración completa de ProductController con modelo y vista
 */

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // cada test se ejecuta en una transacción que se revierte al acabar para dejar la db limpia
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll(); // Elimina todos los registros
        categoryRepository.deleteAll();
    }

    @Test
    void findAll() throws Exception{
        bookRepository.saveAll(List.of(
                Book.builder().title("libro1").price(20.0).build(),
                Book.builder().title("libro2").price(23.0).build()
        ));
        mockMvc.perform(get("/libros"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-list"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("books", hasSize(2)));
    }

    @Test
    @DisplayName("Enviar formulario con nuevo libro para crearlo en base de datos")
    void crearNuevoLibro() throws Exception{

        var cat1 = categoryRepository.save(Category.builder().name("cat1").build());
        var cat2 = categoryRepository.save(Category.builder().name("cat2").build());
        var cat3 = categoryRepository.save(Category.builder().name("cat3").build());

        String id1 = String.valueOf(cat1.getId());
        String id2 = String.valueOf(cat2.getId());
        String id3 = String.valueOf(cat3.getId());

        mockMvc.perform(post("/libros")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "LibroTest")
                .param("price", "23.44")
                .param("categories", id1, id2, id3)
        ).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/libros"));

        var bookSaved = bookRepository.findByTitleLikeIgnoreCase("LibroTest").get(0);
        assertEquals("LibroTest", bookSaved.getTitle());
        assertEquals(23.44, bookSaved.getPrice());
        assertEquals(3, bookSaved.getCategories().size());

    }

    @Test
    @DisplayName("Editar un libro que ya existe")
    void editarLibroExistente() throws Exception{

        var cat1 = categoryRepository.save(Category.builder().name("cat1").build());
        var cat2 = categoryRepository.save(Category.builder().name("cat2").build());
        var cat3 = categoryRepository.save(Category.builder().name("cat3").build());

        Book book1 = new Book();
        book1.setTitle("libro1");
        book1.setPrice(30.5);
        book1.getCategories().add(cat1);
        book1.getCategories().add(cat1);
        bookRepository.save(book1);

        String id1 = String.valueOf(cat1.getId());
        String id2 = String.valueOf(cat2.getId());
        String id3 = String.valueOf(cat3.getId());

        mockMvc.perform(post("/libros")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", String.valueOf(book1.getId()))
                        .param("title", "LibroTest")
                        .param("price", "23.44")
                        .param("categories", id1, id3)
                ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/libros"));

        var bookSaved = bookRepository.findByTitleLikeIgnoreCase("LibroTest").get(0);
        assertEquals("LibroTest", bookSaved.getTitle());
        assertEquals(23.44, bookSaved.getPrice());
        assertEquals(2, bookSaved.getCategories().size());

        assertTrue(bookSaved.getCategories().contains(cat1));
        assertFalse(bookSaved.getCategories().contains(cat2));
        assertTrue(bookSaved.getCategories().contains(cat3));

    }
}