package com.jewellery.erp.shared.tenant;

public final class TenantContext {

    private static final ThreadLocal<TenantInfo> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(TenantInfo info) {
        CURRENT.set(info);
    }

    public static TenantInfo get() {
        return CURRENT.get();
    }

    public static String requireTenantId() {
        TenantInfo info = CURRENT.get();
        if (info == null || info.tenantId() == null) {
            throw new IllegalStateException("Tenant context is not set");
        }
        return info.tenantId();
    }

    public static void clear() {
        CURRENT.remove();
    }

    public record TenantInfo(String tenantId, String tenantCode, String edition) {
    }
}
