package com.v52alex.onboarding.infrastructure.persistence;

import com.v52alex.onboarding.domain.OnboardingProduct;
import com.v52alex.onboarding.domain.ProductCatalog;
import com.v52alex.onboarding.domain.ManagedProduct;
import com.v52alex.onboarding.domain.ProductCatalogAdmin;
import com.v52alex.onboarding.application.ProductNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcProductCatalog implements ProductCatalog, ProductCatalogAdmin {

    private final JdbcTemplate jdbc;

    JdbcProductCatalog(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<OnboardingProduct> availableProducts() {
        return jdbc.query("""
                select id, code, name, description
                  from onboarding_product
                 where active = true
                 order by display_order, id
                """, this::product);
    }

    @Override
    public List<ManagedProduct> allProducts() {
        return jdbc.query("""
                select id, code, name, description, active, display_order
                  from onboarding_product
                 order by display_order, id
                """, this::managedProduct);
    }

    @Override
    public ManagedProduct create(String id, String code, String name, String description,
        boolean active, int displayOrder) {
        jdbc.update("""
            insert into onboarding_product
                (id, code, name, description, active, display_order, created_at, updated_at)
            values (?, ?, ?, ?, ?, ?, current_timestamp, current_timestamp)
            """, id, code, name, description, active, displayOrder);
        return find(id);
    }

    @Override
    public ManagedProduct update(String id, String code, String name, String description,
        boolean active, int displayOrder) {
        jdbc.update("""
            update onboarding_product
               set code = ?, name = ?, description = ?, active = ?, display_order = ?,
                   updated_at = current_timestamp
             where id = ?
            """, code, name, description, active, displayOrder, id);
        return find(id);
    }

    @Override
    public ManagedProduct updateStatus(String id, boolean active) {
        jdbc.update("""
            update onboarding_product
               set active = ?, updated_at = current_timestamp
             where id = ?
            """, active, id);
        return find(id);
    }

    private OnboardingProduct product(ResultSet rs, int row) throws SQLException {
        return new OnboardingProduct(rs.getString("id"), rs.getString("code"),
            rs.getString("name"), rs.getString("description"));
    }

    private ManagedProduct managedProduct(ResultSet rs, int row) throws SQLException {
        return new ManagedProduct(rs.getString("id"), rs.getString("code"),
            rs.getString("name"), rs.getString("description"), rs.getBoolean("active"),
            rs.getInt("display_order"));
    }

    private ManagedProduct find(String id) {
        try {
            return jdbc.queryForObject("""
                select id, code, name, description, active, display_order
                  from onboarding_product where id = ?
                """, this::managedProduct, id);
        } catch (org.springframework.dao.EmptyResultDataAccessException exception) {
            throw new ProductNotFoundException(id);
        }
    }
}
