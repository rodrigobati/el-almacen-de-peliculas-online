package unrn.api;

import java.util.List;

public record PageResponse<T>(List<T> items, long total, int totalPages, int page, int size) {
    public static <T> PageResponse<T> of(List<T> items, long total, int page, int size) {
        int safeSize = size <= 0 ? 1 : size;
        int safePage = Math.max(0, page);
        int computedTotalPages = (int) Math.ceil((double) total / safeSize);
        return new PageResponse<>(items, total, computedTotalPages, safePage, safeSize);
    }
}
