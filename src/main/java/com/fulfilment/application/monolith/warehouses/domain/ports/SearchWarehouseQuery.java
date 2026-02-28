package com.fulfilment.application.monolith.warehouses.domain.ports;

/**
 * Query object for searching warehouses with filters, sorting, and pagination.
 */
public class SearchWarehouseQuery {
  private String location;
  private Integer minCapacity;
  private Integer maxCapacity;
  private String sortBy; // "createdAt" or "capacity"
  private String sortOrder; // "asc" or "desc"
  private int page; // 0-indexed
  private int pageSize;

  public SearchWarehouseQuery(
      String location,
      Integer minCapacity,
      Integer maxCapacity,
      String sortBy,
      String sortOrder,
      int page,
      int pageSize) {
    this.location = location;
    this.minCapacity = minCapacity;
    this.maxCapacity = maxCapacity;
    this.sortBy = sortBy != null && !sortBy.isEmpty() ? sortBy : "createdAt";
    this.sortOrder = sortOrder != null && !sortOrder.isEmpty() ? sortOrder : "asc";
    this.page = Math.max(0, page);
    this.pageSize = Math.min(100, Math.max(1, pageSize));
  }

  public String getLocation() {
    return location;
  }

  public Integer getMinCapacity() {
    return minCapacity;
  }

  public Integer getMaxCapacity() {
    return maxCapacity;
  }

  public String getSortBy() {
    return sortBy;
  }

  public String getSortOrder() {
    return sortOrder;
  }

  public int getPage() {
    return page;
  }

  public int getPageSize() {
    return pageSize;
  }

  public int getOffset() {
    return page * pageSize;
  }
}

