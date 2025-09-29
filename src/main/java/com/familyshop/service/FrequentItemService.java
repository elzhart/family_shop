package com.familyshop.service;

import com.familyshop.dto.FrequentItemDto;
import com.familyshop.events.EventType;
import com.familyshop.events.FamilyEventPublisher;
import com.familyshop.mapper.FrequentItemMapper;
import com.familyshop.model.Family;
import com.familyshop.model.FrequentItem;
import com.familyshop.repository.FrequentItemRepository;

import org.springframework.stereotype.Service;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FrequentItemService {

    private final FrequentItemRepository frequentItemRepository;
    private final FamilyEventPublisher eventPublisher;

    public void updateFrequency(Family family, String itemName) {
        List<FrequentItem> items = frequentItemRepository.findByFamilyOrderByFrequencyDesc(family);
        FrequentItem existing = items.stream()
                .filter(i -> i.getItemName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setFrequency(existing.getFrequency() + 1);
            frequentItemRepository.save(existing);

            eventPublisher.send(family.getId(), EventType.FREQUENT_UPDATED, FrequentItemMapper.toDto(existing));
        } else {
            FrequentItem newItem = FrequentItem.builder()
                    .family(family)
                    .itemName(itemName)
                    .frequency(1)
                    .build();
            frequentItemRepository.save(newItem);
            eventPublisher.send(family.getId(), EventType.FREQUENT_UPDATED, FrequentItemMapper.toDto(newItem));
        }
    }

    public List<FrequentItemDto> getTopItems(Family family) {
        return frequentItemRepository.findByFamilyOrderByFrequencyDesc(family).stream()
                .map(FrequentItemMapper::toDto)
                .toList();
    }
}