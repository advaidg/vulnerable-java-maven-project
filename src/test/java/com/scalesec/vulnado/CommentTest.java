package com.scalesec.vulnado;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @BeforeEach
    void setUp() {
        // Initialize the database or any necessary setup before each test
    }

    @AfterEach
    void tearDown() {
        // Clean up the database or any necessary teardown after each test
    }

    @Test
    void testCreate() {
        // Arrange
        String username = "testUser";
        String body = "This is a test comment.";
        // Act
        Comment comment = Comment.create(username, body);
        // Assert
        assertNotNull(comment);
        assertEquals(username, comment.getUsername());
        assertEquals(body, comment.getBody());
        assertNotNull(comment.getCreatedOn());
    }

    @Test
    void testCreate_ThrowsBadRequest_WhenUnableToSaveComment() {
        // Arrange
        // Simulate a scenario where saving the comment fails in the database
        // (e.g., by mocking the Postgres.connection() method or manipulating the database directly)
        // Act & Assert
        assertThrows(BadRequest.class, () -> Comment.create("testUser", "Test comment"));
    }

    @Test
    void testFetchAll() {
        // Arrange
        // Insert some test comments into the database
        Comment comment1 = Comment.create("user1", "Comment 1");
        Comment comment2 = Comment.create("user2", "Comment 2");
        // Act
        List<Comment> comments = Comment.fetchAll();
        // Assert
        assertEquals(2, comments.size());
        assertTrue(comments.contains(comment1));
        assertTrue(comments.contains(comment2));
    }

    @Test
    void testFetchAll_ReturnsEmptyList_WhenNoCommentsFound() {
        // Arrange
        // Ensure there are no comments in the database
        // (e.g., by clearing the database before the test)
        // Act
        List<Comment> comments = Comment.fetchAll();
        // Assert
        assertTrue(comments.isEmpty());
    }

    @Test
    void testDelete() {
        // Arrange
        Comment comment = Comment.create("testUser", "Test comment");
        // Act
        boolean deleted = Comment.delete(comment.getId());
        // Assert
        assertTrue(deleted);
        // Verify that the comment is no longer present in the database
        List<Comment> comments = Comment.fetchAll();
        assertFalse(comments.contains(comment));
    }

    @Test
    void testDelete_ReturnsFalse_WhenCommentNotFound() {
        // Arrange
        // Ensure that a comment with the given ID does not exist in the database
        // Act
        boolean deleted = Comment.delete("non-existent-id");
        // Assert
        assertFalse(deleted);
    }

    @Test
    void testCommit() {
        // Arrange
        Comment comment = new Comment("testId", "testUser", "Test comment", new Timestamp(new Date().getTime()));
        // Act
        try {
            boolean committed = comment.commit();
            // Assert
            assertTrue(committed);
            // Verify that the comment is present in the database
            List<Comment> comments = Comment.fetchAll();
            assertTrue(comments.contains(comment));
        } catch (SQLException e) {
            fail("Unexpected SQLException: " + e.getMessage());
        }
    }

    @Test
    void testCommit_ThrowsSQLException_WhenCommitFails() {
        // Arrange
        // Simulate a scenario where the commit operation fails in the database
        // (e.g., by mocking the Postgres.connection() method or manipulating the database directly)
        Comment comment = new Comment("testId", "testUser", "Test comment", new Timestamp(new Date().getTime()));
        // Act & Assert
        assertThrows(SQLException.class, comment::commit);
    }
}
