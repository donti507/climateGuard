package com.climateguard.api.health;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayInfo;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFlywayHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final Flyway flyway;

    public DatabaseFlywayHealthIndicator(DataSource dataSource, Flyway flyway) {
        this.dataSource = dataSource;
        this.flyway = flyway;
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.status(Status.UNKNOWN);

        Status databaseStatus = addDatabaseDetails(builder);
        Status flywayStatus;
        if (databaseStatus == Status.UP) {
            flywayStatus = addFlywayDetails(builder);
        } else {
            builder.withDetail("flyway", Status.UNKNOWN.getCode());
            flywayStatus = Status.UNKNOWN;
        }

        builder.status(combine(databaseStatus, flywayStatus));
        return builder.build();
    }

    private Status addDatabaseDetails(Health.Builder builder) {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            if (valid) {
                builder.withDetail("database", Status.UP.getCode());
                return Status.UP;
            }

            builder.withDetail("database", Status.DOWN.getCode());
            builder.withDetail("databaseError", "Connection is not valid");
            return Status.DOWN;
        } catch (SQLException ex) {
            builder.withDetail("database", Status.DOWN.getCode());
            builder.withDetail("databaseError", errorMessage(ex));
            return Status.DOWN;
        }
    }

    private Status addFlywayDetails(Health.Builder builder) {
        try {
            FlywayInfo info = flyway.info();
            if (info == null) {
                builder.withDetail("flyway", Status.DOWN.getCode());
                builder.withDetail("flywayError", "Flyway info is null");
                return Status.DOWN;
            }

            MigrationInfo current = info.current();
            MigrationInfo[] pending = info.pending();
            if (pending == null) {
                pending = new MigrationInfo[0];
            }

            if (current != null) {
                builder.withDetail("flywayCurrentVersion", current.getVersion() == null ? "none" : current.getVersion());
                builder.withDetail("flywayCurrentDescription", current.getDescription() == null ? "none" : current.getDescription());
                builder.withDetail("flywayCurrentState", current.getState() == null ? "none" : current.getState().name());
            } else {
                builder.withDetail("flywayCurrentVersion", "none");
                builder.withDetail("flywayCurrentDescription", "none");
                builder.withDetail("flywayCurrentState", "none");
            }

            if (current != null && current.getState() == MigrationState.FAILED) {
                builder.withDetail("flyway", Status.DOWN.getCode());
                builder.withDetail("flywayError", "Last migration failed: " + describe(current));
                return Status.DOWN;
            }

            List<String> pendingMigrations = new ArrayList<>();
            for (MigrationInfo migration : pending) {
                pendingMigrations.add(describe(migration));
            }

            builder.withDetail("flywayPendingCount", pending.length);
            builder.withDetail("flywayPendingMigrations", pendingMigrations);

            if (pending.length > 0) {
                builder.withDetail("flyway", Status.OUT_OF_SERVICE.getCode());
                return Status.OUT_OF_SERVICE;
            }

            builder.withDetail("flyway", Status.UP.getCode());
            return Status.UP;
        } catch (Exception ex) {
            builder.withDetail("flyway", Status.DOWN.getCode());
            builder.withDetail("flywayError", errorMessage(ex));
            return Status.DOWN;
        }
    }

    private String describe(MigrationInfo migration) {
        if (migration == null) {
            return "unknown";
        }

        String version = migration.getVersion() == null ? "repeatable" : migration.getVersion();
        String description = migration.getDescription() == null ? "unknown" : migration.getDescription();
        return version + ":" + description;
    }

    private String errorMessage(Throwable ex) {
        return ex.getMessage() == null ? ex.toString() : ex.getMessage();
    }

    private Status combine(Status databaseStatus, Status flywayStatus) {
        if (databaseStatus == Status.DOWN || flywayStatus == Status.DOWN) {
            return Status.DOWN;
        }
        if (databaseStatus == Status.OUT_OF_SERVICE || flywayStatus == Status.OUT_OF_SERVICE) {
            return Status.OUT_OF_SERVICE;
        }
        if (databaseStatus == Status.UP && flywayStatus == Status.UP) {
            return Status.UP;
        }
        return Status.UNKNOWN;
    }
}
