package com.example.rbac.Model;

import com.backend.karyanestApplication.Model.RolePermission;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Routes { //roles
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "path", nullable = false, unique = true)
    private String path;

    @Column(name ="permission")
    private String permission;


//    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @ToString.Exclude
//    private List<RolePermission> rolePermissions;
}
