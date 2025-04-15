package com.example.rbac.Service;

import com.backend.karyanestApplication.Exception.CustomException;
import com.example.rbac.Model.RolesPermission;
import com.example.rbac.Model.Routes;
import com.example.rbac.Model.UserRoles;
import com.example.rbac.Repository.RolesPermissionRepository;
import com.example.rbac.Repository.RoutesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignPermissionService {
    @Autowired
    private RoutesRepository routeRepository;

    @Autowired
    private RolesService roleService;

    @Autowired
    private RolesPermissionRepository rolesPermissionRepository;

//    /**
//     * Validates if the permission ID is valid for the given route
//     * @param routeId the route ID
//     * @param permissionId the permission ID to validate
//     * @throws CustomException if the permission is invalid for the route
//     */
//    private void validateRoutePermissionMapping(Long routeId, Long permissionId) {
//        Permission validPermission = permissionRepository.findByRouteId(routeId);
//
//        if (validPermission == null) {
//            throw new CustomException("No permission defined for route ID: " + routeId);
//        }
//
//        if (!validPermission.getId().equals(permissionId)) {
//            throw new CustomException("Invalid permission ID: " + permissionId +
//                    " for route ID: " + routeId +
//                    ". Expected permission ID: " + validPermission.getId());
//        }
//    }

    /**
     * Assigns a permission to a role for a specific route
     * @param roleId the role I'd of the role (e.g., "3->ROLE_ADMIN", "2->ROLE_USER")
     * @param path the route path
     */
    @Transactional
    public void assignPermissionToRole(Long roleId, String path) {

        System.out.println("Service received roleId: " + roleId);
        System.out.println("Service received path: " + path);
        // Find the role by ID
        UserRoles role = roleService.getRoleById(roleId);
        if (role == null) {
            throw new CustomException("Role not found with this ID: " + roleId);
        }

        // Find the route by path
        Routes route = routeRepository.findByPathEquals(path);
        if (route == null) {
            throw new CustomException("Route not found for path: " + path);
        }


        // Fetch the RolesPermission entity directly
        RolesPermission existingRolePermission = rolesPermissionRepository
                .findFirstByRoleAndRoutes(role, route);

        if (existingRolePermission != null) {
            System.out.println("✅ Role is already assigned to the route.");
            return;
        }

        // Create a new RolesPermission object and set its attributes
        RolesPermission rolePermission = new RolesPermission();
        rolePermission.setRole(role);
        rolePermission.setRoutes(route);

        // Save the RolesPermission object to the database
        rolesPermissionRepository.save(rolePermission);

        System.out.println("✅ Successfully assigned role " + role.getName() + " to route " + path);

    }
}
