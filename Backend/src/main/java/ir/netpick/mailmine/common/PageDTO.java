package ir.netpick.mailmine.common;

import java.util.List;

public record PageDTO<T>(
        List<T> context,
        Integer totalPageCount,
        Integer currentPage) {
}
