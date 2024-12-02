package com.certidevs.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class InterfacesFuncionalesUnitTest {


    // Predicate: boolean test(T t)

    @Test
    void predicateTest() {

        List<String> nombres = List.of("persona1", "persona2", "amigo1", "amigo2");
        List<String> amigos = nombres.stream().filter(nombre -> nombre.startsWith("amigo")).toList();

    }

    // Consumer: void accept(T t)

    @Test
    void consumerTest() {

        List<String> nombres = List.of("persona1", "persona2", "amigo1", "amigo2");
        nombres.forEach(nombre -> System.out.println(nombre));
    }

    // Function: R apply (T t)

    @Test
    void functionTest() {
        List<String> nombres = List.of("persona1", "persona2", "amigo1", "amigo2");
        List<String> nombresModificados = nombres.stream().map(nombre -> nombre.toUpperCase()).toList();
    }

    // Supplier: T get();

    @Test
    void supplierTest() {
        Optional<String> nombreOptional = Optional.of("nombre1");

        String nombre = nombreOptional.orElse("alternativa");

        String nombre2 = nombreOptional.map(n -> n.toUpperCase()).orElse("alternativo");

        nombreOptional.orElseGet( () -> {
            System.out.println("No existe el nombre");
            return "alternativo";
        });
    }


}