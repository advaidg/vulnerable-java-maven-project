package com.scalesec.vulnado;

import org.apache.catalina.Server;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Optional;

// Assuming BadRequest and ServerError are custom exceptions defined elsewhere.  If not, define them.
class BadRequest extends Exception {
    public BadRequest(String message) {
        super(message);
    }
}

class ServerError extends Exception {
    public ServerError(String message) {
        super(message);
    }
}


public class Comment {
    public String id;
    public String username;
    public String body;
    public Timestamp created_on;

    //Hardcoded credentials are a major security risk.  Replace with proper secret management.
    //This is a placeholder.  In a real application use environment variables, a secrets manager (like AWS Secrets Manager, HashiCorp Vault, etc.), or a dedicated configuration system.

    //Remove completely - this is a serious security vulnerability.
    //public static final String KEY = "AKIAIOSFODNN7EXAMPLG";
    //public static final String password = "AKIAIOSFODNN7EXAMPLG";


    public Comment(String id, String username, String body, Timestamp created_on) {
        this.id = id;
        this.username = username;
        this.body = body;
        this.created_on = created_on;
    }

    public static Comment create(String username, String body) {
        long time = new Date().getTime();
        Timestamp timestamp = new Timestamp(time);
        Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
        //Removed printing of password - security risk.
        try {
            if (comment.commit()) {
                return comment;
            } else {
                throw new BadRequest("Unable to save comment");
            }
        } catch (SQLException e) {
            throw new ServerError("Database error: " + e.getMessage()); //More informative error message.
        }
    }

    public static List<Comment> fetchAll() {
        try (Connection cxn = Postgres.connection();
             Statement stmt = cxn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM comments;")) { //Try-with-resources for automatic closing.
            List<Comment> comments = new ArrayList<>();
            while (rs.next()) {
                comments.add(new Comment(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("body"),
                        rs.getTimestamp("created_on")
                ));
            }
            return comments;
        } catch (SQLException e) {
            //Log the exception properly instead of printing to System.err.  Use a logging framework like Log4j or SLF4j.
            //Example using System.err for demonstration purposes only.  This should be replaced with a proper logging framework.
            System.err.println("Error fetching comments: " + e.getMessage());
            return new ArrayList<>(); //Return an empty list instead of null for better error handling.
        }
    }


    public static boolean delete(String id) {
        try (Connection con = Postgres.connection();
             PreparedStatement pStatement = con.prepareStatement("DELETE FROM comments WHERE id = ?")) {
            pStatement.setString(1, id);
            return pStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            //Log the exception properly.
            System.err.println("Error deleting comment: " + e.getMessage());
            return false;
        }
    }

    private boolean commit() throws SQLException {
        try (Connection con = Postgres.connection();
             PreparedStatement pStatement = con.prepareStatement("INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, ?)")) {
            pStatement.setString(1, this.id);
            pStatement.setString(2, this.username);
            pStatement.setString(3, this.body);
            pStatement.setTimestamp(4, this.created_on);
            return pStatement.executeUpdate() == 1;
        }
    }
}
