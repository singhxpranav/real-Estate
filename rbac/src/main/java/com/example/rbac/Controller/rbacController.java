package com.example.rbac.Controller;

import com.backend.karyanestApplication.Service.AssignPermissionService;
//import com.backend.karyanestApplication.Service.PermissionService;
//import com.backend.karyanestApplication.Service.RoleService;
import com.example.module_b.Service.ExampleService;
import com.example.rbac.Model.Routes;
//import com.example.rbac.Service.RolesService;
import com.example.rbac.Service.RoutesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class rbacController {


//    @Autowired
//    private RolesService roleService;
    @Autowired
    private RoutesService routeService;

//    @Autowired
//    private AssignPermissionService assignPermissionService;


    @PostMapping("/create_routes")
    public ResponseEntity<Map<String, Object>> createRoutes(@RequestBody Routes routes) {
        Routes createdRoutes = routeService.createRoute(routes);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Routes Created Successfully", "routes", createdRoutes));
    }

//    @PostMapping("/create_permits")
////    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<Map<String, Object>> createPermission(@RequestBody Map<String, Object> requestBody) {
//        Routes route = routeService.getRouteByPath((String) requestBody.get("path"));
//
//        if (route == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "Routes not found"));
//        }
//
//        Permission permission = new Permission();
//        permission.setName((String) requestBody.get("name"));
//        permission.setDescription((String) requestBody.get("description"));
//        permission.setRoute(route);
//
//        Permission createdPermission = permissionService.createPermission(permission);
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(Map.of("message", "Permission Created Successfully", "permission", createdPermission));
//    }

//    @PostMapping("/assign_permission_to_role")
////    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<Map<String, Object>> assignPermissionToRole(@RequestBody Map<String, String> requestBody) {
//        Long roleId = Long.valueOf(requestBody.get("roleId"));
//        String path = requestBody.get("path");
//        Long permissionId = Long.valueOf(requestBody.get("permission_id"));
//
//        Routes routes = routeService.getRouteByPath(path);
//        if (routes == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "User or Routes not found"));
//        }
//
//        assignPermissionService.assignPermissionToRole(roleId, path, permissionId);
//
//        return ResponseEntity.ok(Map.of("message", "Permission assigned to role successfully"));
//    }
}
