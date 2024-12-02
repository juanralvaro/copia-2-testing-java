package com.certidevs.controller;

import com.certidevs.model.Book;
import com.certidevs.model.Product;
import com.certidevs.repository.BookRepository;
import com.certidevs.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@AllArgsConstructor
@Controller
public class BookController {

    private final CategoryRepository categoryRepository;
    private BookRepository bookRepository;


    @GetMapping("libros")
    public String findAll(Model model){
        model.addAttribute("books", bookRepository.findAll());
        return "book-list";
    }
    @GetMapping("libros/{id}")
    public String findById(@PathVariable Long id, Model model) {
        bookRepository.findById(id).ifPresent(b -> model.addAttribute("book", b));
        return "book-detail";
    }
    @GetMapping("libros/crear")
    public String getFormToCreate(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryRepository.findAll()); // Para el selector de categorías del formulario libro
        return "book-form";
    }
    @GetMapping("libros/editar/{id}")
    public String getFormToEdit(Model model, @PathVariable Long id) {
        bookRepository.findById(id).ifPresent(b -> model.addAttribute("book", b));
        model.addAttribute("categories", categoryRepository.findAll()); // Para el selector de categorías del formulario libro
        return "book-form";
    }
    @PostMapping("libros")
    public String save(@ModelAttribute Book book) {
        bookRepository.save(book);
        return "redirect:/libros";
    }





}
