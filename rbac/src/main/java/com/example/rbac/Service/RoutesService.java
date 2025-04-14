package com.example.rbac.Service;

import com.backend.karyanestApplication.Exception.CustomException;
import com.example.rbac.Model.Routes;
import com.example.rbac.Repository.RoutesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoutesService {

    @Autowired
    private RoutesRepository routeRepository;

    // ✅ Create a new routes
    @Transactional
    public Routes createRoute(Routes routes) {
        return routeRepository.save(routes);
    }

    // ✅ Retrieve all routes
    @Transactional(readOnly = true)
    public List<Routes> getAllRoutes() {
        return routeRepository.findAll();
    }

    // ✅ Retrieve route by ID
    @Transactional(readOnly = true)
    public Routes getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new CustomException("Routes not found for this ID: " + id));
    }

    // ✅ Retrieve route by path
    @Transactional(readOnly = true)
    public Routes getRouteByPath(String path) {
        return routeRepository.findByPath(path);
    }

    // ✅ Update route
    @Transactional
    public Routes updateRoute(Long id, Routes routesDetails) {
        Routes routes = getRouteById(id); // Ensuring the routes exists
        routes.setName(routesDetails.getName());
        routes.setPath(routesDetails.getPath());
        return routeRepository.save(routes);
    }

    // ✅ Delete route
    @Transactional
    public void deleteRoute(Long id) {
        Routes routes = getRouteById(id); // Ensuring the routes exists
        routeRepository.delete(routes);
    }
}
