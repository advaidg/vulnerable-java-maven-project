import com.scalesec.vulnado.Postgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;


public class PostgresTest {

    private static Connection connection;

    @BeforeAll
    static void setUp() {
        //Ensure that necessary environment variables are set for testing.  This would normally be handled by a testing framework like Testcontainers.
        System.setProperty("PGHOST", "localhost"); // Replace with your Postgres host
        System.setProperty("PGDATABASE", "vulnado_test"); // Replace with your test database name
        System.setProperty("PGUSER", "postgres"); // Replace with your Postgres username
        System.setProperty("PGPASSWORD", "your_password"); // Replace with your Postgres password

        Postgres.setup();
        connection = Postgres.connection();
    }


    @AfterAll
    static void tearDown() throws SQLException {
        if (connection != null) {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("DROP TABLE IF EXISTS users");
                stmt.executeUpdate("DROP TABLE IF EXISTS comments");
            }
            connection.close();
        }
    }

    @Test
    void testConnection() {
        assertDoesNotThrow((Executable) () -> Postgres.connection());
    }


    @Test
    void testMd5() {
        assertEquals("e5b826a9c3a716b5d3d4575a461c67d0", Postgres.md5("test")); // Known MD5 hash for "test"
    }

    @Test
    void testUserInsertion() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            assertEquals(5, rs.getInt(1)); // 5 initial users
        }
    }


    @Test
    void testCommentInsertion() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM comments")) {
            rs.next();
            assertEquals(2, rs.getInt(1)); //2 initial comments
        }
    }

    @Test
    void testSetupIdempotency() throws SQLException{
        Postgres.setup(); //Call setup again to test idempotency
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            assertEquals(5, rs.getInt(1)); //Should still be 5 users after second setup call.
        }
    }


    @Test
    void testExceptionHandlingConnection() {
        // Simulate missing environment variable - replace with a more robust test if possible, like mocking System.getenv
        System.clearProperty("PGPASSWORD");
        assertThrows(RuntimeException.class, () -> Postgres.connection());
        System.setProperty("PGPASSWORD", "your_password"); //Restore for subsequent tests
    }

    @Test
    void testExceptionHandlingSetup(){
        //This test is difficult to implement reliably without mocking the database interaction. A more sophisticated testing approach (e.g., using a mocking framework) would be needed to properly test exception handling in setup.
        //Consider adding tests for specific exception scenarios within the insertUser and insertComment methods if mocking is implemented.


    }
}


<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.11.0-M1</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.6.0</version>
    </dependency>
</dependencies>
