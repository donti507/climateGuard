package com.climateguard.api.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayInfo;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class DatabaseFlywayHealthIndicatorTest {

    private DataSource dataSource;
    private Connection connection;
    private Flyway flyway;
    private FlywayInfo flywayInfo;
    private DatabaseFlywayHealthIndicator indicator;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        flyway = mock(Flyway.class);
        flywayInfo = mock(FlywayInfo.class);
        when(flyway.info()).thenReturn(flywayInfo);

        indicator = new DatabaseFlywayHealthIndicator(dataSource, flyway);
    }

    @Test
    void reportsUpWhenDatabaseIsAvailableAndNoMigrationsArePending() throws SQLException {
        when(connection.isValid(2)).thenReturn(true);

        MigrationInfo current = mock(MigrationInfo.class);
        when(current.getVersion()).thenReturn("5");
        when(current.getDescription()).thenReturn("create flood events");
        when(current.getState()).thenReturn(MigrationState.SUCCESS);
        when(flywayInfo.current()).thenReturn(current);
        when(flywayInfo.pending()).thenReturn(new MigrationInfo[0]);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("database", "UP")
                .containsEntry("flyway", "UP")
                .containsEntry("flywayCurrentVersion", "5")
                .containsEntry("flywayCurrentDescription", "create flood events")
                .containsEntry("flywayCurrentState", "SUCCESS")
                .containsEntry("flywayPendingCount", 0);
        List<?> pending = (List<?>) health.getDetails().get("flywayPendingMigrations");
        assertThat(pending).isEmpty();
    }

    @Test
    void reportsDownWhenDatabaseConnectionFails() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("connection refused"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("database", "DOWN")
                .containsEntry("databaseError", "connection refused")
                .containsEntry("flyway", "UNKNOWN");
    }

    @Test
    void reportsDownWhenDatabaseConnectionIsInvalid() throws SQLException {
        when(connection.isValid(2)).thenReturn(false);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("database", "DOWN")
                .containsEntry("databaseError", "Connection is not valid")
                .containsEntry("flyway", "UNKNOWN");
    }

    @Test
    void reportsDownWhenFlywayInfoCannotBeLoaded() throws SQLException {
        when(connection.isValid(2)).thenReturn(true);
        when(flyway.info()).thenThrow(new IllegalStateException("flyway metadata unavailable"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("database", "UP")
                .containsEntry("flyway", "DOWN")
                .containsEntry("flywayError", "flyway metadata unavailable");
    }

    @Test
    void reportsDownWhenLastMigrationFailed() throws SQLException {
        when(connection.isValid(2)).thenReturn(true);

        MigrationInfo current = mock(MigrationInfo.class);
        when(current.getVersion()).thenReturn("3");
        when(current.getDescription()).thenReturn("add users");
        when(current.getState()).thenReturn(MigrationState.FAILED);
        when(flywayInfo.current()).thenReturn(current);
        when(flywayInfo.pending()).thenReturn(new MigrationInfo[0]);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("database", "UP")
                .containsEntry("flyway", "DOWN")
                .containsEntry("flywayError", "Last migration failed: 3:add users");
    }

    @Test
    void reportsOutOfServiceWhenMigrationsArePending() throws SQLException {
        when(connection.isValid(2)).thenReturn(true);

        MigrationInfo pending = mock(MigrationInfo.class);
        when(pending.getVersion()).thenReturn("6");
        when(pending.getDescription()).thenReturn("add observation stats");
        when(flywayInfo.current()).thenReturn(null);
        when(flywayInfo.pending()).thenReturn(new MigrationInfo[] { pending });

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(health.getDetails())
                .containsEntry("database", "UP")
                .containsEntry("flyway", "OUT_OF_SERVICE")
                .containsEntry("flywayPendingCount", 1);
        List<?> pendingMigrations = (List<?>) health.getDetails().get("flywayPendingMigrations");
        assertThat(pendingMigrations).containsExactly("6:add observation stats");
    }

    @Test
    void reportsRepeatablePendingMigrationWithoutVersion() throws SQLException {
        when(connection.isValid(2)).thenReturn(true);

        MigrationInfo pending = mock(MigrationInfo.class);
        when(pending.getVersion()).thenReturn(null);
        when(pending.getDescription()).thenReturn("repeatable sql");
        when(flywayInfo.current()).thenReturn(null);
        when(flywayInfo.pending()).thenReturn(new MigrationInfo[] { pending });

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        List<?> pendingMigrations = (List<?>) health.getDetails().get("flywayPendingMigrations");
        assertThat(pendingMigrations).containsExactly("repeatable:repeatable sql");
    }
}
