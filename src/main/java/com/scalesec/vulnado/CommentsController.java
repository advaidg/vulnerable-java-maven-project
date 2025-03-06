package com.scalesec.vulnado;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import java.util.List;
import java.io.Serializable;
import java.util.UUID;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@EnableAutoConfiguration
public class CommentsController {

    private static final Logger logger = LoggerFactory.getLogger(CommentsController.class);
    @Value("${database.url}")
    private String databaseUrl;
    @Value("${database.username}")
    private String databaseUsername;
    @Value("${database.password}")
    private String databasePassword;

    private final String APP_SECRET;

    public CommentsController(@Value("${app.secret}") String appSecret) {
        this.APP_SECRET = appSecret; //Store in a more secure way (Environment variable recommended)
    }


    @CrossOrigin(origins = {"allowedOrigin1", "allowedOrigin2"}) // Replace with allowed origins
    @RequestMapping(value = "/comments", method = RequestMethod.GET, produces = "application/json")
    List<Comment> comments(@RequestHeader(value="x-auth-token") String token, HttpSession session) {
        String secret = System.getenv("APP_SECRET"); // Retrieve from environment variable
        if (secret == null || secret.isEmpty()) {
            secret = APP_SECRET; // Fallback to @Value if environment variable is not set (for local development).  Remove this in production
        }
        User.assertAuth(secret, token);
        try (BasicDataSource dataSource = new BasicDataSource()) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver"); // Replace with your JDBC driver
            dataSource.setUrl(databaseUrl);
            dataSource.setUsername(databaseUsername);
            dataSource.setPassword(databasePassword);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM comments")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    return Comment.fetchAll(resultSet);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error fetching comments: ", e);
            throw new ServerError("Failed to fetch comments");
        }
    }

    @CrossOrigin(origins = {"allowedOrigin1", "allowedOrigin2"}) // Replace with allowed origins
    @RequestMapping(value = "/comments", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    Comment createComment(@RequestHeader(value="x-auth-token") String token, @RequestBody CommentRequest input, HttpSession session) {
        String secret = System.getenv("APP_SECRET");
        if (secret == null || secret.isEmpty()) {
            secret = APP_SECRET; // Fallback to @Value (for local development). Remove this in production.
        }
        User.assertAuth(secret, token);
        //Input Validation
        validateCommentRequest(input);

        try (BasicDataSource dataSource = new BasicDataSource()) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver"); // Replace with your JDBC driver
            dataSource.setUrl(databaseUrl);
            dataSource.setUsername(databaseUsername);
            dataSource.setPassword(databasePassword);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO comments (username, body) VALUES (?, ?)")) {
                statement.setString(1, input.username);
                statement.setString(2, input.body);
                statement.executeUpdate();
                //Retrieve the newly inserted ID (implementation depends on your database)
                return Comment.create(input.username, input.body); 
            }
        } catch (SQLException e) {
            logger.error("Database error creating comment: ", e);
            throw new ServerError("Failed to create comment");
        }

    }

    @CrossOrigin(origins = {"allowedOrigin1", "allowedOrigin2"}) // Replace with allowed origins
    @RequestMapping(value = "/comments/{id}", method = RequestMethod.DELETE, produces = "application/json")
    Boolean deleteComment(@RequestHeader(value="x-auth-token") String token, @PathVariable("id") String id, HttpSession session) {
        String secret = System.getenv("APP_SECRET");
        if (secret == null || secret.isEmpty()) {
            secret = APP_SECRET; // Fallback to @Value (for local development). Remove this in production.
        }
        User.assertAuth(secret, token);
        try (BasicDataSource dataSource = new BasicDataSource()) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver"); // Replace with your JDBC driver
            dataSource.setUrl(databaseUrl);
            dataSource.setUsername(databaseUsername);
            dataSource.setPassword(databasePassword);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM comments WHERE id = ?")) {
                statement.setString(1, id);
                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            logger.error("Database error deleting comment: ", e);
            throw new ServerError("Failed to delete comment");
        }
    }

    private void validateCommentRequest(CommentRequest input) {
        if (input == null || input.username == null || input.username.isEmpty() || input.body == null || input.body.isEmpty()) {
            throw new BadRequest("Username and body cannot be empty.");
        }
        // Add more validation as needed (length checks, etc.)  
        if (input.username.length() > 255 || input.body.length() > 1024) { //Example length check
            throw new BadRequest("Username and body exceed maximum length.");
        }

    }
}

class CommentRequest implements Serializable {
    public String username;
    public String body;
}

//Keep BadRequest and ServerError classes as-is

class User {
    public static void assertAuth(String secret, String token) {
        //Implement robust authentication logic here.  This is a placeholder
        if (!token.equals("validToken")) {
            throw new BadRequest("Authentication failed");
        }

    }
}

class Comment implements Serializable {
    private String username;
    private String body;
    private long id; // Add an ID field

    public Comment(String username, String body, long id) {
        this.username = username;
        this.body = body;
        this.id = id;
    }

    public static List<Comment> fetchAll(ResultSet rs) throws SQLException {
        //Implementation to fetch all comments from ResultSet
        return null; // Replace with your implementation
    }


    public static Comment create(String username, String body) {
        //Implementation to create a comment
        return null; // Replace with your implementation

    }

    public static boolean delete(String id) {
        //Implementation to delete a comment
        return false; // Replace with your implementation
    }
}
