package com.jewellery.erp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "jewellery")
public class JewelleryProperties {

    private String edition = "offline";
    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final Bootstrap bootstrap = new Bootstrap();
    private final Modules modules = new Modules();

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public Cors getCors() {
        return cors;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public Modules getModules() {
        return modules;
    }

    public static class Jwt {
        private String secret;
        private long expiryMinutes = 480;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpiryMinutes() {
            return expiryMinutes;
        }

        public void setExpiryMinutes(long expiryMinutes) {
            this.expiryMinutes = expiryMinutes;
        }
    }

    public static class Cors {
        private String allowedOrigins = "http://localhost:4200";

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Bootstrap {
        private boolean enabled;
        private String adminUsername = "admin";
        private String adminPassword = "Admin@123";
        private String adminDisplayName = "Admin";
        private String tenantCode = "LOCAL";
        private String tenantName = "Local Shop";
        private String firmName = "Local Shop";
        private String branchName = "Main Branch";
        private String branchCode = "MAIN";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAdminUsername() {
            return adminUsername;
        }

        public void setAdminUsername(String adminUsername) {
            this.adminUsername = adminUsername;
        }

        public String getAdminPassword() {
            return adminPassword;
        }

        public void setAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
        }

        public String getAdminDisplayName() {
            return adminDisplayName;
        }

        public void setAdminDisplayName(String adminDisplayName) {
            this.adminDisplayName = adminDisplayName;
        }

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        public String getTenantName() {
            return tenantName;
        }

        public void setTenantName(String tenantName) {
            this.tenantName = tenantName;
        }

        public String getFirmName() {
            return firmName;
        }

        public void setFirmName(String firmName) {
            this.firmName = firmName;
        }

        public String getBranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }

        public String getBranchCode() {
            return branchCode;
        }

        public void setBranchCode(String branchCode) {
            this.branchCode = branchCode;
        }
    }

    public static class Modules {
        private List<String> enabled = new ArrayList<>();

        public List<String> getEnabled() {
            return enabled;
        }

        public void setEnabled(List<String> enabled) {
            this.enabled = enabled;
        }
    }
}
