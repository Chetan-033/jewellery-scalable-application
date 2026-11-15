package com.jewellery.erp.shared.api;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standard paginated API envelope for list endpoints.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int safeSize = Math.max(size, 1);
        int totalPages = totalElements <= 0 ? 0 : (int) Math.ceil((double) totalElements / (double) safeSize);
        boolean last = totalPages == 0 || page + 1 >= totalPages;
        return new PageResponse<>(
                content == null ? List.of() : content,
                Math.max(page, 0),
                safeSize,
                Math.max(totalElements, 0),
                totalPages,
                page <= 0,
                last
        );
    }
}
