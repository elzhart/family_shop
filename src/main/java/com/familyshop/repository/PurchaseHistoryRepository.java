package com.familyshop.repository;

import com.familyshop.model.PurchaseHistory;
import com.familyshop.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
    List<PurchaseHistory> findByFamily(Family family);
}