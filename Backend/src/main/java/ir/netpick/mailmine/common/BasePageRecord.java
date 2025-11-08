package ir.netpick.mailmine.common;

import java.util.List;

public record BasePageRecord<T>(
        List<T> context,
        Integer totalPageCount,
        Integer currentPage) {
}
