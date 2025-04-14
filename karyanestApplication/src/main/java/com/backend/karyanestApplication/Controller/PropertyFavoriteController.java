package com.backend.karyanestApplication.Controller;

import com.backend.karyanestApplication.DTO.PropertyDTO;
import com.backend.karyanestApplication.DTO.PropertyFavoriteRequestDTO;
import com.backend.karyanestApplication.DTO.PropertyFavoriteResponseDTO;
import com.backend.karyanestApplication.Service.PropertyFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/favorites")
@RequiredArgsConstructor
public class PropertyFavoriteController {

    private final PropertyFavoriteService propertyFavoriteService;

    @PostMapping
    public ResponseEntity<PropertyFavoriteResponseDTO> addFavorite(@RequestBody PropertyFavoriteRequestDTO request) {
        return new ResponseEntity<>(propertyFavoriteService.addFavorite(request), HttpStatus.CREATED);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<PropertyDTO>> getUserFavorites(@PathVariable Long id) {
        return ResponseEntity.ok(propertyFavoriteService.getUserFavorites(id));
    }

    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(PropertyFavoriteRequestDTO request) {
        propertyFavoriteService.removeFavorite(request.getUserId(), request.getPropertyId());
        return ResponseEntity.noContent().build();
    }
}
