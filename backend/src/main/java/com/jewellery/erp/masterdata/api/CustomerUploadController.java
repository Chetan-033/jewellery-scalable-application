package com.jewellery.erp.masterdata.api;

import com.jewellery.erp.loans.application.GirviDocumentStorageService;
import com.jewellery.erp.masterdata.application.CustomerDocumentStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/uploads")
public class CustomerUploadController {

    private final CustomerDocumentStorageService storageService;
    private final GirviDocumentStorageService girviStorageService;

    public CustomerUploadController(
            CustomerDocumentStorageService storageService,
            GirviDocumentStorageService girviStorageService) {
        this.storageService = storageService;
        this.girviStorageService = girviStorageService;
    }

    @GetMapping("/customers/{fileName:.+}")
    public ResponseEntity<Resource> getCustomerFile(@PathVariable String fileName) {
        return serveResource(storageService.loadFile(fileName));
    }

    @GetMapping("/girvi/{fileName:.+}")
    public ResponseEntity<Resource> getGirviFile(@PathVariable String fileName) {
        return serveResource(girviStorageService.loadFile(fileName));
    }

    private ResponseEntity<Resource> serveResource(Resource resource) {
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.IMAGE_JPEG;
        try {
            String probe = Files.probeContentType(resource.getFile().toPath());
            if (probe != null) {
                mediaType = MediaType.parseMediaType(probe);
            }
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}

