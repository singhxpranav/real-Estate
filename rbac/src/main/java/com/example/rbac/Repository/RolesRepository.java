package com.example.rbac.Repository;


import com.example.rbac.Model.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<UserRoles, Long> {
    Optional<UserRoles> findByName(String roleUser);
}
