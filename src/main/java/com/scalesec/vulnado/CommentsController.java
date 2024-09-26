package com.scalesec.vulnado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;

@RestController
@EnableAutoConfiguration
public class CommentsController {

    @Value("${app.secret}")
    private String secret;

    @Autowired
    private CommentService commentService;

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/comments", method = RequestMethod.GET, produces = "application/json")
    List<Comment> comments() {
        // Authentication is handled by a filter or interceptor
        // User.assertAuth(secret, token); // Authentication removed, moved to filter or interceptor
        return commentService.fetchAll();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/comments", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    Comment createComment(@RequestBody CommentRequest input) {
        // Authentication is handled by a filter or interceptor
        // User.assertAuth(secret, token); // Authentication removed, moved to filter or interceptor
        return commentService.create(input.username, input.body);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/comments/{id}", method = RequestMethod.DELETE, produces = "application/json")
    Boolean deleteComment(@PathVariable("id") String id) {
        // Authentication is handled by a filter or interceptor
        // User.assertAuth(secret, token); // Authentication removed, moved to filter or interceptor
        return commentService.delete(id);
    }
}

class CommentRequest implements Serializable {
    public String username;
    public String body;
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequest extends RuntimeException {
    public BadRequest(String exception) {
        super(exception);
    }
}

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class ServerError extends RuntimeException {
    public ServerError(String exception) {
        super(exception);
    }
}

// Service layer for Comment operations
interface CommentService {
    List<Comment> fetchAll();
    Comment create(String username, String body);
    Boolean delete(String id);
}

// Implementation of CommentService using JDBC
class CommentServiceImpl implements CommentService {

    @Value("${database.url}")
    private String databaseUrl;

    @Value("${database.username}")
    private String databaseUsername;

    @Value("${database.password}")
    private String databasePassword;

    @Override
    public List<Comment> fetchAll() {
        // Implement fetching all comments using JDBC with proper connection handling
        // Use pagination or filtering to optimize retrieval for large datasets
        // Example:
        try (Connection connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM comments");
             ResultSet resultSet = statement.executeQuery()) {
            List<Comment> comments = new ArrayList<>();
            while (resultSet.next()) {
                comments.add(new Comment(resultSet.getString("id"), resultSet.getString("username"), resultSet.getString("body")));
            }
            return comments;
        } catch (SQLException e) {
            // Handle SQL exception properly
            throw new ServerError("Error fetching comments: " + e.getMessage());
        }
    }

    @Override
    public Comment create(String username, String body) {
        // Implement comment creation using JDBC with proper connection handling
        // Validate inputs to prevent unexpected behavior
        // Example:
        try (Connection connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO comments (username, body) VALUES (?, ?)")) {
            statement.setString(1, username);
            statement.setString(2, body);
            statement.executeUpdate();
            // Get the generated comment ID
            // ...
            return new Comment(commentId, username, body);
        } catch (SQLException e) {
            // Handle SQL exception properly
            throw new ServerError("Error creating comment: " + e.getMessage());
        }
    }

    @Override
    public Boolean delete(String id) {
        // Implement comment deletion using JDBC with proper connection handling
        // Example:
        try (Connection connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM comments WHERE id = ?")) {
            statement.setString(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            // Handle SQL exception properly
            throw new ServerError("Error deleting comment: " + e.getMessage());
        }
    }
}

// Authentication filter or interceptor
class AuthenticationFilter implements Filter {
    @Value("${app.secret}")
    private String secret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("x-auth-token");

        if (token == null || !User.assertAuth(secret, token)) {
            // Handle unauthorized access (e.g., send 401 Unauthorized response)
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }
}
