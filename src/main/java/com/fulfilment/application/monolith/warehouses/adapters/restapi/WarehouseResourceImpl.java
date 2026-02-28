package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.*;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.SearchWarehouseResponse;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject private SearchWarehouseOperation searchWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    // Convert API model to domain model
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = data.getBusinessUnitCode();
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock() != null ? data.getStock() : 0;

    try {
      // Create warehouse through use case (includes validations)
      createWarehouseOperation.create(domainWarehouse);

      // Return the created warehouse
      return toWarehouseResponse(domainWarehouse);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }


  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    // Find warehouse by business unit code
    var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);

    if (domainWarehouse == null) {
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }

    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    if(!NumberUtils.isParsable(id)) {
      throw new WebApplicationException("Invalid warehouse id: " + id, 400);
    }


    // Find warehouse by id
    var domainWarehouse = warehouseRepository.find("id", Long.parseLong(id)).firstResult();

    if (domainWarehouse == null) {
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }

    try {
      // Archive warehouse through use case (includes validations)
      archiveWarehouseOperation.archive(domainWarehouse.toWarehouse());
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
          String businessUnitCode, @NotNull Warehouse data) {
    // Convert API model to domain model
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = businessUnitCode; // Use businessUnitCode from path
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock() != null ? data.getStock() : 0;

    try {
      // Replace warehouse through use case (includes validations)
      replaceWarehouseOperation.replace(domainWarehouse);

      // Return the updated warehouse
      var updated = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
      return toWarehouseResponse(updated);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  /**
   * Search for warehouses with optional filtering, sorting, and pagination.
   *
   * @param location Filter by location identifier (e.g. AMSTERDAM-001)
   * @param minCapacity Filter warehouses with capacity >= this value
   * @param maxCapacity Filter warehouses with capacity <= this value
   * @param sortBy Sort field: createdAt (default) or capacity
   * @param sortOrder asc or desc (default: asc)
   * @param page Page number, 0-indexed (default: 0)
   * @param pageSize Page size (default: 10, max: 100)
   * @return SearchWarehouseResponse with paginated results and metadata
   */
  @Override
  public SearchWarehouseResponse searchWarehousesWithOptionalFiltersSortingAndPagination(String location, String minCapacity, String maxCapacity, String sortBy, String sortOrder, String page, String pageSize) {
    System.out.println("Entered search with params: location=" + location + ", minCapacity=" + minCapacity + ", maxCapacity=" + maxCapacity + ", sortBy=" + sortBy + ", sortOrder=" + sortOrder + ", page=" + page + ", pageSize=" + pageSize);
    Integer minCapacityInt = null;
    if (minCapacity != null && NumberUtils.isParsable(minCapacity)) {
      minCapacityInt = Integer.parseInt(minCapacity);
    }

    Integer maxCapacityInt = null;
    if (maxCapacity != null && NumberUtils.isParsable(maxCapacity)) {
      maxCapacityInt = Integer.parseInt(maxCapacity);
    }

    int pageInt = 0;
    if (page != null && NumberUtils.isParsable(page)) {
      pageInt = Integer.parseInt(page);
    }

    int pageSizeInt = 10;
    if (pageSize != null && NumberUtils.isParsable(pageSize)) {
      pageSizeInt = Integer.parseInt(pageSize);
    }

    SearchWarehouseQuery query =
            new SearchWarehouseQuery(location, minCapacityInt, maxCapacityInt, sortBy, sortOrder, pageInt, pageSizeInt);

    SearchWarehouseResult result = searchWarehouseOperation.search(query);

    List<Warehouse> responseWarehouses =
            result.getWarehouses().stream().map(this::toWarehouseResponse).toList();

    return toSearchResponse(responseWarehouses, result.getTotalCount(), result.getPage(), result.getPageSize());

  }

  private SearchWarehouseResponse toSearchResponse(List<Warehouse> responseWarehouses, long totalCount, int page, int pageSize) {
    SearchWarehouseResponse response = new SearchWarehouseResponse();
    response.setData(responseWarehouses);
    response.setTotalCount((int)totalCount);
    System.out.println("got total count: " + response.getTotalCount());
    response.setPage(page);
    response.setPageSize(pageSize);
    return response;
  }


  private Warehouse toWarehouseResponse(
          com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
