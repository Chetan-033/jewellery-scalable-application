package com.jewellery.erp.operations.bootstrap;

import com.jewellery.erp.config.JewelleryProperties;
import com.jewellery.erp.identity.domain.AppUser;
import com.jewellery.erp.identity.domain.Permission;
import com.jewellery.erp.identity.domain.Role;
import com.jewellery.erp.identity.domain.RolePermission;
import com.jewellery.erp.identity.domain.UserRole;
import com.jewellery.erp.identity.repository.AppUserRepository;
import com.jewellery.erp.identity.repository.PermissionRepository;
import com.jewellery.erp.identity.repository.RolePermissionRepository;
import com.jewellery.erp.identity.repository.RoleRepository;
import com.jewellery.erp.identity.repository.UserRoleRepository;
import com.jewellery.erp.organization.domain.Branch;
import com.jewellery.erp.organization.domain.BusinessFirm;
import com.jewellery.erp.organization.domain.TenantMeta;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.organization.repository.BusinessFirmRepository;
import com.jewellery.erp.organization.repository.TenantMetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class OfflineBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OfflineBootstrapRunner.class);

    private final JewelleryProperties properties;
    private final TenantMetaRepository tenantMetaRepository;
    private final BusinessFirmRepository firmRepository;
    private final BranchRepository branchRepository;
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    public OfflineBootstrapRunner(
            JewelleryProperties properties,
            TenantMetaRepository tenantMetaRepository,
            BusinessFirmRepository firmRepository,
            BranchRepository branchRepository,
            AppUserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.tenantMetaRepository = tenantMetaRepository;
        this.firmRepository = firmRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.getBootstrap().isEnabled()) {
            return;
        }
        if (tenantMetaRepository.existsByCode(properties.getBootstrap().getTenantCode())) {
            log.info("Offline bootstrap skipped — tenant already present");
            return;
        }

        JewelleryProperties.Bootstrap cfg = properties.getBootstrap();

        TenantMeta tenant = new TenantMeta();
        tenant.setId(UUID.randomUUID().toString());
        tenant.setCode(cfg.getTenantCode());
        tenant.setName(cfg.getTenantName());
        tenant.setEdition(properties.getEdition().toUpperCase());
        tenantMetaRepository.save(tenant);

        BusinessFirm firm = new BusinessFirm();
        firm.setId(UUID.randomUUID().toString());
        firm.setCode("FIRM1");
        firm.setLegalName(cfg.getFirmName());
        firm.setDisplayName(cfg.getFirmName());
        firmRepository.save(firm);

        Branch branch = new Branch();
        branch.setId(UUID.randomUUID().toString());
        branch.setFirmId(firm.getId());
        branch.setCode(cfg.getBranchCode());
        branch.setName(cfg.getBranchName());
        branchRepository.save(branch);

        Role adminRole = ensureRole("ADMIN", "Administrator");
        List<String> permissionCodes = List.of(
                "CUSTOMER_READ", "CUSTOMER_WRITE",
                "METAL_READ", "METAL_WRITE",
                "RATE_READ", "RATE_WRITE",
                "STOCK_READ", "STOCK_WRITE",
                "ORG_READ"
        );
        for (String code : permissionCodes) {
            Permission permission = ensurePermission(code);
            RolePermission rp = new RolePermission();
            rp.setRoleId(adminRole.getId());
            rp.setPermissionId(permission.getId());
            rolePermissionRepository.save(rp);
        }

        AppUser admin = new AppUser();
        admin.setId(UUID.randomUUID().toString());
        admin.setUsername(cfg.getAdminUsername());
        admin.setDisplayName(cfg.getAdminDisplayName());
        admin.setPasswordHash(passwordEncoder.encode(cfg.getAdminPassword()));
        userRepository.save(admin);

        UserRole userRole = new UserRole();
        userRole.setUserId(admin.getId());
        userRole.setRoleId(adminRole.getId());
        userRoleRepository.save(userRole);

        log.info("Offline bootstrap complete. Login as '{}' / configured password", cfg.getAdminUsername());
    }

    private Role ensureRole(String code, String name) {
        return roleRepository.findByCode(code).orElseGet(() -> {
            Role role = new Role();
            role.setId(UUID.randomUUID().toString());
            role.setCode(code);
            role.setName(name);
            return roleRepository.save(role);
        });
    }

    private Permission ensurePermission(String code) {
        return permissionRepository.findByCode(code).orElseGet(() -> {
            Permission permission = new Permission();
            permission.setId(UUID.randomUUID().toString());
            permission.setCode(code);
            permission.setDescription(code);
            return permissionRepository.save(permission);
        });
    }
}
