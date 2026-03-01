package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehouseQuery;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehouseResult;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class SearchWarehouseUseCaseTest {

  @Inject
  WarehouseRepository warehouseRepository;

  @Inject
  SearchWarehouseUseCase searchWarehouseUseCase;

  @Inject
  EntityManager em;

  @BeforeEach
  @Transactional
  public void setup() {
    em.createQuery("DELETE FROM DbWarehouse").executeUpdate();
  }

  @Test
  @Transactional
  public void testSearchAllActiveWarehouses() {
    createWarehouse("SEARCH-001", "AMSTERDAM-001", 100, 50);
    createWarehouse("SEARCH-002", "ZWOLLE-001", 200, 10);

    SearchWarehouseQuery query = new SearchWarehouseQuery(null, null, null, null, null, 0, 10);
    SearchWarehouseResult result = searchWarehouseUseCase.search(query);

    assertEquals(2, result.getTotalCount());
    assertEquals(2, result.getWarehouses().size());
  }

  @Test
  @Transactional
  public void testSearchByLocation() {
    createWarehouse("SEARCH-001", "AMSTERDAM-001", 100, 50);
    createWarehouse("SEARCH-002", "ZWOLLE-001", 200, 10);

    SearchWarehouseQuery query = new SearchWarehouseQuery("AMSTERDAM-001", null, null, null, null, 0, 10);
    SearchWarehouseResult result = searchWarehouseUseCase.search(query);

    assertEquals(1, result.getTotalCount());
    assertEquals("SEARCH-001", result.getWarehouses().get(0).businessUnitCode);
  }

  @Test
  @Transactional
  public void testSearchByMinCapacity() {
    createWarehouse("SEARCH-001", "AMSTERDAM-001", 50, 50);
    createWarehouse("SEARCH-002", "ZWOLLE-001", 150, 10);

    SearchWarehouseQuery query = new SearchWarehouseQuery(null, 100, null, null, null, 0, 10);
    SearchWarehouseResult result = searchWarehouseUseCase.search(query);

    assertEquals(1, result.getTotalCount());
    assertEquals("SEARCH-002", result.getWarehouses().get(0).businessUnitCode);
  }

  @Test
  @Transactional
  public void testSearchByMaxCapacity() {
    createWarehouse("SEARCH-001", "AMSTERDAM-001", 50, 50);
    createWarehouse("SEARCH-002", "ZWOLLE-001", 150, 10);

    SearchWarehouseQuery query = new SearchWarehouseQuery(null, null, 100, null, null, 0, 10);
    SearchWarehouseResult result = searchWarehouseUseCase.search(query);

    assertEquals(1, result.getTotalCount());
    assertEquals("SEARCH-001", result.getWarehouses().get(0).businessUnitCode);
  }

  @Test
  @Transactional
  public void testSearchArchivedExcluded() {
    createWarehouse("SEARCH-001", "AMSTERDAM-001", 100, 50);

    Warehouse archived = createWarehouse("SEARCH-002", "ZWOLLE-001", 200, 10);
    archived.archivedAt = LocalDateTime.now();
    warehouseRepository.update(archived);

    SearchWarehouseQuery query = new SearchWarehouseQuery(null, null, null, null, null, 0, 10);
    SearchWarehouseResult result = searchWarehouseUseCase.search(query);

    assertEquals(1, result.getTotalCount());
    assertEquals("SEARCH-001", result.getWarehouses().get(0).businessUnitCode);
  }

  @Test
  public void testPagination() throws InterruptedException {
    for (int i = 1; i <= 5; i++) {
        createWarehouse("PAGE-" + i, "AMSTERDAM-001", 100, 50);
        Thread.sleep(100);
    }

    // Page 0, size 2
    SearchWarehouseQuery query1 = new SearchWarehouseQuery(null, null, null, "createdAt", "asc", 0, 2);
    SearchWarehouseResult result1 = searchWarehouseUseCase.search(query1);
    assertEquals(5, result1.getTotalCount());
    assertEquals(2, result1.getWarehouses().size());
    assertEquals("PAGE-1", result1.getWarehouses().get(0).businessUnitCode);
    assertEquals("PAGE-2", result1.getWarehouses().get(1).businessUnitCode);

    // Page 1, size 2
    SearchWarehouseQuery query2 = new SearchWarehouseQuery(null, null, null, "createdAt", "asc", 1, 2);
    SearchWarehouseResult result2 = searchWarehouseUseCase.search(query2);
    assertEquals(5, result2.getTotalCount());
    assertEquals(2, result2.getWarehouses().size());
    assertEquals("PAGE-3", result2.getWarehouses().get(0).businessUnitCode);
    assertEquals("PAGE-4", result2.getWarehouses().get(1).businessUnitCode);
  }

  @Test
  @Transactional
  public void testSortByCapacityDesc() {
    createWarehouse("SMALL", "AMSTERDAM-001", 50, 10);
    createWarehouse("LARGE", "AMSTERDAM-002", 200, 10);
    createWarehouse("MEDIUM", "AMSTERDAM-003", 100, 10);

    SearchWarehouseQuery query = new SearchWarehouseQuery(null, null, null, "capacity", "desc", 0, 10);
    SearchWarehouseResult result = searchWarehouseUseCase.search(query);

    List<Warehouse> list = result.getWarehouses();
    assertEquals(3, list.size());
    assertEquals("LARGE", list.get(0).businessUnitCode);
    assertEquals("MEDIUM", list.get(1).businessUnitCode);
    assertEquals("SMALL", list.get(2).businessUnitCode);
  }

  private Warehouse createWarehouse(String businessUnitCode, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.createdAt = LocalDateTime.now();

    warehouseRepository.create(warehouse);
    return warehouse;
  }
}


