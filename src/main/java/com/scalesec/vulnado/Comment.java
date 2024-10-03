package com.scalesec.vulnado;

import org.apache.catalina.Server;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class Comment {
  private String id;
  private String username;
  private String body;
  private Timestamp createdOn;

  // Removed hardcoded keys
  // Replace with proper configuration management using environment variables or secrets management

  public Comment(String id, String username, String body, Timestamp createdOn) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.createdOn = createdOn;
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

  public Timestamp getCreatedOn() {
    return createdOn;
  }

  public static Comment create(String username, String body) {
    Timestamp timestamp = new Timestamp(new Date().getTime());
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
    // Use a logger instead of System.out
    //  Logger logger = LoggerFactory.getLogger(Comment.class);
    //  logger.info("password is: {}", password);
    try (Connection connection = Postgres.connection();
         PreparedStatement pStatement = connection.prepareStatement("INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)")) {
      pStatement.setString(1, comment.getId());
      pStatement.setString(2, comment.getUsername());
      pStatement.setString(3, comment.getBody());
      pStatement.setTimestamp(4, comment.getCreatedOn());
      if (1 == pStatement.executeUpdate()) {
        return comment;
      } else {
        throw new BadRequest("Unable to save comment");
      }
    } catch (SQLException e) {
      throw new ServerError(e.getMessage());
    }
  }

  public static List<Comment> fetchAll() {
    List<Comment> comments = new ArrayList<>();
    try (Connection connection = Postgres.connection();
         PreparedStatement pStatement = connection.prepareStatement("SELECT id, username, body, created_on FROM comments")) {
      ResultSet rs = pStatement.executeQuery();
      while (rs.next()) {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String body = rs.getString("body");
        Timestamp createdOn = rs.getTimestamp("created_on");
        Comment c = new Comment(id, username, body, createdOn);
        comments.add(c);
      }
      return comments;
    } catch (SQLException e) {
      // Use a logger instead of System.err
      // Logger logger = LoggerFactory.getLogger(Comment.class);
      // logger.error("Error fetching comments: {}", e.getMessage());
      return comments; // Return empty list on error
    }
  }

  public static boolean delete(String id) {
    try (Connection connection = Postgres.connection();
         PreparedStatement pStatement = connection.prepareStatement("DELETE FROM comments where id = ?")) {
      pStatement.setString(1, id);
      return 1 == pStatement.executeUpdate();
    } catch (SQLException e) {
      // Use a logger instead of System.err
      // Logger logger = LoggerFactory.getLogger(Comment.class);
      // logger.error("Error deleting comment: {}", e.getMessage());
      return false;
    }
  }

  private boolean commit() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
    try (Connection connection = Postgres.connection();
         PreparedStatement pStatement = connection.prepareStatement(sql)) {
      pStatement.setString(1, this.id);
      pStatement.setString(2, this.username);
      pStatement.setString(3, this.body);
      pStatement.setTimestamp(4, this.createdOn);
      return 1 == pStatement.executeUpdate();
    }
  }
}
