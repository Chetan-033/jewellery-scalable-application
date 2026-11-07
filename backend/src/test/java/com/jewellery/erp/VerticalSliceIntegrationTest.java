package com.jewellery.erp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VerticalSliceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginCreateMastersRateAndStockMovement() throws Exception {
        String token = login();

        MvcResult branchResult = mockMvc.perform(get("/api/v1/organization/branches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("MAIN"))
                .andReturn();
        String branchId = objectMapper.readTree(branchResult.getResponse().getContentAsString()).get(0).get("id").asText();

        mockMvc.perform(post("/api/v1/masters/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"C001","name":"Walk-in Customer","phone":"9999999999"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("C001"));

        // Use seeded GOLD / 22K from MasterDataSeedRunner (bootstrap.enabled=true in test profile)
        String metalId = findMetalId(token, "GOLD");
        String purityId = findPurityId(token, metalId, "22K");

        mockMvc.perform(post("/api/v1/rates/metal")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"metalId":"%s","purityId":"%s","branchId":"%s","ratePerGram":6250.5000}
                                """.formatted(metalId, purityId, branchId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratePerGram").value(6250.5));

        MvcResult itemResult = mockMvc.perform(post("/api/v1/inventory/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"RAW-GOLD-22K","name":"Raw Gold 22K","metalId":"%s","purityId":"%s"}
                                """.formatted(metalId, purityId)))
                .andExpect(status().isOk())
                .andReturn();
        String itemId = objectMapper.readTree(itemResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult movementResult = mockMvc.perform(post("/api/v1/inventory/movements")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stockItemId":"%s",
                                  "branchId":"%s",
                                  "movementType":"IN",
                                  "quantity":1,
                                  "grossWeight":10.0000,
                                  "netWeight":10.0000,
                                  "sourceType":"OPENING",
                                  "sourceId":"OPEN-1",
                                  "remarks":"Opening stock"
                                }
                                """.formatted(itemId, branchId)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode movement = objectMapper.readTree(movementResult.getResponse().getContentAsString());
        assertEquals(0, new BigDecimal("9.1600").compareTo(movement.get("fineWeight").decimalValue()));
        assertEquals(0, new BigDecimal("9.1600").compareTo(
                movement.get("resultingBalance").get("fineWeight").decimalValue()));
    }

    @Test
    void addStockWithPacketsCrystalsAndSettingsDrivenValuation() throws Exception {
        String token = login();

        MvcResult branchResult = mockMvc.perform(get("/api/v1/organization/branches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String branchId = objectMapper.readTree(branchResult.getResponse().getContentAsString()).get(0).get("id").asText();

        MvcResult metalResult = mockMvc.perform(post("/api/v1/masters/metals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"GOLD2","name":"Gold","symbol":"Au"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String metalId = objectMapper.readTree(metalResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult purityResult = mockMvc.perform(post("/api/v1/masters/purities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"metalId":"%s","label":"22K-AS","fineness":91.6000}
                                """.formatted(metalId)))
                .andExpect(status().isOk())
                .andReturn();
        String purityId = objectMapper.readTree(purityResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/calc/settings/stock-add")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockValBy").value("byFFineWt"));

        MvcResult previewResult = mockMvc.perform(post("/api/v1/inventory/add-stock/preview")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity":1,
                                  "grossWeight":10.0000,
                                  "pktWeight":0.5000,
                                  "lessWeight":0.5000,
                                  "purityPercent":91.6000,
                                  "wastagePercent":1.0000,
                                  "metalRate":6000.0000,
                                  "labCharges":50.0000,
                                  "labChargesType":"GM",
                                  "makingCharges":100.0000,
                                  "makingChargesType":"GM",
                                  "crystals":[{"name":"CZ","quantity":1,"weight":0.3000,"rate":200,"rateType":"PP","autoLess":true}]
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode preview = objectMapper.readTree(previewResult.getResponse().getContentAsString());
        // net = 10 - 0.5 - 0.5 - 0.3 cry = 8.7
        assertEquals(0, new BigDecimal("8.7000").compareTo(preview.get("netWeight").decimalValue()));

        MvcResult entryResult = mockMvc.perform(post("/api/v1/inventory/add-stock")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "branchId":"%s",
                                  "itemName":"Gold Chain 22K",
                                  "itemCategory":"Chain",
                                  "stockType":"RETAIL",
                                  "metalId":"%s",
                                  "purityId":"%s",
                                  "barcode":"BC9001",
                                  "rfidNo":"RFID9001",
                                  "quantity":1,
                                  "grossWeight":10.0000,
                                  "pktWeight":0.5000,
                                  "lessWeight":0.5000,
                                  "purityPercent":91.6000,
                                  "wastagePercent":1.0000,
                                  "metalRate":6000.0000,
                                  "labCharges":50.0000,
                                  "labChargesType":"GM",
                                  "makingCharges":100.0000,
                                  "makingChargesType":"GM",
                                  "packets":[{"description":"Box","quantity":1,"weight":0.2000,"operation":"LESS"}],
                                  "crystals":[{"name":"CZ","category":"Stone","quantity":1,"weight":0.3000,"rate":200,"rateType":"PP","autoLess":true}]
                                }
                                """.formatted(branchId, metalId, purityId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryNo").isNotEmpty())
                .andExpect(jsonPath("$.barcode").value("BC9001"))
                .andExpect(jsonPath("$.packets.length()").value(1))
                .andExpect(jsonPath("$.crystals.length()").value(1))
                .andReturn();

        JsonNode entry = objectMapper.readTree(entryResult.getResponse().getContentAsString());
        // less includes packet row 0.2 → net = 10 - 0.5 - (0.5+0.2) - 0.3 = 8.5
        assertEquals(0, new BigDecimal("8.5000").compareTo(entry.get("netWeight").decimalValue()));
    }

    private String findMetalId(String token, String code) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/masters/metals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode metals = objectMapper.readTree(result.getResponse().getContentAsString());
        assertFalse(metals.isEmpty(), "Expected seeded metals");
        for (JsonNode metal : metals) {
            if (code.equalsIgnoreCase(metal.get("code").asText())) {
                return metal.get("id").asText();
            }
        }
        throw new AssertionError("Metal not found: " + code);
    }

    private String findPurityId(String token, String metalId, String label) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/masters/purities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode purities = objectMapper.readTree(result.getResponse().getContentAsString());
        assertFalse(purities.isEmpty(), "Expected seeded purities");
        for (JsonNode purity : purities) {
            if (metalId.equals(purity.get("metalId").asText())
                    && label.equalsIgnoreCase(purity.get("label").asText())) {
                return purity.get("id").asText();
            }
        }
        throw new AssertionError("Purity not found: " + label);
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"Admin@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();
        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
        assertNotNull(token);
        return token;
    }
}
