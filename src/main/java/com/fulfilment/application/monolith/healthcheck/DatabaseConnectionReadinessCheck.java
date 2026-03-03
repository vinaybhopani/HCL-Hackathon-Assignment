package com.fulfilment.application.monolith.healthcheck;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

@Readiness
@ApplicationScoped
public class DatabaseConnectionReadinessCheck implements HealthCheck {

    @Inject
    DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("SELECT 1");

            return HealthCheckResponse.up("Database Query Check");
        } catch (SQLException e) {
            return HealthCheckResponse.named("Database Query Check")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}

