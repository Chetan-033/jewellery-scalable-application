package com.jewellery.erp.platform.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Placeholder status endpoints so every blueprint domain is addressable from day one.
 * Full implementations land in domain packages as vertical slices advance.
 */
@RestController
@RequestMapping("/api/v1/modules")
public class ModuleStubController {

    @GetMapping("/{moduleCode}/status")
    public Map<String, Object> status(@PathVariable String moduleCode) {
        return Map.of(
                "module", moduleCode,
                "status", "SCAFFOLDED",
                "message", "Domain package registered; implement via vertical slices"
        );
    }
}
