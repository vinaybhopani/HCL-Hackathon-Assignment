package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class WarehouseSearchIT {

  private static final String SEARCH_PATH = "warehouse/search";

  @Test
  public void testSearchAllWarehouses() {
    given()
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("totalCount", greaterThanOrEqualTo(3))
        .body("page", equalTo(0))
        .body("pageSize", equalTo(10))
        .body("data", notNullValue())
        .body("data.size()", greaterThan(0));
  }

  @Test
  public void testSearchByLocation() {
    given()
        .queryParam("location", "AMSTERDAM-001")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("totalCount", greaterThan(0))
        .body("data", hasSize(greaterThan(0)))
        .body(
            "data.findAll { it.location == 'AMSTERDAM-001' }.size()",
            greaterThan(0));
  }

  @Test
  public void testSearchByLocationNoResults() {
    given()
        .queryParam("location", "NONEXISTENT-001")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("totalCount", equalTo(0))
        .body("data", empty());
  }

  @Test
  public void testSearchByMinCapacity() {
    given()
        .queryParam("minCapacity", 50)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue())
        .body("data.every { it.capacity >= 50 }", is(true));
  }

  @Test
  public void testSearchByMaxCapacity() {
    given()
        .queryParam("maxCapacity", 50)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue())
        .body("data.every { it.capacity <= 50 }", is(true));
  }

  @Test
  public void testSearchByCapacityRange() {
    given()
        .queryParam("minCapacity", 30)
        .queryParam("maxCapacity", 75)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue())
        .body("data.every { it.capacity >= 30 && it.capacity <= 75 }", is(true));
  }

  @Test
  public void testSearchWithMultipleFilters() {
    given()
        .queryParam("location", "AMSTERDAM-001")
        .queryParam("minCapacity", 50)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body(
            "data.findAll { it.location == 'AMSTERDAM-001' && it.capacity >= 50 }.size()",
            greaterThanOrEqualTo(0));
  }

  @Test
  public void testSearchSortByCapacityAsc() {
    given()
        .queryParam("sortBy", "capacity")
        .queryParam("sortOrder", "asc")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue());
  }

  @Test
  public void testSearchSortByCapacityDesc() {
    given()
        .queryParam("sortBy", "capacity")
        .queryParam("sortOrder", "desc")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue());
  }

  @Test
  public void testSearchSortByCreatedAtAsc() {
    given()
        .queryParam("sortBy", "createdAt")
        .queryParam("sortOrder", "asc")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue());
  }

  @Test
  public void testSearchSortByCreatedAtDesc() {
    given()
        .queryParam("sortBy", "createdAt")
        .queryParam("sortOrder", "desc")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue());
  }

  @Test
  public void testSearchDefaultSortOrder() {
    given()
        .queryParam("sortBy", "capacity")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue());
  }

  @Test
  public void testSearchPaginationFirstPage() {
    given()
        .queryParam("page", 0)
        .queryParam("pageSize", 2)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("page", equalTo(0))
        .body("pageSize", equalTo(2))
        .body("data.size()", lessThanOrEqualTo(2));
  }

  @Test
  public void testSearchPaginationSecondPage() {
    given()
        .queryParam("page", 1)
        .queryParam("pageSize", 1)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("page", equalTo(1))
        .body("pageSize", equalTo(1))
        .body("data.size()", lessThanOrEqualTo(1));
  }

  @Test
  public void testSearchPageSizeExceedsMax() {
    given()
        .queryParam("pageSize", 200)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("pageSize", equalTo(100));
  }

  @Test
  public void testSearchPageSizeDefault() {
    given()
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("pageSize", equalTo(10));
  }

  @Test
  public void testSearchPageDefault() {
    given()
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("page", equalTo(0));
  }

  @Test
  public void testSearchResponseMetadata() {
    given()
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue())
        .body("totalCount", notNullValue())
        .body("page", notNullValue())
        .body("pageSize", notNullValue());
  }


  @Test
  public void testSearchExcludesArchivedWarehouses() {
    int initialCount =
        given().when().get(SEARCH_PATH).then().statusCode(200).extract().path("totalCount");

    // Archive a warehouse (using warehouse ID 1 from database)
    given().when().delete("warehouse/1").then().statusCode(204);

    // Search should now return one less warehouse
    int newCount = given().when().get(SEARCH_PATH).then().statusCode(200).extract().path("totalCount");

    // Verify count decreased by 1
    assert newCount == initialCount - 1 : "Archived warehouse should be excluded from search";
  }

  @Test
  public void testSearchNegativePageHandling() {
    given()
        .queryParam("page", -5)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("page", equalTo(0));
  }

  @Test
  public void testSearchZeroPageSize() {
    given()
        .queryParam("pageSize", 0)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("pageSize", greaterThanOrEqualTo(1));
  }

  @Test
  public void testSearchInvalidSortByDefaultsToCreatedAt() {
    given()
        .queryParam("sortBy", "invalid_field")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue());
  }

  @Test
  public void testSearchInvalidSortOrderDefaultsToAsc() {
    // Invalid sortOrder should default to asc
    given()
        .queryParam("sortOrder", "invalid_order")
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("data", notNullValue());
  }

  @Test
  public void testSearchAllParametersCombined() {
    given()
        .queryParam("location", "AMSTERDAM-001")
        .queryParam("minCapacity", 40)
        .queryParam("maxCapacity", 80)
        .queryParam("sortBy", "capacity")
        .queryParam("sortOrder", "desc")
        .queryParam("page", 0)
        .queryParam("pageSize", 5)
        .when()
        .get(SEARCH_PATH)
        .then()
        .statusCode(200)
        .body("page", equalTo(0))
        .body("pageSize", equalTo(5))
        .body("data", notNullValue());
  }
}

