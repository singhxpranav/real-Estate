package com.example.rbac.DTO;

import lombok.Data;

@Data
public class AssignPermissionRequestDTO {
    private Long roleId;
    private String path;
}
