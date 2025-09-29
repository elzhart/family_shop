package com.familyshop.controller;

import com.familyshop.dto.FamilyDto;
import com.familyshop.mapper.FamilyMapper;
import com.familyshop.model.Family;
import com.familyshop.service.FamilyService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping
    public FamilyDto createFamily(@RequestBody Family name) {
        return FamilyMapper.toFamilyDto(familyService.createFamily(name));
    }

    @GetMapping("/{id}")
    public FamilyDto getFamily(@PathVariable Long id) {
        return familyService.getFamilyById(id).map(FamilyMapper::toFamilyDto)
                .orElseThrow(() -> new IllegalArgumentException("Family not found"));
    }
}