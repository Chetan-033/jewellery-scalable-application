package com.jewellery.erp.operations.bootstrap;

import com.jewellery.erp.config.JewelleryProperties;
import com.jewellery.erp.masterdata.domain.Customer;
import com.jewellery.erp.masterdata.domain.Metal;
import com.jewellery.erp.masterdata.domain.Purity;
import com.jewellery.erp.masterdata.repository.CustomerRepository;
import com.jewellery.erp.masterdata.repository.MetalRepository;
import com.jewellery.erp.masterdata.repository.PurityRepository;
import com.jewellery.erp.organization.domain.Branch;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.rates.domain.MetalRate;
import com.jewellery.erp.rates.repository.MetalRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Ensures a shop can operate immediately: metals, purities, daily rates, sample customer.
 * Runs after tenant bootstrap; safe to re-run (skips when GOLD already exists).
 */
@Component
@Order(20)
public class MasterDataSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MasterDataSeedRunner.class);

    private final JewelleryProperties properties;
    private final MetalRepository metalRepository;
    private final PurityRepository purityRepository;
    private final MetalRateRepository metalRateRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;

    public MasterDataSeedRunner(
            JewelleryProperties properties,
            MetalRepository metalRepository,
            PurityRepository purityRepository,
            MetalRateRepository metalRateRepository,
            CustomerRepository customerRepository,
            BranchRepository branchRepository) {
        this.properties = properties;
        this.metalRepository = metalRepository;
        this.purityRepository = purityRepository;
        this.metalRateRepository = metalRateRepository;
        this.customerRepository = customerRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.getBootstrap().isEnabled()) {
            return;
        }
        if (metalRepository.findByCode("GOLD").isPresent()) {
            log.info("Master seed skipped — metals already present");
            return;
        }

        Metal gold = saveMetal("GOLD", "Gold", "Au");
        Metal silver = saveMetal("SILVER", "Silver", "Ag");

        Purity g24 = savePurity(gold.getId(), "24K", "99.9000");
        Purity g22 = savePurity(gold.getId(), "22K", "91.6000");
        Purity g18 = savePurity(gold.getId(), "18K", "75.0000");
        Purity s925 = savePurity(silver.getId(), "925", "92.5000");

        Branch branch = branchRepository.findAll().stream().findFirst().orElse(null);
        String branchId = branch == null ? null : branch.getId();

        // Reference-style display rates (~ per 10g on ticker = ratePerGram * 10)
        saveRate(gold.getId(), g24.getId(), branchId, "13515.0000"); // ~135150 / 10GM
        saveRate(gold.getId(), g22.getId(), branchId, "12380.0000");
        saveRate(gold.getId(), g18.getId(), branchId, "10125.0000");
        saveRate(silver.getId(), s925.getId(), branchId, "95.0000");

        if (customerRepository.count() == 0) {
            Customer demo = new Customer();
            demo.setId(UUID.randomUUID().toString());
            demo.setCode("C1001");
            demo.setTitle("Ms.");
            demo.setFirstName("Demo");
            demo.setLastName("Customer");
            demo.setName("Demo Customer");
            demo.setPhone("9999999999");
            demo.setCity("Hyderabad");
            demo.setStateName("Telangana");
            demo.setCountryName("India");
            demo.setGender("F");
            demo.setPartyType("CUSTOMER");
            customerRepository.save(demo);
        }

        log.info("Master seed complete — GOLD/SILVER, purities, rates, demo customer ready");
    }

    private Metal saveMetal(String code, String name, String symbol) {
        Metal metal = new Metal();
        metal.setId(UUID.randomUUID().toString());
        metal.setCode(code);
        metal.setName(name);
        metal.setSymbol(symbol);
        metal.setWeightUnit("GRAM");
        metal.setActive(true);
        return metalRepository.save(metal);
    }

    private Purity savePurity(String metalId, String label, String fineness) {
        Purity purity = new Purity();
        purity.setId(UUID.randomUUID().toString());
        purity.setMetalId(metalId);
        purity.setLabel(label);
        purity.setFineness(new BigDecimal(fineness));
        purity.setActive(true);
        return purityRepository.save(purity);
    }

    private void saveRate(String metalId, String purityId, String branchId, String ratePerGram) {
        MetalRate rate = new MetalRate();
        rate.setId(UUID.randomUUID().toString());
        rate.setMetalId(metalId);
        rate.setPurityId(purityId);
        rate.setBranchId(branchId);
        rate.setRatePerGram(new BigDecimal(ratePerGram));
        rate.setCurrencyCode("INR");
        rate.setEffectiveFrom(Instant.now());
        rate.setSource("SEED");
        metalRateRepository.save(rate);
    }
}
