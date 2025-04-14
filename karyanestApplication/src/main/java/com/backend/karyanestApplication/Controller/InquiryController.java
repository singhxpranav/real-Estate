package com.backend.karyanestApplication.Controller;

import com.backend.karyanestApplication.DTO.InquiryRequestDTO;
import com.backend.karyanestApplication.DTO.InquiryResponseDTO;
import com.backend.karyanestApplication.Service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<InquiryResponseDTO> createInquiry(@RequestBody InquiryRequestDTO requestDTO) {
        InquiryResponseDTO response = inquiryService.createInquiry(requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<InquiryResponseDTO>> getAllInquiries() {
        return ResponseEntity.ok(inquiryService.getAllInquiries());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<InquiryResponseDTO>> getInquiriesByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(inquiryService.getInquiriesByUserId(id));
    }

    @GetMapping("/property/{id}")
    public ResponseEntity<List<InquiryResponseDTO>> getInquiriesByPropertyId(@PathVariable Long id) {
        return ResponseEntity.ok(inquiryService.getInquiriesByPropertyId(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return ResponseEntity.ok("Inquiry deleted successfully");
    }
}
