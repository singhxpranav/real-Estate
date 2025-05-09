package com.backend.karyanestApplication.Service;

import com.backend.karyanestApplication.DTO.PropertyDTO;
import com.backend.karyanestApplication.DTO.PropertyFavoriteRequestDTO;
import com.backend.karyanestApplication.DTO.PropertyFavoriteResponseDTO;
import com.backend.karyanestApplication.Model.PropertyFavorite;
import com.backend.karyanestApplication.Model.User;
import com.backend.karyanestApplication.Model.Property;
import com.backend.karyanestApplication.Repositry.PropertyFavoriteRepository;
import com.backend.karyanestApplication.Repositry.UserRepo;
import com.backend.karyanestApplication.Repositry.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyFavoriteService {

    private final PropertyFavoriteRepository propertyFavoriteRepository;
    private final UserRepo userRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public PropertyFavoriteResponseDTO addFavorite(PropertyFavoriteRequestDTO request) {
        if (propertyFavoriteRepository.existsByUserIdAndPropertyId(request.getUserId(), request.getPropertyId())) {
            throw new IllegalStateException("Property is already in favorites");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        PropertyFavorite favorite = new PropertyFavorite();
        favorite.setUser(user);
        favorite.setProperty(property);
        PropertyFavorite savedFavorite = propertyFavoriteRepository.save(favorite);

        return convertToResponse(savedFavorite);
    }

    @Transactional(readOnly = true)
    public List<PropertyDTO> getUserFavorites(Long userId) {
        List<PropertyFavorite> favorites = propertyFavoriteRepository.findByUserId(userId);
        return favorites.stream().map(favorite -> new PropertyDTO(favorite.getProperty())).collect(Collectors.toList());
    }
    @Transactional
    public void removeFavorite(Long userId, Long propertyId) {
        int deletedCount = propertyFavoriteRepository.deleteByUserIdAndPropertyId(userId, propertyId);

        if (deletedCount > 0) {
            System.out.println("Property favorite deleted successfully.");
        } else {
            System.out.println("No matching favorite found for deletion.");
        }
    }

    private PropertyFavoriteResponseDTO convertToResponse(PropertyFavorite favorite) {
        return new PropertyFavoriteResponseDTO(
                favorite.getId(),
                favorite.getUser().getId(),
                favorite.getProperty().getId(),
                favorite.getCreatedAt()
        );
    }
}
