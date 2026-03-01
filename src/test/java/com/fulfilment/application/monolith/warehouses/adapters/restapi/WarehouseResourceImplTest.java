package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.*;
import com.warehouse.api.beans.SearchWarehouseResponse;
import com.warehouse.api.beans.Warehouse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseResourceImplTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private CreateWarehouseOperation createWarehouseOperation;

    @Mock
    private ArchiveWarehouseOperation archiveWarehouseOperation;

    @Mock
    private ReplaceWarehouseOperation replaceWarehouseOperation;

    @Mock
    private SearchWarehouseOperation searchWarehouseOperation;

    @InjectMocks
    private WarehouseResourceImpl warehouseResource;

    @Test
    void testListAllWarehousesUnits() {
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domainWarehouse.businessUnitCode = "TEST-001";
        domainWarehouse.location = "Test Location";
        domainWarehouse.capacity = 100;
        domainWarehouse.stock = 50;

        when(warehouseRepository.getAll()).thenReturn(List.of(domainWarehouse));

        List<Warehouse> result = warehouseResource.listAllWarehousesUnits();

        assertEquals(1, result.size());
        assertEquals("TEST-001", result.get(0).getBusinessUnitCode());
    }

    @Test
    void testCreateANewWarehouseUnit() {
        Warehouse input = new Warehouse();
        input.setBusinessUnitCode("NEW-001");
        input.setLocation("New Location");
        input.setCapacity(200);
        input.setStock(10);

        doNothing().when(createWarehouseOperation).create(any(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class));

        Warehouse result = warehouseResource.createANewWarehouseUnit(input);

        assertNotNull(result);
        assertEquals("NEW-001", result.getBusinessUnitCode());
        verify(createWarehouseOperation, times(1)).create(any(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class));
    }

    @Test
    void testCreateANewWarehouseUnit_ThrowsException() {
        Warehouse input = new Warehouse();
        input.setBusinessUnitCode("INVALID");

        doThrow(new IllegalArgumentException("Invalid data")).when(createWarehouseOperation).create(any(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class));

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            warehouseResource.createANewWarehouseUnit(input);
        });

        assertEquals(400, exception.getResponse().getStatus());
    }

    @Test
    void testGetAWarehouseUnitByID() {
        String id = "TEST-001";
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domainWarehouse.businessUnitCode = id;

        when(warehouseRepository.findByBusinessUnitCode(id)).thenReturn(domainWarehouse);

        Warehouse result = warehouseResource.getAWarehouseUnitByID(id);

        assertNotNull(result);
        assertEquals(id, result.getBusinessUnitCode());
    }

    @Test
    void testGetAWarehouseUnitByID_NotFound() {
        String id = "UNKNOWN";
        when(warehouseRepository.findByBusinessUnitCode(id)).thenReturn(null);

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            warehouseResource.getAWarehouseUnitByID(id);
        });

        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void testArchiveAWarehouseUnitByID() {
        String idStr = "123";
        Long id = 123L;

        DbWarehouse dbWarehouse = mock(DbWarehouse.class);
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        when(dbWarehouse.toWarehouse()).thenReturn(domainWarehouse);

        PanacheQuery<DbWarehouse> query = mock(PanacheQuery.class);
        when(warehouseRepository.find("id", id)).thenReturn(query);
        when(query.firstResult()).thenReturn(dbWarehouse);

        doNothing().when(archiveWarehouseOperation).archive(any(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class));

        warehouseResource.archiveAWarehouseUnitByID(idStr);

        verify(archiveWarehouseOperation, times(1)).archive(any(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class));
    }

    @Test
    void testArchiveAWarehouseUnitByID_InvalidId() {
        String idStr = "INVALID";

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            warehouseResource.archiveAWarehouseUnitByID(idStr);
        });

        assertEquals(400, exception.getResponse().getStatus());
    }

    @Test
    void testArchiveAWarehouseUnitByID_NotFound() {
        String idStr = "123";
        Long id = 123L;

        @SuppressWarnings("unchecked")
        PanacheQuery<DbWarehouse> query = mock(PanacheQuery.class);
        when(warehouseRepository.find("id", id)).thenReturn(query);
        when(query.firstResult()).thenReturn(null);

        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            warehouseResource.archiveAWarehouseUnitByID(idStr)
        );

        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void testArchiveAWarehouseUnitByID_OperationThrows() {
        String idStr = "123";
        Long id = 123L;

        DbWarehouse dbWarehouse = mock(DbWarehouse.class);
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        when(dbWarehouse.toWarehouse()).thenReturn(domainWarehouse);

        @SuppressWarnings("unchecked")
        PanacheQuery<DbWarehouse> query = mock(PanacheQuery.class);
        when(warehouseRepository.find("id", id)).thenReturn(query);
        when(query.firstResult()).thenReturn(dbWarehouse);

        doThrow(new IllegalArgumentException("Cannot archive")).when(archiveWarehouseOperation).archive(any(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class));

        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            warehouseResource.archiveAWarehouseUnitByID(idStr)
        );

        assertEquals(400, exception.getResponse().getStatus());
    }


     @Test
    void testReplaceTheCurrentActiveWarehouse() {
        String businessUnitCode = "TEST-001";
        Warehouse input = new Warehouse();
        input.setBusinessUnitCode(businessUnitCode);
        input.setLocation("Updated Location");
        input.setCapacity(300);
        input.setStock(20);

        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse updatedWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        updatedWarehouse.businessUnitCode = businessUnitCode;
        updatedWarehouse.location = "Updated Location";

        doNothing().when(replaceWarehouseOperation).replace(any(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class));
        when(warehouseRepository.findByBusinessUnitCode(businessUnitCode)).thenReturn(updatedWarehouse);

        Warehouse result = warehouseResource.replaceTheCurrentActiveWarehouse(businessUnitCode, input);

        assertNotNull(result);
        assertEquals("Updated Location", result.getLocation());
        verify(replaceWarehouseOperation, times(1)).replace(any(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class));
    }

    @Test
    void testReplaceTheCurrentActiveWarehouse_Throws() {
        String businessUnitCode = "TEST-001";
        Warehouse input = new Warehouse();
        input.setLocation("Updated Location");

        doThrow(new IllegalArgumentException("Error")).when(replaceWarehouseOperation).replace(any(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class));

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            warehouseResource.replaceTheCurrentActiveWarehouse(businessUnitCode, input);
        });

        assertEquals(400, exception.getResponse().getStatus());
    }

    @Test
    void testSearchWarehousesWithOptionalFiltersSortingAndPagination() {
        String location = "AMSTERDAM";
        String minCapacity = "10";
        String maxCapacity = "100";
        String sortBy = "capacity";
        String sortOrder = "asc";
        String page = "0";
        String pageSize = "5";

        SearchWarehouseResult searchResult = new SearchWarehouseResult(Collections.emptyList(), 0, 0, 5);

        when(searchWarehouseOperation.search(any(SearchWarehouseQuery.class))).thenReturn(searchResult);

        SearchWarehouseResponse response = warehouseResource.searchWarehousesWithOptionalFiltersSortingAndPagination(
                location, minCapacity, maxCapacity, sortBy, sortOrder, page, pageSize);

        assertNotNull(response);
        assertEquals(0, response.getTotalCount());
        verify(searchWarehouseOperation, times(1)).search(any(SearchWarehouseQuery.class));
    }
}

