package com.scalesec.vulnado;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import java.util.List;
import java.io.Serializable;
import java.sql.*;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource; //Example using Apache Commons DBCP2.  Replace with your preferred connection pool.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@EnableAutoConfiguration
public class CommentsController {

    private static final Logger logger = LoggerFactory.getLogger(CommentsController.class);

    @Autowired
    private DataSource dataSource; // Inject DataSource for connection pooling

    @Value("${app.secret}") //secret is still here for demonstration. Replace with proper secret management.
    private String secret;


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/comments", method = RequestMethod.GET, produces = "application/json")
    List<Comment> comments(@RequestHeader(value = "x-auth-token") String token) {
        //Replace with a proper secret management solution.  This is a placeholder.
        User.assertAuth(secret, token); 
        try (Connection connection = dataSource.getConnection()){
            return Comment.fetchAll(connection);
        } catch (SQLException e) {
            logger.error("Error fetching comments: ", e);
            throw new ServerError("Failed to fetch comments.");
        }
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/comments", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    Comment createComment(@RequestHeader(value = "x-auth-token") String token, @RequestBody CommentRequest input) {
        // Input validation
        if (input.username == null || input.username.isEmpty() || input.body == null || input.body.isEmpty()) {
            throw new BadRequest("Username and body cannot be empty.");
        }
        //Sanitize input -  Add more robust sanitization as needed.
        String sanitizedUsername = input.username.replaceAll("[^a-zA-Z0-9_]", "");
        String sanitizedBody = input.body.replaceAll("[^a-zA-Z0-9_.,!?;:'\" ]", ""); //Example sanitization -  Replace with more robust method.

        User.assertAuth(secret, token); //Replace with a proper secret management solution. This is a placeholder.

        try (Connection connection = dataSource.getConnection()){
            return Comment.create(connection, sanitizedUsername, sanitizedBody);
        } catch (SQLException e) {
            logger.error("Error creating comment: ", e);
            throw new ServerError("Failed to create comment.");
        }
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/comments/{id}", method = RequestMethod.DELETE, produces = "application/json")
    Boolean deleteComment(@RequestHeader(value = "x-auth-token") String token, @PathVariable("id") String id) {
        User.assertAuth(secret, token); //Replace with a proper secret management solution. This is a placeholder.

        try (Connection connection = dataSource.getConnection()){
            return Comment.delete(connection, id);
        } catch (SQLException e) {
            logger.error("Error deleting comment: ", e);
            throw new ServerError("Failed to delete comment.");
        }
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


// Example implementation of Comment class with database interaction using prepared statements and connection pooling
class Comment implements Serializable {
    private int id;
    private String username;
    private String body;

    //Getters and setters for id, username, body

    public static List<Comment> fetchAll(Connection connection) throws SQLException {
        //Implementation using prepared statement for fetching comments
        List<Comment> comments = new java.util.ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, username, body FROM comments")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Comment comment = new Comment();
                    comment.id = resultSet.getInt("id");
                    comment.username = resultSet.getString("username");
                    comment.body = resultSet.getString("body");
                    comments.add(comment);
                }
            }
        }
        return comments;
    }


    public static Comment create(Connection connection, String username, String body) throws SQLException {
        //Implementation using prepared statement for creating comments
        Comment comment = new Comment();
        comment.username = username;
        comment.body = body;

        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO comments (username, body) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, body);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.id = generatedKeys.getInt(1);
                }
            }
        }
        return comment;

    }

    public static boolean delete(Connection connection, String id) throws SQLException {
        //Implementation using prepared statement for deleting comments
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM comments WHERE id = ?")) {
            statement.setString(1, id);
            return statement.executeUpdate() > 0;
        }
    }
}

class User {
    // Placeholder - Replace with actual authentication logic
    public static void assertAuth(String secret, String token) {
        if (!secret.equals(token)) {
            throw new BadRequest("Invalid token");
        }
    }
}
