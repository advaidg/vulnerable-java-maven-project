import com.scalesec.vulnado.BadRequest;
import com.scalesec.vulnado.Comment;
import com.scalesec.vulnado.CommentRequest;
import com.scalesec.vulnado.CommentsController;
import com.scalesec.vulnado.ServerError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentsControllerTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private HttpSession mockHttpSession;


    @InjectMocks
    private CommentsController commentsController;

    @BeforeEach
    void setUp() {
        //Set up mocks for database interaction as needed in individual tests. 
        //Example: when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        //This example assumes that the CommentsController is being injected with the mocks.  Adjust this based on your testing framework.
        commentsController = new CommentsController("testSecret"); // Replace "testSecret" with a valid secret for testing

    }

    @Test
    void testComments_validToken() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(Comment.fetchAll(mockResultSet)).thenReturn(new ArrayList<>()); // Return an empty list for simplicity

        List<Comment> result = commentsController.comments("validToken", mockHttpSession);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockStatement, times(1)).executeQuery();
    }


    @Test
    void testComments_invalidToken() {
        assertThrows(BadRequest.class, () -> commentsController.comments("invalidToken", mockHttpSession));
    }

    @Test
    void testComments_databaseError() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        assertThrows(ServerError.class, () -> commentsController.comments("validToken", mockHttpSession));
    }

    @Test
    void testCreateComment_validInput() throws SQLException {
        CommentRequest input = new CommentRequest();
        input.username = "testuser";
        input.body = "test comment";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(Comment.create(anyString(), anyString())).thenReturn(new Comment("testuser", "test comment", 1L)); // Simulate ID generation

        Comment result = commentsController.createComment("validToken", input, mockHttpSession);
        assertNotNull(result);
        assertEquals("testuser", result.username);
        assertEquals("test comment", result.body);
        verify(mockStatement, times(1)).executeUpdate();

    }

    @Test
    void testCreateComment_invalidInput() {
        CommentRequest input = new CommentRequest();
        input.username = "";
        input.body = "";

        assertThrows(BadRequest.class, () -> commentsController.createComment("validToken", input, mockHttpSession));
    }

    @Test
    void testCreateComment_databaseError() throws SQLException {
        CommentRequest input = new CommentRequest();
        input.username = "testuser";
        input.body = "test comment";

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        assertThrows(ServerError.class, () -> commentsController.createComment("validToken", input, mockHttpSession));
    }

    @Test
    void testDeleteComment_success() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = commentsController.deleteComment("validToken", "1", mockHttpSession);
        assertTrue(result);
        verify(mockStatement, times(1)).executeUpdate();
    }

    @Test
    void testDeleteComment_notFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0);

        boolean result = commentsController.deleteComment("validToken", "1", mockHttpSession);
        assertFalse(result);
        verify(mockStatement, times(1)).executeUpdate();
    }


    @Test
    void testDeleteComment_databaseError() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        assertThrows(ServerError.class, () -> commentsController.deleteComment("validToken", "1", mockHttpSession));
    }

    @Test
    void testValidateCommentRequest_validInput() {
        CommentRequest input = new CommentRequest();
        input.username = "testuser";
        input.body = "test comment";
        assertDoesNotThrow(() -> commentsController.validateCommentRequest(input));
    }

    @Test
    void testValidateCommentRequest_invalidInput() {
        CommentRequest input = new CommentRequest();
        input.username = "";
        input.body = "";
        assertThrows(BadRequest.class, () -> commentsController.validateCommentRequest(input));

        input.username = "testuser";
        input.body = "a".repeat(1025); // Exceeding max length
        assertThrows(BadRequest.class, () -> commentsController.validateCommentRequest(input));
    }


}
