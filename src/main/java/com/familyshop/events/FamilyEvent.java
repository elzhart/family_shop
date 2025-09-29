package com.familyshop.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyEvent {
    private EventType type;     // например: SHOPPING_ADDED, SHOPPING_UPDATED, SHOPPING_BOUGHT
    private Long familyId;   // для роутинга на нужный топик
    private Object payload;  // опционально: объект/данные события
}