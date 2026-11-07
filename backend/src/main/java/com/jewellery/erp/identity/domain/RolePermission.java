package com.jewellery.erp.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "role_permission")
@IdClass(RolePermission.Pk.class)
public class RolePermission {

    @Id
    @Column(name = "role_id", length = 36)
    private String roleId;

    @Id
    @Column(name = "permission_id", length = 36)
    private String permissionId;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public static class Pk implements Serializable {
        private String roleId;
        private String permissionId;

        public Pk() {
        }

        public Pk(String roleId, String permissionId) {
            this.roleId = roleId;
            this.permissionId = permissionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pk pk)) {
                return false;
            }
            return Objects.equals(roleId, pk.roleId) && Objects.equals(permissionId, pk.permissionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, permissionId);
        }
    }
}
