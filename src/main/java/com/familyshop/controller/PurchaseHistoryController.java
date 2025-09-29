package com.familyshop.controller;

import com.familyshop.dto.PurchaseHistoryDto;
import com.familyshop.mapper.PurchaseHistoryMapper;
import com.familyshop.model.Family;
import com.familyshop.service.FamilyService;
import com.familyshop.service.PurchaseHistoryService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class PurchaseHistoryController {

    private final PurchaseHistoryService purchaseHistoryService;
    private final FamilyService familyService;

    @GetMapping("{familyId}")
    public List<PurchaseHistoryDto> getHistory(@PathVariable Long familyId) {
        Family family = familyService.getFamilyById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));
        return purchaseHistoryService.getHistory(family).stream().map(PurchaseHistoryMapper::toDto).toList();
    }
}