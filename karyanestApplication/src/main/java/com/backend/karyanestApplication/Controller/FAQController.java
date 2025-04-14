package com.backend.karyanestApplication.Controller;

import com.backend.karyanestApplication.DTO.FAQRequestDTO;
import com.backend.karyanestApplication.DTO.FAQResponseDTO;
import com.backend.karyanestApplication.Service.FAQService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/faqs")
@RequiredArgsConstructor
public class FAQController {

    private final FAQService faqService;

    @PostMapping
    public ResponseEntity<FAQResponseDTO> create(@RequestBody FAQRequestDTO requestDTO) {
        return new ResponseEntity<>(faqService.createFAQ(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FAQResponseDTO>> getAll() {
        return ResponseEntity.ok(faqService.getAllFAQs());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FAQResponseDTO> update(@PathVariable Long id, @RequestBody FAQRequestDTO requestDTO) {
        return ResponseEntity.ok(faqService.updateFAQ(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        faqService.deleteFAQ(id);
        return ResponseEntity.noContent().build();
    }
}
