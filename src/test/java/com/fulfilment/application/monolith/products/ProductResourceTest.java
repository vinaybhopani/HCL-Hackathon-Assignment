package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ProductResourceTest {

    @Inject
    ProductResource productResource;

    @Inject
    ProductRepository productRepository;

    @AfterEach
    @Transactional
    public void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    public void testGet() {
        List<Product> result = productResource.get();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    public void testGetSingle() {
        Product product = new Product();
        product.name = "Test Product";
        product.description = "Test Description";
        product.price = BigDecimal.valueOf(10.00);
        product.stock = 10;
        productRepository.persist(product);

        Product result = productResource.getSingle(product.id);
        assertNotNull(result);
        assertEquals("Test Product", result.name);
    }

    @Test
    public void testGetSingleNotFound() {
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> productResource.getSingle(999L));
        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    @Transactional
    public void testCreate() {
        Product newProduct = new Product();
        newProduct.name = "New Product";
        newProduct.description = "New Description";
        newProduct.price = BigDecimal.valueOf(20.00);
        newProduct.stock = 5;

        Response response = productResource.create(newProduct);
        assertEquals(201, response.getStatus());
        Product created = (Product) response.getEntity();
        assertEquals("New Product", created.name);
        assertNotNull(created.id);
    }

    @Test
    public void testCreateWithId() {
        Product newProduct = new Product();
        newProduct.id = 1L;
        newProduct.name = "New Product";

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> productResource.create(newProduct));
        assertEquals(422, exception.getResponse().getStatus());
    }

    @Test
    @Transactional
    public void testCreateDuplicateName() {
        Product existingProduct = new Product();
        existingProduct.name = "Existing Product";
        existingProduct.description = "Existing Description";
        existingProduct.price = BigDecimal.valueOf(10.00);
        existingProduct.stock = 10;
        productRepository.persist(existingProduct);

        Product newProduct = new Product();
        newProduct.name = "Existing Product";
        newProduct.description = "Another Description";
        newProduct.price = BigDecimal.valueOf(15.00);
        newProduct.stock = 5;

        assertThrows(Exception.class, () -> productResource.create(newProduct));
    }

    @Test
    @Transactional
    public void testUpdate() {
        Product existingProduct = new Product();
        existingProduct.name = "Old Name";
        existingProduct.description = "Old Desc";
        existingProduct.price = BigDecimal.valueOf(10.00);
        existingProduct.stock = 10;
        productRepository.persist(existingProduct);

        Product updatedProduct = new Product();
        updatedProduct.name = "New Name";
        updatedProduct.description = "New Desc";
        updatedProduct.price = BigDecimal.valueOf(20.00);
        updatedProduct.stock = 20;

        Product result = productResource.update(existingProduct.id, updatedProduct);

        assertEquals("New Name", result.name);
        assertEquals("New Desc", result.description);
        assertEquals(BigDecimal.valueOf(20.00), result.price);
        assertEquals(20, result.stock);
    }

    @Test
    public void testUpdateNotFound() {
        Product updatedProduct = new Product();
        updatedProduct.name = "New Name";

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> productResource.update(999L, updatedProduct));
        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    public void testUpdateInvalidName() {
        Product updatedProduct = new Product();
        // name is null

        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> productResource.update(1L, updatedProduct));
        assertEquals(422, exception.getResponse().getStatus());
    }

    @Test
    @Transactional
    public void testDelete() {
        Product existingProduct = new Product();
        existingProduct.name = "To Delete";
        existingProduct.description = "Desc";
        existingProduct.price = BigDecimal.valueOf(10.00);
        existingProduct.stock = 10;
        productRepository.persist(existingProduct);
        Long id = existingProduct.id;

        Response response = productResource.delete(id);
        assertEquals(204, response.getStatus());

        assertNull(productRepository.findById(id));
    }

    @Test
    public void testDeleteNotFound() {
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> productResource.delete(999L));
        assertEquals(404, exception.getResponse().getStatus());
    }
}


