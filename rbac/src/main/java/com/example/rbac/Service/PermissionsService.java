package com.example.rbac.Service;

import com.example.rbac.Model.Permissions;
import com.example.rbac.Repository.PermissionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionsService {

    @Autowired
    private PermissionsRepository permissionsRepository;

    // ✅ Create a new routes
    @Transactional
    public Permissions createRoute(Permissions permissions) {
        return permissionsRepository.save(permissions);
    }

    // ✅ Retrieve all routes
    @Transactional(readOnly = true)
    public List<Permissions> getAllRoutes() {
        return permissionsRepository.findAll();
    }

    // ✅ Retrieve route by ID
    @Transactional(readOnly = true)
    public Permissions getRouteById(Long id) {
        return permissionsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Routes not found for this ID: " + id));
    }

    // ✅ Retrieve route by path
    @Transactional(readOnly = true)
    public Permissions getPermissionByName(String permission) {
        return permissionsRepository.findByPermissionEquals(permission);
    }

    // ✅ Update route
    @Transactional
    public Permissions updateRoute(Long id, Permissions permissionsDetails) {
        Permissions permissions = getRouteById(id); // Ensuring the routes exists
        permissions.setName(permissionsDetails.getName());
        permissions.setPermission(permissionsDetails.getPermission());
        return permissionsRepository.save(permissions);
    }

    // ✅ Delete route
    @Transactional
    public void deleteRoute(Long id) {
        Permissions permissions = getRouteById(id); // Ensuring the routes exists
        permissionsRepository.delete(permissions);
    }
}
