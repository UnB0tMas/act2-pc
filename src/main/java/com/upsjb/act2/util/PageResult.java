package com.upsjb.act2.util;

import java.util.Collections;
import java.util.List;

public class PageResult<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public PageResult(List<T> content, int page, int size, long totalElements) {
        this.content = content == null ? Collections.emptyList() : content;
        this.page = Math.max(page, 0);
        this.size = Math.max(size, 1);
        this.totalElements = Math.max(totalElements, 0);
        this.totalPages = calculateTotalPages(this.totalElements, this.size);
    }

    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        return new PageResult<>(content, page, size, totalElements);
    }

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getCurrentPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public boolean hasContent() {
        return !content.isEmpty();
    }

    public boolean isFirst() {
        return page <= 0;
    }

    public boolean isLast() {
        return totalPages == 0 || page >= totalPages - 1;
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean hasNext() {
        return totalPages > 0 && page < totalPages - 1;
    }

    public int getPreviousPage() {
        return hasPrevious() ? page - 1 : 0;
    }

    public int getNextPage() {
        return hasNext() ? page + 1 : page;
    }

    public int getFromElement() {
        if (totalElements == 0) {
            return 0;
        }
        return page * size + 1;
    }

    public long getToElement() {
        if (totalElements == 0) {
            return 0;
        }
        long to = (long) (page + 1) * size;
        return Math.min(to, totalElements);
    }

    private static int calculateTotalPages(long totalElements, int size) {
        if (totalElements <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }
}