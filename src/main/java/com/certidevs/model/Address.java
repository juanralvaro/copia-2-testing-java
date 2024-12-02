package com.certidevs.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Address {

    @Id // Indica que este campo es la clave primaria de la entidad
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Especifica que el ID se generará automáticamente
    private Long id; // Identificador único de la dirección
    private String street; // Calle de la dirección
    private String city; // Ciudad de la dirección
    private String state; // Estado de la dirección
    private String zipcode; // Código postal de la dirección

    @OneToOne(mappedBy = "address", cascade = CascadeType.ALL) // Definir la relación inversa y la cascada
    @ToString.Exclude // Excluye este campo del método toString para evitar bucles
    private Manufacturer manufacturer; // Referencia a la entidad Manufacturer

}
