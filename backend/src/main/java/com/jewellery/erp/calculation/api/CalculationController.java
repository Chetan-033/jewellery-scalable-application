package com.jewellery.erp.calculation.api;

import com.jewellery.erp.calculation.MetalLineInput;
import com.jewellery.erp.calculation.MetalLineResult;
import com.jewellery.erp.calculation.MetalValuationEngine;
import com.jewellery.erp.calculation.StockAddSettings;
import com.jewellery.erp.calculation.application.CalcSettingService;
import com.jewellery.erp.calculation.domain.CalcSetting;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/calc")
public class CalculationController {

    private final MetalValuationEngine engine;
    private final CalcSettingService calcSettingService;

    public CalculationController(MetalValuationEngine engine, CalcSettingService calcSettingService) {
        this.engine = engine;
        this.calcSettingService = calcSettingService;
    }

    @PostMapping("/metal-line")
    public MetalLineResult preview(@Valid @RequestBody PreviewRequest request) {
        return engine.calculate(new MetalLineInput(
                request.quantity(),
                request.grossWeight(),
                request.stoneWeight(),
                request.netWeight(),
                request.purityPercent(),
                request.wastagePercent(),
                request.ratePerGram(),
                request.makingType(),
                request.makingRate(),
                request.stoneAmount(),
                request.discountAmount(),
                request.cgstPercent(),
                request.sgstPercent(),
                request.igstPercent(),
                request.custWastagePercent(),
                request.sellCustWstgMode() != null && !request.sellCustWstgMode().isBlank()
                        ? request.sellCustWstgMode()
                        : calcSettingService.get("sellCustWstgMode", MetalValuationEngine.MODE_ADD_TO_NET_PCT),
                request.valueAdded()
        ));
    }

    @GetMapping("/settings")
    public Map<String, String> settings() {
        return calcSettingService.allAsMap();
    }

    @GetMapping("/settings/stock-add")
    public StockAddSettings stockAddSettings() {
        return calcSettingService.stockAddSettings();
    }

    @PutMapping("/settings")
    public CalcSetting updateSetting(@Valid @RequestBody UpdateSettingRequest request) {
        return calcSettingService.requireUpdate(request.key(), request.value());
    }

    @GetMapping("/settings/rows")
    public List<CalcSetting> settingRows() {
        return calcSettingService.list();
    }

    public record PreviewRequest(
            BigDecimal quantity,
            @NotNull BigDecimal grossWeight,
            BigDecimal stoneWeight,
            BigDecimal netWeight,
            @NotNull BigDecimal purityPercent,
            BigDecimal wastagePercent,
            @NotNull BigDecimal ratePerGram,
            String makingType,
            BigDecimal makingRate,
            BigDecimal stoneAmount,
            BigDecimal discountAmount,
            BigDecimal cgstPercent,
            BigDecimal sgstPercent,
            BigDecimal igstPercent,
            BigDecimal custWastagePercent,
            String sellCustWstgMode,
            BigDecimal valueAdded
    ) {
    }

    public record UpdateSettingRequest(
            @NotBlank String key,
            @NotBlank String value
    ) {
    }
}
