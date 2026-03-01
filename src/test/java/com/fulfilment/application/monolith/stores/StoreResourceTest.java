package com.fulfilment.application.monolith.stores;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreResourceTest {

    @Inject
    StoreResource storeResource;


    @AfterEach
    @Transactional
    public void tearDown() {
        Store.deleteAll();
    }

    @Test
    public void testGet() {
        List<Store> result = storeResource.get();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    public void testGetSingle() {
        Store store = new Store();
        store.name = "Test Store";
        store.persist();

        Store result = storeResource.getSingle(store.id);
        assertNotNull(result);
        assertEquals("Test Store", result.name);
    }

    @Test
    public void testGetSingleNotFound() {
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> storeResource.getSingle(999L));
        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    public void testCreate() {
        Store newStore = new Store();
        newStore.name = "New Store";

        Response response = storeResource.create(newStore);
        assertEquals(201, response.getStatus());
        Store created = (Store) response.getEntity();
        assertEquals("New Store", created.name);
        assertNotNull(created.id);

    }

    @Test
    public void testCreateWithId() {
        Store newStore = new Store();
        newStore.id = 1L;
        newStore.name = "New Store";

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> storeResource.create(newStore));
        assertEquals(422, exception.getResponse().getStatus());
    }

    @Test
    @Transactional
    public void testCreateDuplicateName() {
        Store existingStore = new Store();
        existingStore.name = "Existing Store";
        existingStore.persist();

        Store newStore = new Store();
        newStore.name = "Existing Store";

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> storeResource.create(newStore));
        assertEquals(500, exception.getResponse().getStatus());
    }

    @Test
    @Transactional
    public void testUpdate() {
        Store existingStore = new Store();
        existingStore.name = "Old Name";
        existingStore.quantityProductsInStock = 10;
        existingStore.persist();

        Store updatedStore = new Store();
        updatedStore.name = "New Name";
        updatedStore.quantityProductsInStock = 20;

        Store result = storeResource.update(existingStore.id, updatedStore);

        assertEquals("New Name", result.name);
        assertEquals(20, result.quantityProductsInStock);

    }

    @Test
    public void testUpdateNotFound() {
        Store updatedStore = new Store();
        updatedStore.name = "New Name";

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> storeResource.update(999L, updatedStore));
        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    public void testUpdateInvalidName() {
        Store updatedStore = new Store();

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> storeResource.update(1L, updatedStore));
        assertEquals(422, exception.getResponse().getStatus());
    }

    @Test
    @Transactional
    public void testPatch() {
        Store existingStore = new Store();
        existingStore.name = "Old Name";
        existingStore.quantityProductsInStock = 10;
        existingStore.persist();

        Store updatedStore = new Store();
        updatedStore.name = "New Name";

        Store result = storeResource.patch(existingStore.id, updatedStore);

        assertEquals("New Name", result.name);
        // Based on logic: if (entity.quantityProductsInStock != 0) { entity.quantityProductsInStock = updatedStore.quantityProductsInStock; }
        // entity has 10. updatedStore has 0. so entity becomes 0.
        assertEquals(0, result.quantityProductsInStock);

    }

    @Test
    @Transactional
    public void testPatchLogicClarification() {
        Store existingStore = new Store();
        existingStore.name = "Old";
        existingStore.quantityProductsInStock = 0; // Existing is 0
        existingStore.persist();

        Store updated = new Store();
        updated.name = "New";
        updated.quantityProductsInStock = 50;

        Store result = storeResource.patch(existingStore.id, updated);

        assertEquals(0, result.quantityProductsInStock);

    }

    @Test
    public void testPatchNotFound() {
        Store updatedStore = new Store();
        updatedStore.name = "New Name";

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> storeResource.patch(999L, updatedStore));
        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    @Transactional
    public void testDelete() {
        Store existingStore = new Store();
        existingStore.name = "To Delete";
        existingStore.persist();
        Long id = existingStore.id;

        Response response = storeResource.delete(id);
        assertEquals(204, response.getStatus());

        assertNull(Store.findById(id));
    }

    @Test
    public void testDeleteNotFound() {
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> storeResource.delete(999L));
        assertEquals(404, exception.getResponse().getStatus());
    }
}

