package com.jewellery.erp.platform.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final ObjectMapper objectMapper;
    private final Path settingsDir = Paths.get("data", "settings");

    public SettingsController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        try {
            Files.createDirectories(settingsDir);
        } catch (IOException e) {
            // Log directory creation warning
        }
    }

    @GetMapping("/forms/{formCode}")
    public ResponseEntity<Map<String, Object>> getFormSettings(@PathVariable String formCode) {
        Path filePath = settingsDir.resolve(formCode + ".json");
        if (Files.exists(filePath)) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = objectMapper.readValue(filePath.toFile(), Map.class);
                return ResponseEntity.ok(data);
            } catch (IOException ignored) {
            }
        }
        return ResponseEntity.ok(getDefaultSettings(formCode));
    }

    @PutMapping("/forms/{formCode}")
    public ResponseEntity<Map<String, Object>> saveFormSettings(
            @PathVariable String formCode,
            @RequestBody Map<String, Object> payload) {
        try {
            Files.createDirectories(settingsDir);
            Path filePath = settingsDir.resolve(formCode + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), payload);
            return ResponseEntity.ok(payload);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/forms/{formCode}/reset")
    public ResponseEntity<Map<String, Object>> resetFormSettings(@PathVariable String formCode) {
        Path filePath = settingsDir.resolve(formCode + ".json");
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
        Map<String, Object> defaults = getDefaultSettings(formCode);
        return ResponseEntity.ok(defaults);
    }

    private Map<String, Object> getDefaultSettings(String formCode) {
        Map<String, Object> map = new LinkedHashMap<>();
        if ("loan-notice".equalsIgnoreCase(formCode)) {
            map.put("noticeViewingLanguage", "English");
            map.put("defaultLanguage", "English");
            map.put("paperSize", "A5 Size");
            map.put("documentWidth", 135);
            map.put("documentHeight", 195);
            map.put("topMargin", 10);
            map.put("leftMargin", 15);
            map.put("rightMargin", 15);
            map.put("bottomMargin", 10);
            map.put("formBorder", true);
            map.put("borderStyle", "SOLID");
            map.put("headerSectionHeight", 25);
            map.put("displayHeader", true);
            map.put("formLeftLogo", true);
            map.put("formRightLogo", true);

            map.put("headerLeftText", "श्री गणेशाय नमः");
            map.put("headerLeftFontSize", 14);
            map.put("headerLeftColor", "Black");
            map.put("headerLeftVisible", true);

            map.put("headerRightText", "|| ॐ नमः शिवाय ||");
            map.put("headerRightFontSize", 14);
            map.put("headerRightColor", "Black");
            map.put("headerRightVisible", true);

            map.put("headerLabelText", "GIRVI / LOAN NOTICE");
            map.put("headerLabelFontSize", 18);
            map.put("headerLabelColor", "Black");
            map.put("headerLabelVisible", true);

            map.put("formHeaderFontSize", 20);
            map.put("formHeaderColor", "Black");
            map.put("formHeaderVisible", true);

            map.put("firmName", "Local Jewellery Shop");
            map.put("firmNameFontSize", 16);
            map.put("firmNameColor", "Black");
            map.put("firmNameVisible", true);

            map.put("firmDes", "Certified Gold & Silver Jewellery & Girvi Merchant");
            map.put("firmDesFontSize", 14);
            map.put("firmDesColor", "Black");
            map.put("firmDesVisible", true);

            map.put("firmAddress", "Shop #4, Main Market, Gold Bazaar, Kolkata - 700001");
            map.put("firmAddressFontSize", 12);
            map.put("firmAddressColor", "Black");
            map.put("firmAddressVisible", true);

            map.put("phone", "+91 98301 23456 / 033-22114455");
            map.put("phoneFontSize", 12);
            map.put("phoneColor", "Black");
            map.put("phoneVisible", true);

            map.put("email", "support@localjewellery.com");
            map.put("emailFontSize", 12);
            map.put("emailColor", "Black");
            map.put("emailVisible", true);

            map.put("licenseNo", "WB-GIRVI-2026/8941");
            map.put("warningPeriodDays", 15);
            map.put("noticeSubject", "FINAL NOTICE FOR REPAYMENT & SETTLEMENT OF PLEDGED ORNAMENTS");
            map.put("noticeBody", "This is to bring to your kind attention that the loan advance availed by you against the pledge of gold/silver ornaments detailed below is currently overdue and interest charges have accumulated. You are hereby requested to settle the outstanding dues and release your ornaments within the stipulated period.");
            map.put("signatoryTitle", "For Local Jewellery Shop (Authorized Signatory / Prop.)");
        } else if ("money-deposit".equalsIgnoreCase(formCode)) {
            map.put("formTitle", "MONEY DEPOSIT RECEIPT / VOUCHER");
            map.put("paperSize", "A5 Size");
            map.put("formBorder", true);
            map.put("displayHeader", true);
            map.put("firmName", "Local Jewellery Shop");
            map.put("voucherPrefix", "MDEP-");
        } else if ("form-n8".equalsIgnoreCase(formCode)) {
            map.put("formTitle", "FORM N / FORM 8 - PAWN REGISTER");
            map.put("paperSize", "A4 Size");
            map.put("formBorder", true);
            map.put("statutoryRule", "Under Pawn Brokers Act Section 7 & Rule 8");
        } else if ("form-r7".equalsIgnoreCase(formCode)) {
            map.put("formTitle", "FORM R / FORM 7 - RELEASE CERTIFICATE");
            map.put("paperSize", "A5 Size");
            map.put("formBorder", true);
        } else if ("udhaar-notice".equalsIgnoreCase(formCode)) {
            map.put("formTitle", "CREDIT / UDHAAR OUTSTANDING NOTICE");
            map.put("paperSize", "A5 Size");
            map.put("formBorder", true);
            map.put("warningPeriodDays", 7);
        } else {
            map.put("formCode", formCode);
            map.put("customLayout", true);
            map.put("paperSize", "A4 Size");
        }
        return map;
    }
}
