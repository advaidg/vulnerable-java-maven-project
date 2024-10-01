package com.scalesec.vulnado;

import org.apache.catalina.Server;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Comment {
  // Use 'private static final' for constants according to the best practices
  private static final Logger logger = Logger.getLogger(Comment.class.getName());
  
  // Make sensitive information private and non-final; use secure methods for key/password retrieval
  private static final String KEY = System.getenv("AWS_KEY");
  private static final String password = System.getenv("AWS_PASSWORD");

  // Change visibility to private to encapsulate fields and provide accessors if needed
  private String id;
  private String username;
  private String body;
  private Timestamp createdOn;  // Rename `created_on` according to Java naming conventions

  public Comment(String id, String username, String body, Timestamp createdOn) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.createdOn = createdOn;
  }

  // Add public getters for the Comment class if needed
  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getBody() {
    return body;
  }

  public Timestamp getCreatedOn() {
    return createdOn;
  }

  public static Comment create(String username, String body) {
    long time = new Date().getTime();
    Timestamp timestamp = new Timestamp(time);
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);

    // Avoid printing sensitive data
    logger.log(Level.FINE, "Creating comment, password should not be printed");
    
    try {
      if (comment.commit()) {
        return comment;
      } else {
        throw new BadRequest("Unable to save comment");
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Database error while creating comment", e);
      throw new ServerError(e.getMessage());
    }
  }

  public static List<Comment> fetchAll() {
    List<Comment> comments = new ArrayList<>();
    String query = "SELECT id, username, body, created_on FROM comments";  // Avoid using 'SELECT *'
    
    try (Connection cxn = Postgres.connection(); 
         Statement stmt = cxn.createStatement(); 
         ResultSet rs = stmt.executeQuery(query)) {

      while (rs.next()) {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String body = rs.getString("body");
        Timestamp createdOn = rs.getTimestamp("created_on");
        
        Comment c = new Comment(id, username, body, createdOn);
        comments.add(c);
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error fetching comments", e);
    }
    return comments;
  }

  public static Boolean delete(String id) {
    String sql = "DELETE FROM comments WHERE id = ?";

    try (Connection con = Postgres.connection(); 
         PreparedStatement pStatement = con.prepareStatement(sql)) {

      pStatement.setString(1, id);
      return 1 == pStatement.executeUpdate();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error deleting comment with id: " + id, e);
      return false;
    }
  }

  private Boolean commit() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, ?)";

    try (Connection con = Postgres.connection(); 
         PreparedStatement pStatement = con.prepareStatement(sql)) {

      pStatement.setString(1, this.id);
      pStatement.setString(2, this.username);
      pStatement.setString(3, this.body);
      pStatement.setTimestamp(4, this.createdOn);
      
      return 1 == pStatement.executeUpdate();
    }
  }
}
