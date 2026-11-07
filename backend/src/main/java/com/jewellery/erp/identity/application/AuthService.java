package com.jewellery.erp.identity.application;

import com.jewellery.erp.config.JewelleryProperties;
import com.jewellery.erp.identity.api.AuthDtos;
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
import com.jewellery.erp.identity.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JewelleryProperties properties;

    public AuthService(
            AppUserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JewelleryProperties properties) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!"ACTIVE".equals(user.getStatus()) || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        List<String> roles = new ArrayList<>();
        Set<String> permissions = new LinkedHashSet<>();
        for (UserRole ur : userRoleRepository.findByUserId(user.getId())) {
            Role role = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (role != null) {
                roles.add(role.getCode());
                for (RolePermission rp : rolePermissionRepository.findByRoleId(role.getId())) {
                    permissionRepository.findById(rp.getPermissionId())
                            .map(Permission::getCode)
                            .ifPresent(permissions::add);
                }
            }
        }
        List<String> permissionList = List.copyOf(permissions);
        String token = jwtService.createToken(user.getId(), user.getUsername(), roles, permissionList);
        return new AuthDtos.LoginResponse(
                token,
                "Bearer",
                properties.getJwt().getExpiryMinutes(),
                toUserInfo(user, roles, permissionList)
        );
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserInfo currentUser(String userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        List<String> roles = new ArrayList<>();
        Set<String> permissions = new LinkedHashSet<>();
        for (UserRole ur : userRoleRepository.findByUserId(user.getId())) {
            Role role = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (role != null) {
                roles.add(role.getCode());
                for (RolePermission rp : rolePermissionRepository.findByRoleId(role.getId())) {
                    permissionRepository.findById(rp.getPermissionId())
                            .map(Permission::getCode)
                            .ifPresent(permissions::add);
                }
            }
        }
        return toUserInfo(user, roles, List.copyOf(permissions));
    }

    private AuthDtos.UserInfo toUserInfo(AppUser user, List<String> roles, List<String> permissions) {
        return new AuthDtos.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                roles,
                permissions
        );
    }
}
