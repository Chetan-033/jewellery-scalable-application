package com.jewellery.erp.platform.api;

import com.jewellery.erp.config.JewelleryProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final JewelleryProperties properties;

    public HealthController(JewelleryProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "jewellery-erp");
        body.put("edition", properties.getEdition());
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    @GetMapping("/modules")
    public Map<String, Object> modules() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("edition", properties.getEdition());
        body.put("enabled", properties.getModules().getEnabled());
        return body;
    }
}
