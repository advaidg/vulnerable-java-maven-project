package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User {
  private static final Logger logger = LoggerFactory.getLogger(User.class);  // Replacing all System.out and System.err with a logger

  private static final String SELECT_USER_QUERY = "SELECT user_id, username, password FROM users WHERE username = ? LIMIT 1"; // Using PreparedStatement
  private String id, username, hashedPassword; 

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  } 
  
  private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("your-256-bit-secret".getBytes()); // Secret should be managed as a static final

  public String token() { 
    // We avoid fetching the secret key repeatedly and instead use a constant
    return Jwts.builder().setSubject(this.username).signWith(SECRET_KEY).compact(); // Directly returning the token
  }

  public static void assertAuth(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
    } catch(Exception e) {
      logger.error("Authentication failed: {}", e.getMessage());  // Logging the error instead of e.printStackTrace()
      throw new Unauthorized("Unauthorized access attempt.");  // Avoid leaking the raw exception message
    }
  }

  public static User fetch(String un) {
    User user = null;
    try (Connection cxn = Postgres.connection();  // Using try-with-resources to ensure automatic closing of Connection
         PreparedStatement stmt = cxn.prepareStatement(SELECT_USER_QUERY)) { // Using PreparedStatement to prevent SQL injection
         
      stmt.setString(1, un);  // Binding the variable
            
      logger.info("Executing query: {}", SELECT_USER_QUERY);
      try (ResultSet rs = stmt.executeQuery()) {  // Using try-with-resources to handle ResultSet and ensure its closure
        
        if (rs.next()) {
          String userId = rs.getString("user_id");
          String username = rs.getString("username");
          String password = rs.getString("password");
          user = new User(userId, username, password);
        }
      } 
      logger.info("Query executed successfully");
    } catch (SQLException e) {
      logger.error("SQL exception: {}", e.getMessage()); // Replacing System.err with SLF4J-compliant logger.
    } catch (Exception e) {
      logger.error("Unexpected exception: {}", e.getMessage());
    }
    return user;
  }
}
