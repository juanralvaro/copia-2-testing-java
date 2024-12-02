package com.certidevs.controller.integration;

import com.certidevs.controller.ManufacturerController;
import com.certidevs.model.Manufacturer;
import com.certidevs.repository.ManufacturerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/* TEST DE INTEGRACIÓN CON SPRING TEST (PARCIAL)
No es un test unitario
Es un test de integración parcial porque se hace mocks con @MockBean
Hacemos mock de la capa repositorio
Se integra la capa controlador y vista con el contexto Spring
*/

// @SpringBootTest
// @AutoConfigureMockMvc // Configura MockMvc
@WebMvcTest(ManufacturerController.class)
public class ManufacturerControllerPartialIntegrationTest {

    // Utilidad de testing para lanzar y tantear peticiones HTTP
    @Autowired // Anotación de Spring para inyectar objetos en clases
    private MockMvc mockMvc;

    @MockBean // Hacer mock del repositorio
    private ManufacturerRepository manufacturerRepository;

    @Test
    void findAll() throws Exception {

        when(manufacturerRepository.findAll()).thenReturn(List.of(
                Manufacturer.builder().id(1L).name("MSI").build(),
                Manufacturer.builder().id(2L).name("Asus").build()
        ));

        // Lanzar peticiones HTTP a los controladores de Spring
        mockMvc.perform(get("/manufacturers"))
                .andExpect(view().name("manufacturer-list"))
                .andExpect(model().attributeExists("manufacturers"))
                .andExpect(model().attribute("manufacturers", hasSize(2)))
                .andExpect(model().attribute("manufacturers", hasItem(
                        allOf(
                                hasProperty("id", is(1L)),
                                hasProperty("name", is("MSI"))
                        )
                )));

    }

    @Test
    void findById() throws Exception {
        var manufacturer = Manufacturer.builder().id(1L).name("MSI").build();
        when(manufacturerRepository.findById(1L))
                .thenReturn(Optional.of(manufacturer));

        mockMvc.perform(get("/manufacturers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer-detail"))
                .andExpect(model().attributeExists("manufacturer"))
                .andExpect(model().attribute("manufacturer",
                        allOf(
                                hasProperty("id", is(1L)),
                                hasProperty("name", is("MSI"))
                        )
                ));

        verify(manufacturerRepository).findById(1L);
    }

    @Test
    void getFormToUpdate_Empty() throws Exception {
        when(manufacturerRepository.findById(1L))
                .thenReturn(Optional.empty());

        //El Error Controller es un Advice que captura los errores y los devuelve como status 400
        //400 Bad Request
        mockMvc.perform(get("/manufacturers/update/{id}", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void save_CreateNew() throws Exception {

        var manufacturer = Manufacturer.builder()
                .name("MSI").year(2024).build();

        mockMvc.perform(post("/manufacturers")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "MSI")
                        .param("year", "2024")
                )
                .andExpect(status().is3xxRedirection()) // 303
                .andExpect(redirectedUrl("/manufacturers"));

        verify(manufacturerRepository).
                save(Mockito.any(Manufacturer.class));

    }

    @Test
    void save_update() throws Exception {

        var manufacturer = Manufacturer.builder()
                .id(1L)
                .name("MSI")
                .year(2004)
                .build();
        when(manufacturerRepository.findById(1L)).thenReturn(
                Optional.of(manufacturer));

        mockMvc.perform(
                        post("/manufacturers")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("id", "1")  // poner id para que sea un update
                                .param("name", "MSI Modificado")
                                .param("year", "2025")
                ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturers"));

        verify(manufacturerRepository).findById(1L);
        verify(manufacturerRepository).save(manufacturer);
    }

    @Test
    void deleteById() throws Exception {

        mockMvc.perform(
                        get("/manufacturers/delete/{id}", 1L)
                ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturers"));

        verify(manufacturerRepository).deleteById(1L);

    }

    @Test
    @DisplayName("Crear un nuevo fabricante que no existe en la base de datos")
    void saveAndGoDetail_CreateNew() throws Exception {

        // Cuando el método a testear no es void, y queremos capturar el argumento,
        // para modificarlo, usamos when(...).thenAnswer(...)
        when(manufacturerRepository.save(
                Mockito.any(Manufacturer.class)
        )).thenAnswer(invocation -> {
            Manufacturer manufacturerToSave = invocation.getArgument(0);
            manufacturerToSave.setId(1L); // simular que la base de datos asigna un ID al objeto manufacturer
            return null;
        });

        mockMvc.perform(
                        post("/manufacturers2")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("name", "MSI")
                            .param("year", "2024")
                ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturers/1"));

        verify(manufacturerRepository).save(Mockito.any(Manufacturer.class));
    }

    @Test
    void saveAndGoDetail_UpdateExistent() throws Exception {
        var manufacturer = Manufacturer.builder()
                .id(1L)
                .name("MSI")
                .year(2024)
                .build();
        when(manufacturerRepository.findById(1L)).thenReturn(
                Optional.of(manufacturer)
        );

        mockMvc.perform(
                        post("/manufacturers2")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("id", "1") // poner id para que sea una update
                                .param("name", "MSI Modificado")
                                .param("year", "2025")
                ).andExpect(status().is3xxRedirection())
// Como ahora redirecciona hacia el detail verificamos el ID
                .andExpect(redirectedUrl("/manufacturers/1"));

        verify(manufacturerRepository).findById(1L);
        verify(manufacturerRepository).save(manufacturer);
    }
}