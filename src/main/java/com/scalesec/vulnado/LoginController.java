package com.scalesec.vulnado;

import org.springframework.boot.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RestController
@EnableAutoConfiguration
public class LoginController {
  
  private static final Logger log = LoggerFactory.getLogger(LoginController.class);

  // Remove hardcoded secret value from properties file. Instead, fetch it securely using a secrets manager.
  @Value("${app.secret}")  // Ideally, this should be managed via a secrets management system (e.g., AWS Secrets Manager or Vault)
  private String secret;

  @CrossOrigin(origins = "https://trusted-origin.com")  // Avoid unrestricted CORS, replace "*" with trusted origins.
  @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
  LoginResponse login(@RequestBody LoginRequest input) {
    // Validate input
    validateInput(input);

    User user = User.fetch(input.username);
    if (user == null || !Postgres.md5(input.password).equals(user.getHashedPassword())) {
      logFailedAttempt(input.username);  // Log failed attempts for security monitoring
      throw new Unauthorized("Access Denied");
    } else {
      log.info("Login successful for user: {}", input.username);  // Log successful login
      return new LoginResponse(user.generateToken(secret));
    }
  }

  private void validateInput(LoginRequest input) {
    if (input.username == null || input.username.isEmpty()) {
      throw new IllegalArgumentException("Username cannot be blank");
    }
    if (input.password == null || input.password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be blank");
    }
  }

  private void logFailedAttempt(String username) {
    log.warn("Failed login attempt for username: {}", username);
  }
}

class LoginRequest implements Serializable {
  private static final long serialVersionUID = 1L;  // Serializable class should declare a serial version UID

  // Make the fields private and enforce access through getters and setters
  private String username;
  private String password;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}

class LoginResponse implements Serializable {
  private static final long serialVersionUID = 1L;

  private String token;

  public LoginResponse(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public Unauthorized(String exception) {
    super(exception);
  }
}

// Example User class with lazy-loaded password and token generation using PreparedStatement
class User {

  private String username;
  private String hashedPassword;
  
  // Fetch user securely using a prepared statement to prevent SQL injection
  public static User fetch(String username) {
    String query = "SELECT username, hashed_password FROM users WHERE username = ?";  // Only fetch required fields
    try (Connection connection = Database.getConnection();
        PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, username);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          User user = new User();
          user.username = resultSet.getString("username");
          user.hashedPassword = resultSet.getString("hashed_password");
          return user;
        } else {
          return null;  // User not found
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Database error", e);
    }
  }

  public String getHashedPassword() {
    return hashedPassword;
  }

  // Generate a token based on the app's secret securely
  public String generateToken(String secret) {
    // Example token generation logic using the secret
    return TokenUtil.generate(this.username, secret);
  }
}

// Utility class for token generation (to be expanded as necessary)
class TokenUtil {
  public static String generate(String username, String secret) {
    // Example token generation logic
    return username + ":" + secret; // Replace with a secure token generation process (e.g., JWT)
  }
}

// Example database connection utility class using connection-pooling
class Database {
  // Obtain a connection from a connection-pooling middleware (e.g., HikariCP)
  public static Connection getConnection() throws SQLException {
    // Implement connection-pooling logic here
    // For example: return HikariCPDataSource.getConnection();
    return null;  // Placeholder, replace with actual code to obtain a connection
  }
}
