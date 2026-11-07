package com.jewellery.erp.calculation.application;

import com.jewellery.erp.calculation.StockAddSettings;
import com.jewellery.erp.calculation.domain.CalcSetting;
import com.jewellery.erp.calculation.repository.CalcSettingRepository;
import com.jewellery.erp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CalcSettingService {

    private final CalcSettingRepository repository;

    public CalcSettingService(CalcSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Map<String, String> allAsMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (CalcSetting row : repository.findAll()) {
            map.put(row.getSettingKey(), row.getSettingValue());
        }
        return map;
    }

    @Transactional(readOnly = true)
    public List<CalcSetting> list() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public String get(String key, String fallback) {
        return repository.findBySettingKey(key).map(CalcSetting::getSettingValue).orElse(fallback);
    }

    @Transactional
    public CalcSetting upsert(String key, String value) {
        CalcSetting row = repository.findBySettingKey(key).orElseGet(() -> {
            CalcSetting created = new CalcSetting();
            created.setId(UUID.randomUUID().toString());
            created.setSettingKey(key);
            return created;
        });
        row.setSettingValue(value);
        return repository.save(row);
    }

    @Transactional(readOnly = true)
    public StockAddSettings stockAddSettings() {
        StockAddSettings d = StockAddSettings.defaults();
        return new StockAddSettings(
                get("stockValBy", d.stockValBy()),
                get("stockMkgBy", d.stockMkgBy()),
                get("stockCsWstgBy", d.stockCsWstgBy()),
                get("stockLbrBy", d.stockLbrBy()),
                get("addStockFinalFinewt", d.addStockFinalFinewt()),
                get("stockFineBaseBy", d.stockFineBaseBy())
        );
    }

    @Transactional
    public CalcSetting requireUpdate(String key, String value) {
        if (repository.findBySettingKey(key).isEmpty() && !key.startsWith("stock") && !key.equals("addStockFinalFinewt")
                && !key.equals("WASTAGE_MODE") && !key.equals("MAKING_BASE") && !key.equals("TAX_INCLUSIVE")
                && !key.equals("ROUND_OFF_SCALE")) {
            throw new NotFoundException("Unknown setting key: " + key);
        }
        return upsert(key, value);
    }
}
