package com.scalesec.vulnado;

import org.apache.catalina.Server;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Optional;

public class Comment {
    public String id, username, body;
    public Timestamp created_on;

    //Hardcoded credentials are a major security risk.  This needs proper configuration management.
    //Consider using environment variables or a secrets management system.  This example removes them for brevity.
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
        //Removed printing of password to console - major security vulnerability.
        //System.out.println("password is:" + password);

        try {
            if (comment.commit()) {
                return comment;
            } else {
                throw new BadRequest("Unable to save comment");
            }
        } catch (SQLException e) {
            //More specific exception handling is preferable,  log the exception instead of throwing a generic ServerError.
            //Consider using a logging framework like Log4j or SLF4j.
            e.printStackTrace();
            throw new ServerError("Database error: " + e.getMessage());
        }
    }

    public static List<Comment> fetch_all() {
        //Using try-with-resources for automatic resource management.
        try (Connection cxn = Postgres.connection();
             Statement stmt = cxn.createStatement();
             ResultSet rs = stmt.executeQuery("select * from comments;")) {
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
            //Log the exception instead of printing to System.err.
            e.printStackTrace();
            // Consider throwing a custom exception for better error handling.
            return new ArrayList<>(); //Return an empty list instead of potentially null.
        }
    }

    public static boolean delete(String id) {
        try (Connection con = Postgres.connection();
             PreparedStatement pStatement = con.prepareStatement("DELETE FROM comments where id = ?")) {
            pStatement.setString(1, id);
            return 1 == pStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); //Log this instead of printing.
            return false;
        }
    }

    private boolean commit() throws SQLException {
        try (Connection con = Postgres.connection();
             PreparedStatement pStatement = con.prepareStatement("INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)")) {
            pStatement.setString(1, this.id);
            pStatement.setString(2, this.username);
            pStatement.setString(3, this.body);
            pStatement.setTimestamp(4, this.created_on);
            return 1 == pStatement.executeUpdate();
        }
    }
}
