package com.familyshop.service;

import com.familyshop.dto.ShoppingListDto;
import com.familyshop.events.EventType;
import com.familyshop.events.FamilyEventPublisher;
import com.familyshop.mapper.ShoppingListMapper;
import com.familyshop.model.Family;
import com.familyshop.model.ShoppingList;
import com.familyshop.repository.ShoppingListRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final PurchaseHistoryService purchaseHistoryService;
    private final FrequentItemService frequentItemService;
    private final FamilyEventPublisher eventPublisher;

    public ShoppingList addItem(ShoppingList item) {
        var saved = shoppingListRepository.save(item);

        // оповещаем фронт
        eventPublisher.send(saved.getId(), EventType.SHOPPING_ADDED, saved);
        return saved;
    }

    public List<ShoppingListDto> getItems(Family family) {
        List<ShoppingList> byFamilyAndIsBought = shoppingListRepository.findByFamilyAndIsBought(family, false);
        return byFamilyAndIsBought
                .stream()
                .map(ShoppingListMapper::toDto)
                .toList();
    }

    @Transactional
    public ShoppingListDto markAsBought(Long id) {
        ShoppingList item = shoppingListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setIsBought(true);
        ShoppingList saved = shoppingListRepository.save(item);

        purchaseHistoryService.addToHistory(item.getFamily(), item.getItemName(), item.getQuantity());
        frequentItemService.updateFrequency(item.getFamily(), item.getItemName());

        ShoppingListDto dto = ShoppingListMapper.toDto(saved);
        eventPublisher.send(item.getFamily().getId(), EventType.SHOPPING_BOUGHT, dto);
        return dto;
    }

    public void deleteItem(Long id) {
        ShoppingList item = shoppingListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        shoppingListRepository.deleteById(id);
        eventPublisher.send(item.getFamily().getId(), EventType.SHOPPING_DELETED, item.getId());
    }
}