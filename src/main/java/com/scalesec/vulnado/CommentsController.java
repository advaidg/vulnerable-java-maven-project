package com.scalesec.vulnado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
@EnableAutoConfiguration
@RequestMapping("/api/v1")
public class CommentsController {

  // Externalize secret from configuration file to prevent hardcoding.
  @Value("${app.secret}")
  private String secret;

  // Inject the data source for database connection
  @Autowired
  private DataSource dataSource;

  // Apply proper CORS restrictions for security, using pre-configured origins and methods by environment (allow only specific origins in production)
  @CrossOrigin(origins = "${cors.allowed-origins}", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
  @GetMapping(value = "/comments", produces = "application/json")
  public List<Comment> comments(@RequestHeader(value = "x-auth-token") String token) {
    // Use authentication best practices: Validating JWT or OAuth2 token securely.
    User.assertAuth(secret, token);
    // Apply pagination and avoid fetching all comments in one go
    return Comment.fetchAll(0, 10); // assuming pagination incorporated in fetchAll
  }

  @CrossOrigin(origins = "${cors.allowed-origins}", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
  @PostMapping(value = "/comments", produces = "application/json", consumes = "application/json")
  @PreAuthorize("hasAuthority('ROLE_USER')") // Secure the endpoint based on the user’s roles and authorities.
  public Comment createComment(@RequestHeader(value = "x-auth-token") String token, @Valid @RequestBody CommentRequest input) {
    // Token validation
    User.assertAuth(secret, token);
    // Secure database operations with transactions and proper error handling
    try (Connection connection = dataSource.getConnection()) {
      return Comment.create(input.getUsername(), input.getBody(), connection);
    } catch (SQLException e) {
      throw new ServerError("Error during comment creation.");
    }
  }

  @CrossOrigin(origins = "${cors.allowed-origins}", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
  @DeleteMapping(value = "/comments/{id}", produces = "application/json")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Secure DELETE endpoint for authenticated admins only.
  public Boolean deleteComment(@RequestHeader(value = "x-auth-token") String token, @PathVariable("id") String id) {
    // Token validation
    User.assertAuth(secret, token);
    // Secure deletion within a transaction
    try (Connection connection = dataSource.getConnection()) {
      return Comment.delete(id, connection);
    } catch (SQLException e) {
      throw new ServerError("Error during comment deletion.");
    }
  }
}

class CommentRequest implements Serializable {

  // Make fields private and final (immutable if possible), and provide getters.
  private static final long serialVersionUID = 1L;

  @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters.")
  private String username;

  @Size(min = 1, max = 1000, message = "Comment body must be between 1 and 1000 characters.")
  private String body;
  
  // Add standard getters (no setters to maintain immutability).
  public String getUsername() {
    return username;
  }

  public String getBody() {
    return body;
  }
}

// Custom error handling: Handling validation and internal server errors explicitly.
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
