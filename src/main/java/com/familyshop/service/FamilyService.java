package com.familyshop.service;

import com.familyshop.model.Family;
import com.familyshop.repository.FamilyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;

    public Family createFamily(Family family) {
        return familyRepository.save(family);
    }

    public Optional<Family> getFamilyById(Long id) {
        return familyRepository.findById(id);
    }
}