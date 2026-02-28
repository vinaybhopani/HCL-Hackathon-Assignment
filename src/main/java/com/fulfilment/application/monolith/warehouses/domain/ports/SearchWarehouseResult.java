package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.List;

/**
 * Result object for warehouse search operations.
 */
public class SearchWarehouseResult {
  private List<Warehouse> warehouses;
  private long totalCount;
  private int page;
  private int pageSize;

  public SearchWarehouseResult(
      List<Warehouse> warehouses, long totalCount, int page, int pageSize) {
    this.warehouses = warehouses;
    this.totalCount = totalCount;
    this.page = page;
    this.pageSize = pageSize;
  }

  public List<Warehouse> getWarehouses() {
    return warehouses;
  }

  public long getTotalCount() {
    return totalCount;
  }

  public int getPage() {
    return page;
  }

  public int getPageSize() {
    return pageSize;
  }

  public long getTotalPages() {
    return (totalCount + pageSize - 1) / pageSize;
  }
}

