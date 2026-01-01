package ir.netpick.mailmine.common;

import java.util.List;

public record PageDTO<T>(
        List<T> content,
        Integer currentPage,
        Integer pageSize,
        Integer totalPages,
        Long totalElements,
        Integer numberOfElements,
        Boolean hasNext,
        Boolean hasPrevious,
        Boolean isFirst,
        Boolean isLast) {
}
