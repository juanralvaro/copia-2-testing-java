package com.certidevs.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double price;
    private Integer quantity;
    @Column(columnDefinition = "boolean default true")
    private Boolean active;

    //asociación
    @ManyToOne
    private Manufacturer manufacturer;

}
