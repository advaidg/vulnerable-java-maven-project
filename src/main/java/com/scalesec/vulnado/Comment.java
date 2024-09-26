package com.scalesec.vulnado;

import org.apache.catalina.Server;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Comment {
  private String id;
  private String username;
  private String body;
  private Timestamp createdOn;

  private static final Logger logger = LoggerFactory.getLogger(Comment.class);

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

  public Comment create(String username, String body) {
    long time = new Date().getTime();
    Timestamp timestamp = new Timestamp(time);
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
    try {
      if (comment.commit()) {
        return comment;
      } else {
        throw new BadRequest("Unable to save comment");
      }
    } catch (SQLException e) {
      logger.error("Error saving comment: {}", e.getMessage(), e);
      throw new ServerError(e.getMessage());
    }
  }

  public List<Comment> fetchAll() {
    List<Comment> comments = new ArrayList<>();
    try (Connection cxn = Postgres.connection();
         Statement stmt = cxn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT id, username, body, created_on FROM comments")) {
      while (rs.next()) {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String body = rs.getString("body");
        Timestamp createdOn = rs.getTimestamp("created_on");
        Comment c = new Comment(id, username, body, createdOn);
        comments.add(c);
      }
    } catch (SQLException e) {
      logger.error("Error fetching comments: {}", e.getMessage(), e);
    }
    return comments;
  }

  public boolean delete(String id) {
    try (Connection con = Postgres.connection();
         PreparedStatement pStatement = con.prepareStatement("DELETE FROM comments WHERE id = ?")) {
      pStatement.setString(1, id);
      return 1 == pStatement.executeUpdate();
    } catch (SQLException e) {
      logger.error("Error deleting comment: {}", e.getMessage(), e);
      return false;
    }
  }

  private boolean commit() throws SQLException {
    try (Connection con = Postgres.connection();
         PreparedStatement pStatement = con.prepareStatement("INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)")) {
      pStatement.setString(1, this.id);
      pStatement.setString(2, this.username);
      pStatement.setString(3, this.body);
      pStatement.setTimestamp(4, this.createdOn);
      return 1 == pStatement.executeUpdate();
    }
  }
}
