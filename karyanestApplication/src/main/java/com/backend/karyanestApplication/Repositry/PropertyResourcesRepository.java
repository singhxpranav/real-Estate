package com.backend.karyanestApplication.Repositry;

import com.backend.karyanestApplication.Model.PropertyResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyResourcesRepository extends JpaRepository<PropertyResource, Long> {

    // ✅ Fetch all resources for a specific property
    List<PropertyResource> findByPropertyId(Long propertyId);
    void deleteByPropertyId(Long id);
}
