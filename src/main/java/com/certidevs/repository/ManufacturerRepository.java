package com.certidevs.repository;

import com.certidevs.dto.ManufacturerWithAddressDTO;
import com.certidevs.dto.ManufacturerWithProductDataDTO;
import com.certidevs.model.Manufacturer;
import com.certidevs.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
    // métodos derivados
    List<Manufacturer> findByYear(Integer year);

    List<Manufacturer> findByNameIgnoreCase(String name);

    boolean existsByName(String name);

    // consultas JPQL
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    @Query("select m from Manufacturer m where m.name = :name")
    List<Manufacturer> findByName(String name);

    @Query("""
    SELECT new com.certidevs.dto.ManufacturerWithProductDataDTO(
        m.id,
        m.name,
        COUNT(p),
        SUM(p.price)
    ) FROM Manufacturer m
    LEFT JOIN Product p ON m.id = p.manufacturer.id
    GROUP BY m.id, m.name
    """)
    List<ManufacturerWithProductDataDTO> findAllWithCalculatedProductsStats();

    Manufacturer findByAddress_Id(Long id);

    @Query("select count(m) from Manufacturer m where m.address.zipcode = :zipcode")
    long countByAddress_Zipcode(String zipcode);


    @Query("""
    SELECT new com.certidevs.dto.ManufacturerWithAddressDTO(
        m.id,
        m.name,
        a.city,
        COUNT(p),
        SUM(p.price)
    ) FROM Manufacturer m
    JOIN m.address a
    LEFT JOIN Product p ON m.id = p.manufacturer.id
    WHERE a.city = :city
    GROUP BY m.id, m.name, a.city
    """)
    List<ManufacturerWithAddressDTO> findManufacturersInCityWithProductStats(@Param("city") String city); // Método que ejecuta la consulta y devuelve los resultados

}