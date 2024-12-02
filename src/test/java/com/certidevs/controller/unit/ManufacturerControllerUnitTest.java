package com.certidevs.controller.unit;

import com.certidevs.controller.ManufacturerController;
import com.certidevs.model.Manufacturer;
import com.certidevs.repository.ManufacturerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*; // añadir en todos los tests
import static org.mockito.Mockito.*; // añadir en todos los tests


/**
 * Test unitario utilizando JUnit y Mockito para los mocks
 * No carga Spring, no inicializa la app Spring con base de datos
 * Test: aislado, rápido, fácil, unitario
 */
@ExtendWith(MockitoExtension.class)
class ManufacturerControllerUnitTest {

    // system under test - SUT
    @InjectMocks
    private ManufacturerController manufacturerController;
    // Dependencias o escenario necesario - fixture
    @Mock
    private ManufacturerRepository manufacturerRepository;
    @Mock
    private Model model;

    @Test
    void findAll() {

        //1. configurar las respuestas de los mocks
        Manufacturer man1 = Manufacturer.builder().id(1L).build();
        Manufacturer man2 = Manufacturer.builder().id(2L).build();
        List <Manufacturer> manufacturers = List.of(man1, man2);
        when(manufacturerRepository.findAll()).thenReturn(manufacturers);

        //2. ejecutar método a testear
        String view = manufacturerController.findAll(model);

        //3. verificación y aserciones
        assertEquals("manufacturer-list", view);
        verify(manufacturerRepository).findAll();
        verify(model).addAttribute(("manufacturers"), manufacturers);

    }

    @Test
    @DisplayName("findById que SÍ tiene fabricante")
    void findById_WhenManufacturerExists() {

        //1. configurar respuestas mocks
        Manufacturer adidas = Manufacturer.builder().id(1L).name("Adidas").build();
        Optional<Manufacturer> adidasOpt = Optional.of(adidas);

        when(manufacturerRepository.findById(1L)).thenReturn(adidasOpt);

        //2. ejecutar método a testear
        String view = manufacturerController.findById(1L, model);

        //3. assert y verify
        assertEquals("manufacturer-detail", view);
        verify(manufacturerRepository).findById(1L);
        verify(model).addAttribute("manufacturer", adidas);
    }

    @Test
    @DisplayName("find ById que NO tiene fabricante")
    void findById_WhenManufacturerNotExists() {

        //1. configurar respuestas mocks
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.empty());

        //2. ejecutar método a testear
        String view = manufacturerController.findById(1L, model);

        //3. assert y verify
        assertEquals("manufacturer-detail", view);
        verify(manufacturerRepository).findById(1L);

        // Verificar que el model.AddAttribute no haya sido invocado nunca
        verify(model, never()).addAttribute(anyString(), any());

    }

    @Test
    @DisplayName("Método que te desplaza al formulario de manufacturers vacío")
    void getFormToCreate() {

        String view = manufacturerController.getFormToCreate(model);

        assertEquals("manufacturer-form", view);
        verify(model).addAttribute(eq("manufacturer"), any(Manufacturer.class));

    }

    @Test
    void getFormToUpdate_Exists() {
        //1. configurar respuestas mocks
        Manufacturer adidas = Manufacturer.builder().id(1L).name("Adidas").build();
        Optional<Manufacturer> adidasOpt = Optional.of(adidas);

        when(manufacturerRepository.findById(1L)).thenReturn(adidasOpt);

        //2. ejecutar método a testear
        String view = manufacturerController.getFormToUpdate(1L, model);

        //3. assert y verify
        assertEquals("manufacturer-form", view);
        verify(manufacturerRepository).findById(1L);
        verify(model).addAttribute("manufacturer", adidas);
    }

    @Test
    void getFormToUpdate_NotExists() {
        //1. configurar respuestas mocks
        when(manufacturerRepository.findById(1L)).thenReturn(Optional.empty());

        //2. ejecutar método a testear
        //CÓDIGO ACTUALIZADO PORQUE AHORA EL CONTROLADOR LANZA EXCEPCIÓN
        assertThrows(ResponseStatusException.class, () ->
                manufacturerController.getFormToUpdate(1L, model));

        //CÓDIGO ANTIGUO, SI NO HAY EXCEPCIÓN
//        String view = manufacturerController.getFormToUpdate(1L, model);
//
//        //3. assert y verify
//        assertEquals("manufacturer-form", view);
//        verify(manufacturerRepository).findById(1L);
//
//        // Verificar que el model.AddAttribute no haya sido invocado nunca
//        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    void save_ManufacturerNew() {

        // 1. Preparar datos y mocks
        Manufacturer manufacturer = new Manufacturer();

        // 2. Invocar método a testear
        String view = manufacturerController.save(manufacturer);

        // 3. Verificaciones
        verify(manufacturerRepository).save(manufacturer);
        assertEquals("redirect:/manufacturers", view);

    }

    @Test
    void save_ManufacturerExistsUpdate() {

// 1. Preparar datos y mocks
        Manufacturer manufacturerToUpdate = new Manufacturer();
        manufacturerToUpdate.setId(1L);
        manufacturerToUpdate.setName("Adidas modificado");

        Manufacturer manufacturerFromDB = new Manufacturer();
        manufacturerFromDB.setId(1L);
        manufacturerFromDB.setName("Adidas");

        Optional<Manufacturer> manufacturerFromDBOptional = Optional.of(manufacturerFromDB);
        when(manufacturerRepository.findById(1L)).thenReturn(manufacturerFromDBOptional);

// 2. invocar metodo a testear
        String view = manufacturerController.save(manufacturerToUpdate);

// 3. aserciones y verificaciones
        assertEquals("redirect:/manufacturers", view);
        verify(manufacturerRepository).findById(1L);
        verify(manufacturerRepository).save(manufacturerFromDB);
        assertEquals("Adidas modificado", manufacturerFromDB.getName());

    }

    @Test
    void saveAndGoDetail_NewManufacturer() {

        // Si el método save no devuelve nada y sólo modifica el ID
        // no vale thenReturn y necesito un enfoque más avanzado
        // doAnswer PERMITE CAPTURAR ARGUMENTOS Y MODIFICARLOS,
        // ENFOQUE MÁS AVANZADO QUE EL thenReturn

        // OPCIÓN 1: con clase anónima
        //doAnswer(new Answer() {
        //    public Object answer(InvocationOnMock invocation) {
        //        Manufacturer manufacturerToSave = invocation.getArgument(0);
        //        manufacturerToSave.setId(1L);
        //        return null;
        //    }
        //})
        //        .when(manufacturerRepository).save(manufacturer);

        // OPCIÓN 2: con lambda
        Manufacturer manufacturer = new Manufacturer();
        doAnswer(invocation -> {
            Manufacturer manufacturerToSave = invocation.getArgument(0);
            manufacturerToSave.setId(1L);
            return null;
        }).when(manufacturerRepository).save(any());


        // 2. Invocar método a testear
        String view = manufacturerController.saveAndGoDetail(manufacturer);

        // 3. Verificaciones
        verify(manufacturerRepository).save(manufacturer);
        assertEquals("redirect:/manufacturers/1", view);

    }

    @Test
    void deleteById() {

        String view = manufacturerController.deleteById(1L);
        assertEquals("redirect:/manufacturers", view);
        verify(manufacturerRepository).deleteById(1L);
    }
}