package com.jewellery.erp.identity.repository;

import com.jewellery.erp.identity.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.Pk> {
    List<UserRole> findByUserId(String userId);
}
