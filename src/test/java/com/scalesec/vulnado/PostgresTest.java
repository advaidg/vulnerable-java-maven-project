import com.scalesec.vulnado.Postgres;
import com.scalesec.vulnado.Postgres.PostgresConnectionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;


public class PostgresTest {

    private static final Logger logger = LoggerFactory.getLogger(PostgresTest.class);
    private static Connection connection;

    @BeforeAll
    public static void setupDatabase() {
        try {
            Postgres.setup();
            connection = Postgres.connection();
        } catch (PostgresConnectionException e) {
            logger.error("Failed to setup database for tests: ", e);
            fail("Failed to connect to database for tests.");
        }
    }


    @AfterAll
    public static void teardownDatabase() {
        try {
            if (connection != null) {
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("DELETE FROM users");
                stmt.executeUpdate("DELETE FROM comments");
                stmt.close();
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Error during database teardown: ", e);
        }
    }

    @Test
    void testConnection() {
        assertDoesNotThrow(() -> Postgres.connection());
    }


    @Test
    void testMd5() {
        assertEquals("81dc9bdb52d04dc20036dbd8313ed055", Postgres.md5("test"));
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Postgres.md5(""));

    }

    @Test
    void testInsertUser() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            int initialCount = rs.getInt(1);

            Postgres.insertUser("testuser", "TestPassword123");


            rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            assertEquals(initialCount + 1, rs.getInt(1));

             rs = stmt.executeQuery("SELECT * FROM users WHERE username = 'testuser'");
             assertTrue(rs.next());
             assertEquals("TestPassword123", rs.getString("password")); //password will be MD5 hashed
        }
    }

    @Test
    void testInsertComment() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM comments");
            rs.next();
            int initialCount = rs.getInt(1);

            Postgres.insertComment("alice", "This is a test comment.");

            rs = stmt.executeQuery("SELECT COUNT(*) FROM comments");
            rs.next();
            assertEquals(initialCount + 1, rs.getInt(1));
        }
    }


    @Test
    void testSetup() {
        assertDoesNotThrow(Postgres::setup); //This test is mostly about ensuring setup doesn't throw an exception
    }


    @Test
    void testPostgresConnectionException() {
        assertThrows(PostgresConnectionException.class, () -> {
            // Simulate a connection failure - replace with actual failure condition if possible.
            System.clearProperty("PGHOST");
            Postgres.connection();
        });
    }

}
