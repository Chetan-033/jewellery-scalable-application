package com.jewellery.erp.loans.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GirviDocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(GirviDocumentStorageService.class);
    private static final Path ROOT_DIR = Paths.get("data", "uploads", "girvi");
    private static final Pattern DATA_URL_PATTERN = Pattern.compile("^data:image/([a-zA-Z0-9]+);base64,(.*)$");

    public GirviDocumentStorageService() {
        try {
            if (!Files.exists(ROOT_DIR)) {
                Files.createDirectories(ROOT_DIR);
            }
        } catch (IOException e) {
            log.error("Failed to initialize girvi uploads directory: {}", ROOT_DIR, e);
        }
    }

    /**
     * If input is a Base64 data URL, decodes and saves it to disk, returning the relative HTTP URL.
     * If input is already a URL or path, returns it unchanged.
     */
    public String saveIfBase64(String baseName, String dataUrlOrPath) {
        if (dataUrlOrPath == null || dataUrlOrPath.isBlank()) {
            return null;
        }

        // If it's already a saved URL/path, keep it
        if (!dataUrlOrPath.startsWith("data:")) {
            return dataUrlOrPath;
        }

        Matcher matcher = DATA_URL_PATTERN.matcher(dataUrlOrPath.trim());
        String extension = "jpg";
        String base64Content = dataUrlOrPath;

        if (matcher.matches()) {
            String mimeSub = matcher.group(1).toLowerCase();
            extension = switch (mimeSub) {
                case "png" -> "png";
                case "webp" -> "webp";
                case "gif" -> "gif";
                default -> "jpg";
            };
            base64Content = matcher.group(2);
        } else {
            int commaIdx = dataUrlOrPath.indexOf(',');
            if (commaIdx >= 0) {
                base64Content = dataUrlOrPath.substring(commaIdx + 1);
            }
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(base64Content.trim());
            String fileName = baseName + "." + extension;
            Path targetFile = ROOT_DIR.resolve(fileName);

            Files.write(targetFile, bytes);
            log.info("Saved girvi item image to disk: {} ({} bytes)", targetFile, bytes.length);

            return "/api/v1/uploads/girvi/" + fileName;
        } catch (Exception e) {
            log.error("Failed to save girvi base64 image for {}", baseName, e);
            return dataUrlOrPath;
        }
    }

    /**
     * Resolves a stored file as a Spring Resource for serving.
     */
    public Resource loadFile(String fileName) {
        Path filePath = ROOT_DIR.resolve(fileName).normalize();
        if (!filePath.startsWith(ROOT_DIR)) {
            throw new IllegalArgumentException("Invalid file path: " + fileName);
        }
        if (!Files.exists(filePath)) {
            return null;
        }
        return new FileSystemResource(filePath);
    }
}

