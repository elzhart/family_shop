package com.familyshop.repository;

import com.familyshop.model.ShoppingList;
import com.familyshop.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    List<ShoppingList> findByFamilyAndIsBought(Family family, Boolean isBought);
}