package com.familyshop.service;

import com.familyshop.events.EventType;
import com.familyshop.events.FamilyEventPublisher;
import com.familyshop.mapper.PurchaseHistoryMapper;
import com.familyshop.model.Family;
import com.familyshop.model.PurchaseHistory;
import com.familyshop.repository.PurchaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseHistoryService {

    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final FamilyEventPublisher eventPublisher;

    public PurchaseHistory addToHistory(Family family, String itemName, String quantity) {
        PurchaseHistory history = PurchaseHistory.builder()
                .family(family)
                .itemName(itemName)
                .quantity(quantity)
                .build();
        PurchaseHistory saved = purchaseHistoryRepository.save(history);

        eventPublisher.send(family.getId(), EventType.HISTORY_ADDED, PurchaseHistoryMapper.toDto(saved));
        return saved;
    }

    public List<PurchaseHistory> getHistory(Family family) {
        return purchaseHistoryRepository.findByFamily(family);
    }
}