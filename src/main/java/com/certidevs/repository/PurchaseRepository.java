package com.certidevs.repository;

import com.certidevs.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByEmail(String email);

//    List<Purchase> findByPurchaseDateBetween(LocalDateTime dateStart, LocalDateTime dateEnd);


}
