package com.scalesec.vulnado;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
public class PostgresTest {

    private static final String DB_URL = "jdbc:postgresql://%s/%s";
    private static final String DB_DRIVER = "org.postgresql.Driver";

    private Connection connection;

    @BeforeAll
    void setUp() throws SQLException, ClassNotFoundException {
        Class.forName(DB_DRIVER);
        String url = String.format(DB_URL, System.getenv("PGHOST"), System.getenv("PGDATABASE"));
        connection = DriverManager.getConnection(url, System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
    }

    @AfterAll
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testSetupUsers() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM users")) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            assertEquals(5, count);
        }
    }

    @Test
    void testSetupComments() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM comments")) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            assertEquals(2, count);
        }
    }

    @Test
    void testInsertUser() throws SQLException {
        String username = "testuser";
        String password = "TestPassword1!";

        try (PreparedStatement stmt = connection.prepareStatement(Postgres.INSERT_USER_SQL)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, username);
            stmt.setString(3, Postgres.md5(password));
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            assertEquals(1, count);
        }
    }

    @Test
    void testInsertComment() throws SQLException {
        String username = "testuser";
        String body = "This is a test comment";

        try (PreparedStatement stmt = connection.prepareStatement(Postgres.INSERT_COMMENT_SQL)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, username);
            stmt.setString(3, body);
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM comments WHERE body = ?")) {
            stmt.setString(1, body);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            assertEquals(1, count);
        }
    }

    @Test
    void testMd5() {
        String input = "test";
        String expectedHash = "098f6bcd4621d373cade4e832627b4f6";
        String actualHash = Postgres.md5(input);
        assertEquals(expectedHash, actualHash);
    }

    @Test
    void testUniqueUsername() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(Postgres.INSERT_USER_SQL)) {
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, "alice"); // Existing username
            stmt.setString(3, Postgres.md5("NewPassword!"));
            int rowsAffected = stmt.executeUpdate();
            assertEquals(0, rowsAffected); // Should not insert due to unique constraint
        }
    }

    @Test
    void testDatabaseExists() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT datname FROM pg_database WHERE datname = '" + System.getenv("PGDATABASE") + "'");
            assertTrue(rs.next());
            assertEquals(System.getenv("PGDATABASE"), rs.getString("datname"));
        }
    }

    @Test
    void testTableExists() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name = 'users' AND table_schema = 'public')");
            assertTrue(rs.next());
            assertTrue(rs.getBoolean(1));
        }
    }

    @Test
    void testTableColumns() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users'");
            int count = 0;
            while (rs.next()) {
                count++;
            }
            assertEquals(6, count);
        }
    }
}
