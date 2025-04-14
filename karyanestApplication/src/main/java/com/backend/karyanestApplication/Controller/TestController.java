package com.backend.karyanestApplication.Controller;

import com.backend.karyanestApplication.Model.Permission;
import com.backend.karyanestApplication.Model.Route;
import com.backend.karyanestApplication.Model.UserRole;
import com.backend.karyanestApplication.Service.AssignPermissionService;
import com.backend.karyanestApplication.Service.PermissionService;
import com.backend.karyanestApplication.Service.RoleService;
import com.backend.karyanestApplication.Service.RouteService;
import com.example.module_b.Service.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ExampleService exampleService;

    @Autowired
    private RoleService roleService;
    @Autowired
    private RouteService routeService;
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AssignPermissionService assignPermissionService;


    @GetMapping("/data")
    public String getData() {
        return exampleService.getServiceData();
    }

    @PostMapping("/create_routes")
    public ResponseEntity<Map<String, Object>> createRoutes(@RequestBody Route route) {
        Route createdRoute = routeService.createRoute(route);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Route Created Successfully", "route", createdRoute));
    }

    @PostMapping("/create_permits")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> createPermission(@RequestBody Map<String, Object> requestBody) {
        Route route = routeService.getRouteByPath((String) requestBody.get("path"));

        if (route == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Route not found"));
        }

        Permission permission = new Permission();
        permission.setName((String) requestBody.get("name"));
        permission.setDescription((String) requestBody.get("description"));
        permission.setRoute(route);

        Permission createdPermission = permissionService.createPermission(permission);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Permission Created Successfully", "permission", createdPermission));
    }

    @PostMapping("/assign_permission_to_role")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> assignPermissionToRole(@RequestBody Map<String, String> requestBody) {
        Long roleId = Long.valueOf(requestBody.get("roleId"));
        String path = requestBody.get("path");
        Long permissionId = Long.valueOf(requestBody.get("permission_id"));

        Route route = routeService.getRouteByPath(path);
        if (route == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User or Route not found"));
        }

        assignPermissionService.assignPermissionToRole(roleId, path, permissionId);

        return ResponseEntity.ok(Map.of("message", "Permission assigned to role successfully"));
    }

}
