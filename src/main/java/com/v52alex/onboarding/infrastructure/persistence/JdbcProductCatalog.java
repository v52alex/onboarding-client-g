package com.v52alex.onboarding.infrastructure.persistence;

import com.v52alex.onboarding.domain.OnboardingProduct;
import com.v52alex.onboarding.domain.ProductCatalog;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcProductCatalog implements ProductCatalog {

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

    private OnboardingProduct product(ResultSet rs, int row) throws SQLException {
        return new OnboardingProduct(rs.getString("id"), rs.getString("code"),
            rs.getString("name"), rs.getString("description"));
    }
}
