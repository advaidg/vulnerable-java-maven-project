import com.scalesec.vulnado.BadRequest;
import com.scalesec.vulnado.Comment;
import com.scalesec.vulnado.ServerError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


//Dummy Postgres class for testing purposes. Replace with a proper mocking or test database setup in a real application.
class Postgres {
    static java.sql.Connection connection() throws SQLException {
        //In a real application, replace this with your database connection logic.  This is a placeholder.
        return null;
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(TemporaryPostgresExtension.class) // Requires a custom extension to manage a temporary test database.  See below.
public class CommentTest {

    @BeforeAll
    void setup() throws SQLException {
        // Create the comments table if it doesn't exist.  This should be in a setup method or handled by the test database extension.
        try (java.sql.Connection connection = Postgres.connection();
             java.sql.Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS comments (id VARCHAR(255) PRIMARY KEY, username VARCHAR(255), body TEXT, created_on TIMESTAMP)");
        }
    }


    @Test
    void testCreateComment() {
        Comment comment = Comment.create("testuser", "This is a test comment.");
        assertNotNull(comment);
        assertNotNull(comment.id);
        assertEquals("testuser", comment.username);
        assertEquals("This is a test comment.", comment.body);
        assertNotNull(comment.created_on);
    }

    @Test
    void testCreateCommentBadRequest() {
        //Simulate a scenario where commit() fails - needs a mock or test DB setup to reliably test this.
        assertThrows(BadRequest.class, () -> Comment.create("testuser", "")); //Empty body should ideally trigger a BadRequest (or similar validation).
    }


    @Test
    void testCreateCommentServerError() {
        //Simulate a database error - needs a mock or test DB setup to reliably test this.
        assertThrows(ServerError.class, () -> Comment.create("testuser", "This should cause a database error.")); // Needs a way to force a SQLException.
    }


    @Test
    void testFetchAllComments() {
        Comment.create("user1", "Comment 1");
        Comment.create("user2", "Comment 2");
        List<Comment> comments = Comment.fetchAll();
        assertEquals(2, comments.size());
    }

    @Test
    void testFetchAllCommentsEmpty() {
        List<Comment> comments = Comment.fetchAll();
        assertTrue(comments.isEmpty());
    }


    @Test
    void testDeleteComment() {
        Comment comment = Comment.create("user3", "Comment to delete");
        assertTrue(Comment.delete(comment.id));
        List<Comment> comments = Comment.fetchAll();
        assertFalse(comments.stream().anyMatch(c -> c.id.equals(comment.id)));
    }

    @Test
    void testDeleteCommentNotFound() {
        assertFalse(Comment.delete("nonexistent-id"));
    }

    // Add more test cases for edge cases and error handling.  For example:
    // - Test with very long usernames or bodies to check for input validation (if any).
    // - Test deleting a comment that doesn't exist.
    // - Test creating a comment with special characters.


}


// Custom JUnit 5 extension to manage a temporary PostgreSQL database for testing.

// This is a placeholder;  you'll need to implement this using a library like Testcontainers.
// Testcontainers simplifies the process of setting up and managing test dependencies like databases.

class TemporaryPostgresExtension implements org.junit.jupiter.api.extension.Extension {
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // Use Testcontainers or a similar library to start a temporary PostgreSQL instance here.
        //  The Postgres class should then use this instance for its connection() method.
        System.out.println("Setting up temporary Postgres database -  Implementation required using Testcontainers.");
        // Example using Testcontainers (replace with actual implementation):
        //  PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13").withDatabaseName("testdb").withUsername("testuser").withPassword("testpass");
        //  postgreSQLContainer.start();
        //  Postgres.setContainer(postgreSQLContainer); // Assuming a setter method is added to the Postgres class

    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // Stop the temporary PostgreSQL instance here.
        System.out.println("Tearing down temporary Postgres database - Implementation required using Testcontainers.");
        // Example using Testcontainers:
        //Postgres.getContainer().stop(); // Assuming a getter method is added to the Postgres class.
    }
}
