package com.familyshop.controller;

import com.familyshop.dto.ShoppingListDto;
import com.familyshop.model.Family;
import com.familyshop.model.ShoppingList;
import com.familyshop.service.FamilyService;
import com.familyshop.service.ShoppingListService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final FamilyService familyService;

    @PostMapping
    public ShoppingList addItem(@RequestBody ShoppingList item) {
        return shoppingListService.addItem(item);
    }

    @GetMapping("/{familyId}")
    public List<ShoppingListDto> getItems(@PathVariable Long familyId) {
        Family family = familyService.getFamilyById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));
        return shoppingListService.getItems(family);
    }

    @PutMapping("/{id}/bought")
    public ShoppingListDto markAsBought(@PathVariable Long id) {
        return shoppingListService.markAsBought(id);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        shoppingListService.deleteItem(id);
    }
}