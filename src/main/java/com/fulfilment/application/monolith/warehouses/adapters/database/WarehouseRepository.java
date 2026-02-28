package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehouseQuery;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehouseResult;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.LockModeType.*;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return find("archivedAt is null").list().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;

    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    getEntityManager().createQuery(
                    "UPDATE DbWarehouse w SET w.location = :loc, w.capacity = :cap, " +
                            "w.stock = :stock, w.archivedAt = :archived WHERE w.businessUnitCode = :code")
            .setParameter("loc", warehouse.location)
            .setParameter("cap", warehouse.capacity)
            .setParameter("stock", warehouse.stock)
            .setParameter("archived", warehouse.archivedAt)
            .setParameter("code", warehouse.businessUnitCode)
            .executeUpdate();

    // Clear persistence context to see updates in subsequent queries
    getEntityManager().flush();
    getEntityManager().clear();
  }

  @Override
  public void remove(Warehouse warehouse) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }

  @Override
  @Transactional
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }

  // This method is used in the archive operation to ensure we have a lock on the warehouse record before updating itList<Warehouse> warehouses = searchQueryObj.getResultList()
  @Override
  public Warehouse findByBusinessUnitCodeForUpdate(String buCode) {
    DbWarehouse dbWarehouse = getEntityManager()
            .createQuery("SELECT w FROM DbWarehouse w WHERE w.businessUnitCode = :code FOR UPDATE", DbWarehouse.class)
            .setParameter("code", buCode)
            .getSingleResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }

  @Override
  public SearchWarehouseResult search(SearchWarehouseQuery query) {
    String baseQuery = buildBaseQuery(query);
    String countQuery = buildCountQuery(query);
    String orderByClause = buildOrderBy(query);

    Query searchQueryObj = getEntityManager().createQuery(
        baseQuery + orderByClause,
        DbWarehouse.class
    );
    applyQueryParameters(searchQueryObj, query);
    searchQueryObj.setFirstResult(query.getOffset());
    searchQueryObj.setMaxResults(query.getPageSize());

    List<DbWarehouse> dbWarehouses = searchQueryObj.getResultList();

    List<Warehouse> warehouses = dbWarehouses.stream()
            .map(DbWarehouse::toWarehouse)
            .toList();

    // Execute count query
    Query countQueryObj = getEntityManager().createQuery(countQuery, Long.class);
    applyQueryParameters(countQueryObj, query);
    long totalCount = (long) countQueryObj.getSingleResult();

    System.out.println("Search query executed with totalCount: " + totalCount + ", page: " + query.getPage() + ", pageSize: " + query.getPageSize());

    return new SearchWarehouseResult(warehouses, totalCount, query.getPage(), query.getPageSize());
  }

  private String buildBaseQuery(SearchWarehouseQuery query) {
    StringBuilder sql = new StringBuilder("SELECT w FROM DbWarehouse w WHERE w.archivedAt IS NULL");

    if (query.getLocation() != null && !query.getLocation().isEmpty()) {
      sql.append(" AND w.location = :location");
    }
    if (query.getMinCapacity() != null) {
      sql.append(" AND w.capacity >= :minCapacity");
    }
    if (query.getMaxCapacity() != null) {
      sql.append(" AND w.capacity <= :maxCapacity");
    }

    return sql.toString();
  }

  private String buildCountQuery(SearchWarehouseQuery query) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(w) FROM DbWarehouse w WHERE w.archivedAt IS NULL");

    if (query.getLocation() != null && !query.getLocation().isEmpty()) {
      sql.append(" AND w.location = :location");
    }
    if (query.getMinCapacity() != null) {
      sql.append(" AND w.capacity >= :minCapacity");
    }
    if (query.getMaxCapacity() != null) {
      sql.append(" AND w.capacity <= :maxCapacity");
    }

    return sql.toString();
  }

  private String buildOrderBy(SearchWarehouseQuery query) {
    String sortField = "capacity".equals(query.getSortBy()) ? "w.capacity" : "w.createdAt";
    String sortOrder = "desc".equalsIgnoreCase(query.getSortOrder()) ? "DESC" : "ASC";
    return " ORDER BY " + sortField + " " + sortOrder;
  }

  private void applyQueryParameters(Query query, SearchWarehouseQuery searchQuery) {
    if (searchQuery.getLocation() != null && !searchQuery.getLocation().isEmpty()) {
      query.setParameter("location", searchQuery.getLocation());
    }
    if (searchQuery.getMinCapacity() != null) {
      query.setParameter("minCapacity", searchQuery.getMinCapacity());
    }
    if (searchQuery.getMaxCapacity() != null) {
      query.setParameter("maxCapacity", searchQuery.getMaxCapacity());
    }
  }
}
