import com.scalesec.vulnado.BadRequest;
import com.scalesec.vulnado.Comment;
import com.scalesec.vulnado.CommentRequest;
import com.scalesec.vulnado.CommentsController;
import com.scalesec.vulnado.ServerError;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"app.secret=testsecret"})
class CommentsControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DataSource dataSource;

    @Value("${app.secret}")
    private String secret;


    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS comments (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, body TEXT)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM comments");
        }
    }

    @Test
    void testCommentsGet() {
        // Add a comment for testing purposes
        CommentRequest request = new CommentRequest();
        request.username = "testuser";
        request.body = "testbody";
        createComment(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-auth-token", secret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List> response = restTemplate.exchange(
                "http://localhost:" + port + "/comments", HttpMethod.GET, entity, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().size() >= 1); // At least one comment should exist
    }


    @Test
    void testCreateComment() {
        CommentRequest request = new CommentRequest();
        request.username = "testuser2";
        request.body = "testbody2";

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-auth-token", secret);
        HttpEntity<CommentRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Comment> response = restTemplate.exchange(
                "http://localhost:" + port + "/comments", HttpMethod.POST, entity, Comment.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("testuser2", response.getBody().getUsername());
        assertEquals("testbody2", response.getBody().getBody());
    }

    @Test
    void testCreateCommentInvalidInput() {
        CommentRequest request = new CommentRequest();
        request.username = "";
        request.body = "";

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-auth-token", secret);
        HttpEntity<CommentRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/comments", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Username and body cannot be empty"));
    }

    @Test
    void testDeleteComment() throws SQLException {
        // Create a comment to delete
        CommentRequest request = new CommentRequest();
        request.username = "testuser3";
        request.body = "testbody3";
        Comment createdComment = createComment(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-auth-token", secret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Boolean> response = restTemplate.exchange(
                "http://localhost:" + port + "/comments/" + createdComment.getId(), HttpMethod.DELETE, entity, Boolean.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());

        //Verify deletion
        HttpHeaders getHeaders = new HttpHeaders();
        getHeaders.add("x-auth-token", secret);
        HttpEntity<Void> getEntity = new HttpEntity<>(getHeaders);
        ResponseEntity<List> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/comments", HttpMethod.GET, getEntity, List.class);
        assertFalse(getResponse.getBody().stream().anyMatch(c -> c.getId() == createdComment.getId()));

    }

    @Test
    void testUnauthorizedAccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-auth-token", "wrongtoken");
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/comments", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid token"));
    }

    private Comment createComment(CommentRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-auth-token", secret);
        HttpEntity<CommentRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Comment> response = restTemplate.exchange(
                "http://localhost:" + port + "/comments", HttpMethod.POST, entity, Comment.class);
        return response.getBody();
    }
}
