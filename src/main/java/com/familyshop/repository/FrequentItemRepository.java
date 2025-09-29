package com.familyshop.repository;

import com.familyshop.model.FrequentItem;
import com.familyshop.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequentItemRepository extends JpaRepository<FrequentItem, Long> {
    List<FrequentItem> findByFamilyOrderByFrequencyDesc(Family family);
}