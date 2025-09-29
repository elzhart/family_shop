package com.familyshop.service;

import com.familyshop.dto.ShoppingListDto;
import com.familyshop.events.EventType;
import com.familyshop.events.FamilyEventPublisher;
import com.familyshop.model.Family;
import com.familyshop.model.ShoppingList;
import com.familyshop.repository.ShoppingListRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShoppingListServiceTest {

    @Mock
    ShoppingListRepository shoppingRepo;

    @Mock
    FrequentItemService frequentItemService;

    @Mock
    FamilyEventPublisher events;

    @Mock
    PurchaseHistoryService purchaseHistoryService;

    @InjectMocks
    ShoppingListService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("addItem — happy: сохраняет и шлёт событие")
    void addItem_ok() {
        ShoppingList req = new ShoppingList(null, new Family(1L, "F"), "Молоко", "2", false);
        ShoppingList saved = new ShoppingList(1L, new Family(1L, "F"), "Молоко", "2", false);

        when(shoppingRepo.save(any(ShoppingList.class))).thenReturn(saved);

        ShoppingList result = service.addItem(req);

        assertEquals(1L, result.getId());
        verify(shoppingRepo, times(1)).save(any(ShoppingList.class));
        verify(events).send(eq(1L), eq(EventType.SHOPPING_ADDED), any());
    }

    @Test
    @DisplayName("markAsBought — unhappy: item не найден ⇒ NotFoundException")
    void markAsBought_notFound() {
        when(shoppingRepo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.markAsBought(999L));
    }

    @Test
    @DisplayName("markAsBought — happy: обновляет isBought и шлёт событие")
    void markAsBought_ok() {
        ShoppingList db = new ShoppingList(1L, new Family(1L, "F"), "Хлеб", "1", false);
        when(shoppingRepo.findById(1L)).thenReturn(Optional.of(db));
        when(shoppingRepo.save(any(ShoppingList.class))).thenReturn(db);

        ShoppingListDto result = service.markAsBought(1L);
        assertTrue(result.isBought());
        verify(events).send(eq(1L), eq(EventType.SHOPPING_BOUGHT), any());
        verify(purchaseHistoryService).addToHistory(any(), eq("Хлеб"), eq("1"));
        verify(frequentItemService).updateFrequency(any(), eq("Хлеб"));
        verify(events).send(eq(1L), eq(EventType.SHOPPING_BOUGHT), any());
    }
}
