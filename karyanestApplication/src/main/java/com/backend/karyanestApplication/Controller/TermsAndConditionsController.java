package com.backend.karyanestApplication.Controller;

import com.backend.karyanestApplication.DTO.TermsAndConditionsRequestDTO;
import com.backend.karyanestApplication.DTO.TermsAndConditionsResponseDTO;
import com.backend.karyanestApplication.Service.TermsAndConditionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/terms")
@RequiredArgsConstructor
public class TermsAndConditionsController {

    private final TermsAndConditionsService service;

    @PostMapping
    public ResponseEntity<TermsAndConditionsResponseDTO> createTerms(@RequestBody TermsAndConditionsRequestDTO request) {
        return new ResponseEntity<>(service.createTerms(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TermsAndConditionsResponseDTO>> getAllTerms() {
        return ResponseEntity.ok(service.getAllTerms());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TermsAndConditionsResponseDTO> updateTerms(@PathVariable Long id, @RequestBody TermsAndConditionsRequestDTO request) {
        return ResponseEntity.ok(service.updateTerms(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTerms(@PathVariable Long id) {
        service.deleteTerms(id);
        return ResponseEntity.noContent().build();
    }
}
