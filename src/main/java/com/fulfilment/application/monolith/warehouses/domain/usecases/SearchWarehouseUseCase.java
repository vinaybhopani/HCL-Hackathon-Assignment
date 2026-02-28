package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehouseQuery;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehouseResult;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SearchWarehouseUseCase implements SearchWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public SearchWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public SearchWarehouseResult search(SearchWarehouseQuery query) {
    return warehouseStore.search(query);
  }
}

