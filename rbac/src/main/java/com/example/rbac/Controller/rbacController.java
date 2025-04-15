package com.example.rbac.Controller;


import com.example.rbac.DTO.AssignPermissionRequestDTO;
import com.example.rbac.Model.Routes;
import com.example.rbac.Service.AssignPermissionService;
import com.example.rbac.Service.RoutesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rbac/test")
public class rbacController {


//    @Autowired
//    private RolesService roleService;
    @Autowired
    private RoutesService routeService;

    @Autowired
    private AssignPermissionService assignPermissionService;


    @PostMapping("/create_routes")
    public ResponseEntity<Map<String, Object>> createRoutes(@RequestBody Routes routes) {
        Routes createdRoutes = routeService.createRoute(routes);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Routes Created Successfully", "routes", createdRoutes));
    }

    @PostMapping("/assign_permission_to_role")
    public ResponseEntity<Map<String, Object>> assignPermissionToRole(@RequestBody AssignPermissionRequestDTO request) {
        System.out.println("Incoming roleId: " + request.getRoleId());
        System.out.println("Incoming path: " + request.getPath());

        Long roleId = request.getRoleId();
        String path = request.getPath();

        Routes routes = routeService.getRouteByPath(path);
        if (routes == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User or Routes not found"));
        }

        assignPermissionService.assignPermissionToRole(roleId, path);

        return ResponseEntity.ok(Map.of("message", "Permission assigned to role successfully"));
    }
}
