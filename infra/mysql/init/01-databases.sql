CREATE DATABASE IF NOT EXISTS jewellery_erp_platform
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS jewellery_erp_tenant_demo
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON jewellery_erp_platform.* TO 'erp_app'@'%';
GRANT ALL PRIVILEGES ON jewellery_erp_tenant_demo.* TO 'erp_app'@'%';
FLUSH PRIVILEGES;
