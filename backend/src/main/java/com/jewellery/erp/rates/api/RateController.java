package com.jewellery.erp.rates.api;

import com.jewellery.erp.masterdata.domain.Metal;
import com.jewellery.erp.masterdata.domain.Purity;
import com.jewellery.erp.masterdata.repository.MetalRepository;
import com.jewellery.erp.masterdata.repository.PurityRepository;
import com.jewellery.erp.rates.domain.MetalRate;
import com.jewellery.erp.rates.repository.MetalRateRepository;
import com.jewellery.erp.shared.domain.MoneyMath;
import com.jewellery.erp.shared.domain.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rates")
public class RateController {

    private final MetalRateRepository metalRateRepository;
    private final MetalRepository metalRepository;

    private final PurityRepository purityRepository;

    public RateController(
            MetalRateRepository metalRateRepository,
            MetalRepository metalRepository,
            PurityRepository purityRepository) {
        this.metalRateRepository = metalRateRepository;
        this.metalRepository = metalRepository;
        this.purityRepository = purityRepository;
    }

    /** Latest rate per metal (and purity, when the rate is purity-specific) for the display ticker. */
    @GetMapping("/ticker")
    public List<TickerRate> ticker() {
        Map<String, Metal> metals = metalRepository.findAll().stream()
                .collect(Collectors.toMap(Metal::getId, Function.identity()));
        Map<String, Purity> purities = purityRepository.findAll().stream()
                .collect(Collectors.toMap(Purity::getId, Function.identity()));
        Map<String, MetalRate> latest = new LinkedHashMap<>();
        for (MetalRate rate : metalRateRepository.findLatestPerMetalAndPurity(Instant.now())) {
            latest.putIfAbsent(rate.getMetalId() + "|" + (rate.getPurityId() == null ? "" : rate.getPurityId()), rate);
        }
        BigDecimal ten = BigDecimal.TEN;
        return latest.values().stream()
                .filter(r -> metals.containsKey(r.getMetalId()))
                .map(r -> {
                    Metal m = metals.get(r.getMetalId());
                    Purity p = r.getPurityId() == null ? null : purities.get(r.getPurityId());
                    BigDecimal perGram = MoneyMath.rate(r.getRatePerGram());
                    return new TickerRate(m.getCode(), m.getName(), p == null ? null : p.getLabel(),
                            perGram, MoneyMath.rate(perGram.multiply(ten)));
                })
                .sorted(Comparator.comparing(TickerRate::metalCode).thenComparing(
                        t -> t.purityLabel() == null ? "" : t.purityLabel()))
                .toList();
    }

    @PostMapping("/metal")
    public RateResponse create(@Valid @RequestBody CreateRateRequest request, Authentication authentication) {
        metalRepository.findById(request.metalId())
                .orElseThrow(() -> new NotFoundException("Metal not found"));
        MetalRate rate = new MetalRate();
        rate.setId(UUID.randomUUID().toString());
        rate.setMetalId(request.metalId());
        rate.setPurityId(request.purityId());
        rate.setBranchId(request.branchId());
        rate.setRatePerGram(MoneyMath.rate(request.ratePerGram()));
        rate.setEffectiveFrom(request.effectiveFrom() == null ? Instant.now() : request.effectiveFrom());
        rate.setSource("MANUAL");
        if (authentication != null) {
            rate.setCreatedBy(String.valueOf(authentication.getPrincipal()));
        }
        metalRateRepository.save(rate);
        return toDto(rate);
    }

    @GetMapping("/metal")
    public List<RateResponse> list(@RequestParam String metalId) {
        return metalRateRepository.findByMetalIdOrderByEffectiveFromDesc(metalId).stream()
                .map(this::toDto)
                .toList();
    }

    private RateResponse toDto(MetalRate rate) {
        return new RateResponse(
                rate.getId(),
                rate.getMetalId(),
                rate.getPurityId(),
                rate.getBranchId(),
                rate.getRatePerGram(),
                rate.getCurrencyCode(),
                rate.getEffectiveFrom(),
                rate.getSource()
        );
    }

    public record CreateRateRequest(
            @NotBlank String metalId,
            String purityId,
            String branchId,
            @NotNull BigDecimal ratePerGram,
            Instant effectiveFrom
    ) {
    }

    public record TickerRate(
            String metalCode,
            String metalName,
            String purityLabel,
            BigDecimal ratePerGram,
            BigDecimal ratePerTenGram
    ) {
    }

    public record RateResponse(
            String id,
            String metalId,
            String purityId,
            String branchId,
            BigDecimal ratePerGram,
            String currencyCode,
            Instant effectiveFrom,
            String source
    ) {
    }
}
