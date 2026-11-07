package com.jewellery.erp.sales.api;

import com.jewellery.erp.organization.application.DocumentNumberService;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.sales.domain.CustomerOrder;
import com.jewellery.erp.sales.repository.CustomerOrderRepository;
import com.jewellery.erp.shared.domain.MoneyMath;
import com.jewellery.erp.shared.domain.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CustomerOrderRepository repository;
    private final DocumentNumberService numbers;
    private final BranchRepository branchRepository;

    public OrderController(
            CustomerOrderRepository repository,
            DocumentNumberService numbers,
            BranchRepository branchRepository) {
        this.repository = repository;
        this.numbers = numbers;
        this.branchRepository = branchRepository;
    }

    @PostMapping
    @Transactional
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request, Authentication auth) {
        branchRepository.findById(request.branchId()).orElseThrow(() -> new NotFoundException("Branch not found"));
        CustomerOrder order = new CustomerOrder();
        order.setId(UUID.randomUUID().toString());
        order.setBranchId(request.branchId());
        order.setCustomerId(request.customerId());
        order.setOrderNo(numbers.next("ORDER"));
        order.setOrderDate(request.orderDate() == null ? LocalDate.now() : request.orderDate());
        order.setDueDate(request.dueDate());
        order.setItemName(request.itemName());
        order.setMetalId(request.metalId());
        order.setPurityId(request.purityId());
        order.setTargetWeight(request.targetWeight());
        order.setEstimatedAmount(request.estimatedAmount() == null ? null : MoneyMath.money(request.estimatedAmount()));
        order.setAdvanceAmount(MoneyMath.money(request.advanceAmount() == null ? BigDecimal.ZERO : request.advanceAmount()));
        order.setRemarks(request.remarks());
        order.setCreatedBy(auth == null ? null : String.valueOf(auth.getPrincipal()));
        repository.save(order);
        return toDto(order);
    }

    @GetMapping
    public List<OrderResponse> list() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    private OrderResponse toDto(CustomerOrder o) {
        return new OrderResponse(
                o.getId(), o.getOrderNo(), o.getOrderDate(), o.getDueDate(), o.getStatus(),
                o.getBranchId(), o.getCustomerId(), o.getItemName(), o.getTargetWeight(),
                o.getEstimatedAmount(), o.getAdvanceAmount(), o.getRemarks()
        );
    }

    public record CreateOrderRequest(
            @NotBlank String branchId,
            String customerId,
            LocalDate orderDate,
            LocalDate dueDate,
            @NotBlank String itemName,
            String metalId,
            String purityId,
            BigDecimal targetWeight,
            BigDecimal estimatedAmount,
            BigDecimal advanceAmount,
            String remarks
    ) {}

    public record OrderResponse(
            String id, String orderNo, LocalDate orderDate, LocalDate dueDate, String status,
            String branchId, String customerId, String itemName, BigDecimal targetWeight,
            BigDecimal estimatedAmount, BigDecimal advanceAmount, String remarks
    ) {}
}
