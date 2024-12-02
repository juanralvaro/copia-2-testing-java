package com.certidevs.model;

import jakarta.persistence.*;
import jdk.jshell.Snippet;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Manufacturer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    @Column(name = "manufacturer_year")
    private Integer year;

    @OneToOne(cascade = CascadeType.ALL) // Define una relación OneToOne con Address
    @JoinColumn(name = "address_id", referencedColumnName = "id") // Especifica la columna de unión en la tabla manufacturer
    private Address address; // Dirección asociada al fabricante

}
