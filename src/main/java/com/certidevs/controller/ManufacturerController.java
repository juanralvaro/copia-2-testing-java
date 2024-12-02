package com.certidevs.controller;

import com.certidevs.model.Address;
import com.certidevs.model.Manufacturer;
import com.certidevs.repository.ManufacturerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Controller
public class ManufacturerController {

    //Objeto inicializado e inyectado por Spring
    private ManufacturerRepository manufacturerRepo;

    //http://localhost:8080/fabricantes
    @GetMapping("manufacturers")
    public String findAll(Model model){
        model.addAttribute("manufacturers", manufacturerRepo.findAll());
        return "manufacturer-list";
    }

    //http://localhost:8080/fabricantes/1
    @GetMapping("manufacturers/{id}")
    public String findById(@PathVariable Long id, Model model) {
        manufacturerRepo.findById(id)
                .ifPresent(manufacturer -> model.addAttribute("manufacturer", manufacturer));
        //Extra: podemos cargar más datos, por ejemplo productos de ese manufacturer
        return "manufacturer-detail";
    }

    //http://localhost:8080/fabricantes/new
    @GetMapping("manufacturers/new")
    public String getFormToCreate(Model model) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setAddress(new Address());
        model.addAttribute("manufacturer", manufacturer);
        //model.addAttribute("manufacturer", new Manufacturer());
        return "manufacturer-form";
    }

    //http://localhost:8080/fabricantes/edit/3
    @GetMapping("manufacturers/update/{id}")
    public String getFormToUpdate(@PathVariable Long id, Model model) {
        manufacturerRepo.findById(id)
                .ifPresentOrElse(
                        manufacturer -> {
                            if (manufacturer.getAddress() == null) { // Verifica si la dirección del fabricante es nula
                                manufacturer.setAddress(new Address());
                            }
                            model.addAttribute("manufacturer", manufacturer); // Añade el fabricante al modelo para usarlo
                        },
                        () -> { // Si el fabricante no se encuentra
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fabricante no encontrado"); // Lanza un error 404
                        });
                        //manufacturer -> model.addAttribute("manufacturer", manufacturer),
                        //() -> {
                        //    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
                        //});
        return "manufacturer-form";
    }

    @PostMapping("manufacturers")
    public String save(@ModelAttribute Manufacturer manufacturer) {

        if (manufacturer.getId() == null) { // Verifica si el ID es nulo (crear nuevo fabricante)
            manufacturerRepo.save(manufacturer); // Guarda el nuevo fabricante en la base de datos

        } else { // Si el ID no es nulo (editar fabricante existente)
            manufacturerRepo.findById(manufacturer.getId()).ifPresent(manufacturerDB -> {
                BeanUtils.copyProperties(manufacturer, manufacturerDB, "id", "address"); // Copia las propiedades excepto ID y Address

                // Actualizar la dirección asociada
                if(manufacturer.getAddress() != null) { // Verifica si se proporcionó una dirección nueva
                    if (manufacturerDB.getAddress() == null) { // Si no hay una dirección existente
                        manufacturerDB.setAddress(manufacturer.getAddress()); // Asigna la nueva dirección al fabricante
                    } else {
                        BeanUtils.copyProperties(manufacturer.getAddress(), manufacturerDB.getAddress(), "id"); // Actualiza la direccón existente menos el ID
                    }

                }
                //BeanUtils.copyProperties(manufacturer, manufacturerDB);
                manufacturerRepo.save(manufacturerDB); // Guarda los cambios del fabricante en la base de datos
            });
        }

        return "redirect:/manufacturers"; // Redirige a la lista de fabricantes
    }

    @PostMapping("manufacturers2") // Maneja solicitudes POST a la URL /manufacturers2
    public String saveAndGoDetail(@ModelAttribute Manufacturer manufacturer) { // Método que recibe un objeto y guarda los cambios en la base de datos

        if (manufacturer.getId() == null) { // Verifica si el ID es nulo (crear nuevo fabricante)
            manufacturerRepo.save(manufacturer); // Guarda el nuevo fabricante en la base de datos

        } else { // Si el ID no es nulo (editar existente)
            manufacturerRepo.findById(manufacturer.getId()).ifPresent(manufacturerDB -> { // Verifica si el fabricante existe
                BeanUtils.copyProperties(manufacturer, manufacturerDB, "id", "address"); // Copia  propiedades excepto ID y Address

                //Actualizar la dirección asociada
                if(manufacturer.getAddress() != null) { // Verifica si hay una dirección nueva proporcionada
                    if (manufacturerDB.getAddress() == null) { // Si no hay una dirección existente
                        manufacturerDB.setAddress(manufacturer.getAddress()); // Asigna la nueva dirección al fabricante
                    } else { // Si ya existe una dirección
                        BeanUtils.copyProperties(manufacturer.getAddress(), manufacturerDB.getAddress(), "id"); // Actualiza la dirección existente menos el ID
                    }

                }
//                BeanUtils.copyProperties(manufacturer, manufacturerDB);
                manufacturerRepo.save(manufacturerDB);
            });
        }

        return "redirect:/manufacturers/" + manufacturer.getId(); // Redirige a la página detalle del fabricante
    }

    //http://localhost:8080/manufacturers/delete/3
    @GetMapping("manufacturers/delete/{id}")
    public String deleteById(@PathVariable Long id) {
        manufacturerRepo.deleteById(id);
        return "redirect:/manufacturers";
    }


}
