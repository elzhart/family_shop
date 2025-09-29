package com.familyshop.mapper;

import com.familyshop.dto.ShoppingListDto;
import com.familyshop.model.ShoppingList;

public class ShoppingListMapper {
    public static ShoppingListDto toDto(ShoppingList shoppingList) {
        return new ShoppingListDto(
                shoppingList.getId(),
                FamilyMapper.toFamilyDto(shoppingList.getFamily()),
                shoppingList.getItemName(),
                shoppingList.getQuantity(),
                shoppingList.getIsBought()
        );

    }

}