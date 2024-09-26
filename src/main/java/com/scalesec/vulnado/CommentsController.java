package com.scalesec.vulnado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import java.util.List;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RestController
@EnableAutoConfiguration
public class CommentsController {

    @Value("${app.secret}")
    private String secret;

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/comments", method = RequestMethod.GET, produces = "application/json")
    public List<Comment> comments(@RequestHeader(value = "x-auth-token") String token) {
        User.assertAuth(secret, token);
        return Comment.fetchAll();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/comments", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public Comment createComment(@RequestHeader(value = "x-auth-token") String token, @RequestBody CommentRequest input) {
        User.assertAuth(secret, token);
        return Comment.create(input.getUsername(), input.getBody());
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/comments/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public Boolean deleteComment(@RequestHeader(value = "x-auth-token") String token, @PathVariable("id") String id) {
        User.assertAuth(secret, token);
        return Comment.delete(id);
    }
}

class CommentRequest implements Serializable {
    private String username;
    private String body;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
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

class Comment implements Serializable {
    private String id;
    private String username;
    private String body;

    public Comment(String id, String username, String body) {
        this.id = id;
        this.username = username;
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getBody() {
        return body;
    }

    public static List<Comment> fetchAll() {
        List<Comment> comments = new java.util.ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydatabase", "user", "password")) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM comments")) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    comments.add(new Comment(resultSet.getString("id"), resultSet.getString("username"), resultSet.getString("body")));
                }
            } catch (SQLException e) {
                throw new ServerError("Error fetching comments: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new ServerError("Error connecting to database: " + e.getMessage());
        }
        return comments;
    }

    public static Comment create(String username, String body) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydatabase", "user", "password")) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO comments (username, body) VALUES (?, ?)")) {
                statement.setString(1, username);
                statement.setString(2, body);
                statement.executeUpdate();
                // Get the generated ID
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return new Comment(generatedKeys.getString(1), username, body);
                    } else {
                        throw new ServerError("Error creating comment: Could not get generated ID.");
                    }
                }
            } catch (SQLException e) {
                throw new ServerError("Error creating comment: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new ServerError("Error connecting to database: " + e.getMessage());
        }
    }

    public static Boolean delete(String id) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydatabase", "user", "password")) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM comments WHERE id = ?")) {
                statement.setString(1, id);
                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException e) {
                throw new ServerError("Error deleting comment: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new ServerError("Error connecting to database: " + e.getMessage());
        }
    }
}

class User {
    // Placeholder for authentication logic - implement securely
    public static void assertAuth(String secret, String token) {
        // Implement authentication using a secure method (e.g., JWT)
        if (!token.equals("valid_token")) {
            throw new BadRequest("Invalid authentication token");
        }
    }
}
