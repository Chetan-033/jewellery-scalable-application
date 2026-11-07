package com.jewellery.erp.identity.repository;

import com.jewellery.erp.identity.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermission.Pk> {
    List<RolePermission> findByRoleId(String roleId);
}
