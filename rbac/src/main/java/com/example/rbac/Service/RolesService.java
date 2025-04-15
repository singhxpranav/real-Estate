package com.example.rbac.Service;

import com.backend.karyanestApplication.Exception.CustomException;
import com.example.rbac.Model.UserRoles;
import com.example.rbac.Repository.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RolesService {

    @Autowired
    private final RolesRepository roleRepository;

    public RolesService(RolesRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // ✅ Get all roles
    @Transactional(readOnly = true)
    public List<UserRoles> getAllRoles() {
        return roleRepository.findAll();
    }

    // ✅ Get role by ID
    @Transactional(readOnly = true)
    public UserRoles getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new CustomException("Role not found for this ID: " + id));
    }

    // ✅ Create a new role
    @Transactional
    public UserRoles createRole(UserRoles role) {
        return roleRepository.save(role);
    }


    // ✅ Delete role
    @Transactional
    public void deleteRole(Long id) {
        UserRoles role = getRoleById(id); // Ensuring the role exists before deleting
        roleRepository.delete(role);
    }

    public UserRoles getRoleByName(String roleName) {
        return roleRepository.findByName(roleName).orElseThrow(()->new CustomException("Role not found"));
    }

    public Optional<UserRoles> findByName(String roleName) {
        return roleRepository.findByName(roleName);
    }

}
