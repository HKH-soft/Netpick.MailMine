package ir.netpick.mailmine.common.utils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import ir.netpick.mailmine.common.PageDTO;

public class PageDTOMapper {
    public static <T, R> PageDTO<R> map(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return new PageDTO<>(
                content,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumberOfElements(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast());
    }

    public static <T, R> PageDTO<R> map(PageDTO<T> page, Function<T, R> mapper) {
        List<R> content = page.content().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return new PageDTO<>(
                content,
                page.currentPage() + 1,
                page.pageSize(),
                page.totalPages(),
                page.totalElements(),
                page.numberOfElements(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast());
    }

    public static <T> PageDTO<T> map(Page<T> page) {
        return new PageDTO<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumberOfElements(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast());
    }
}
