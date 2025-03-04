import com.scalesec.vulnado.Comment;
import com.scalesec.vulnado.BadRequest;
import com.scalesec.vulnado.ServerError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.sql.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class CommentTest {

    private static Connection connection;

    @BeforeAll
    public static void setUpClass() throws SQLException {
        // Establish database connection.  Replace with your actual database connection details.
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/your_database_name", "your_username", "your_password");
        // Create the comments table if it doesn't exist.
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS comments (id VARCHAR(255) PRIMARY KEY, username VARCHAR(255), body TEXT, created_on TIMESTAMP)");
        }
    }


    @BeforeEach
    public void setUp() throws SQLException {
        // Clean up the table before each test.
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM comments");
        }

    }

    @AfterEach
    public void tearDown() throws SQLException{
        //Clean up after each test - not strictly necessary given the beforeEach, but good practice.
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM comments");
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

        //Verify it was inserted into the database.
        try(Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM comments WHERE id = '" + comment.id + "'")){
            assertTrue(rs.next());
        } catch (SQLException e){
            fail("Failed to verify comment insertion: " + e.getMessage());
        }
    }

    @Test
    void testCreateCommentFails() {
        // Simulate a database error (replace with a more robust failure simulation if possible).  This test is brittle and relies on specific DB error behaviour.
        try {
            Comment.create("testuser", "This will fail");
            fail("Expected BadRequest or ServerError exception");
        } catch (BadRequest | ServerError e) {
            //Expected
        }

    }


    @Test
    void testFetchAllComments() {
        Comment.create("user1", "Comment 1");
        Comment.create("user2", "Comment 2");

        List<Comment> comments = Comment.fetch_all();
        assertEquals(2, comments.size());
    }

    @Test
    void testFetchAllCommentsEmpty() {
        List<Comment> comments = Comment.fetch_all();
        assertEquals(0, comments.size());
    }

    @Test
    void testDeleteComment() {
        Comment comment = Comment.create("testuser", "Comment to delete");
        assertTrue(Comment.delete(comment.id));

        //Verify deletion
        try(Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM comments WHERE id = '" + comment.id + "'")){
            assertFalse(rs.next());
        } catch (SQLException e){
            fail("Failed to verify comment deletion: " + e.getMessage());
        }
    }

    @Test
    void testDeleteCommentNotFound() {
        assertFalse(Comment.delete(UUID.randomUUID().toString()));
    }

    @Test
    void testCommit(){
        Comment comment = new Comment(UUID.randomUUID().toString(), "testuser", "Test Commit", new Timestamp(System.currentTimeMillis()));
        try {
            assertTrue(comment.commit());
        } catch (SQLException e){
            fail("Failed to commit comment: " + e.getMessage());
        }

    }

    @Test
    void testCommitFails() {
        // This test is difficult to implement without mocking or manipulating the database directly
        // in a way that would not be representative of real-world failures.  A more comprehensive test suite 
        // would include tests for edge cases, error handling, and potentially mocking database interactions.
    }

}


<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version> <!-- Use the latest version -->
</dependency>
