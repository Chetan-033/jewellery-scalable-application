package com.jewellery.erp.calculation.repository;

import com.jewellery.erp.calculation.domain.CalcSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalcSettingRepository extends JpaRepository<CalcSetting, String> {
    Optional<CalcSetting> findBySettingKey(String settingKey);
}
