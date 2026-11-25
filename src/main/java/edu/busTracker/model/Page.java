package edu.busTracker.model;

import java.util.List;

/**
 * Generic paginated result for admin list endpoints.
 * Demonstrates: generic class (Unit 5 Generics).
 */
public class Page<T> {

    private List<T> items;
    private int page;
    private int pageSize;
    private long totalItems;
    private int totalPages;

    public Page() {}

    public Page(List<T> items, int page, int pageSize, long totalItems) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public long getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
}
