package com.scalesec.vulnado;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Comment {

  private static final Logger logger = LoggerFactory.getLogger(Comment.class);

  public String id;
  public String username;
  public String body;
  public Timestamp createdOn;

  public Comment(String id, String username, String body, Timestamp createdOn) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.createdOn = createdOn;
  }

  public static Comment create(String username, String body) {
    long time = new Date().getTime();
    Timestamp timestamp = new Timestamp(time);
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
    try (Connection con = Postgres.connection();
        PreparedStatement pStatement = con.prepareStatement("INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)")) {
      pStatement.setString(1, comment.id);
      pStatement.setString(2, comment.username);
      pStatement.setString(3, comment.body);
      pStatement.setTimestamp(4, comment.createdOn);
      if (1 == pStatement.executeUpdate()) {
        return comment;
      } else {
        throw new BadRequest("Unable to save comment");
      }
    } catch (SQLException e) {
      logger.error("Error saving comment: {}", e.getMessage(), e);
      throw new ServerError("Error saving comment");
    }
  }

  public static List<Comment> fetchAll() {
    List<Comment> comments = new ArrayList<>();
    try (Connection cxn = Postgres.connection();
        PreparedStatement stmt = cxn.prepareStatement("SELECT id, username, body, created_on FROM comments")) {
      ResultSet rs = stmt.executeQuery();
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

  public static boolean delete(String id) {
    try (Connection con = Postgres.connection();
        PreparedStatement pStatement = con.prepareStatement("DELETE FROM comments where id = ?")) {
      pStatement.setString(1, id);
      return 1 == pStatement.executeUpdate();
    } catch (SQLException e) {
      logger.error("Error deleting comment: {}", e.getMessage(), e);
      return false;
    }
  }

  private boolean commit() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
    Connection con = Postgres.connection();
    PreparedStatement pStatement = con.prepareStatement(sql);
    pStatement.setString(1, this.id);
    pStatement.setString(2, this.username);
    pStatement.setString(3, this.body);
    pStatement.setTimestamp(4, this.createdOn);
    return 1 == pStatement.executeUpdate();
  }
}
