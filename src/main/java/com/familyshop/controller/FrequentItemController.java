package com.familyshop.controller;

import com.familyshop.dto.FrequentItemDto;
import com.familyshop.model.Family;
import com.familyshop.service.FamilyService;
import com.familyshop.service.FrequentItemService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/frequent-items")
@RequiredArgsConstructor
public class FrequentItemController {

    private final FrequentItemService frequentItemService;
    private final FamilyService familyService;

    @GetMapping("{familyId}")
    public List<FrequentItemDto> getFrequentItems(@PathVariable Long familyId) {
        Family family = familyService.getFamilyById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));
        return frequentItemService.getTopItems(family);
    }
}