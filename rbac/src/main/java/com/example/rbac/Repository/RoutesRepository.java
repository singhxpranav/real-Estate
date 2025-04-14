package com.example.rbac.Repository;


import com.example.rbac.Model.Routes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutesRepository extends JpaRepository<Routes, Long> {
    Routes findByPath(@Param("path") String path);
//    Routes findByPathEquals(String path);
//    // Repository method for finding all pattern routes
//    @Query("SELECT r FROM Routes r WHERE r.path LIKE '%/{id}%'")
//    List<Routes> findAllIdPatternRoutes();
//    List<Routes> findAllByPath(String path);
}
