package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class Postgres {

    private static final String DB_URL = "jdbc:postgresql://%s/%s";
    private static final String DB_DRIVER = "org.postgresql.Driver";
    private static final String INSERT_USER_SQL = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
    private static final String INSERT_COMMENT_SQL = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            return String.format("%32s", no.toString(16)).replace(' ', '0');
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection connection() throws SQLException {
        try {
            Class.forName(DB_DRIVER);
            String url = String.format(DB_URL, System.getenv("PGHOST"), System.getenv("PGDATABASE"));
            return DriverManager.getConnection(url, System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage(), e);
        }
    }

    public static void setup() {
        try (Connection c = connection();
             Statement stmt = c.createStatement()) {
            System.out.println("Setting up Database...");

            // Create Schema
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR (36) PRIMARY KEY, username VARCHAR (50) UNIQUE NOT NULL, password VARCHAR (50) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments(id VARCHAR (36) PRIMARY KEY, username VARCHAR (36), body VARCHAR (500), created_on TIMESTAMP NOT NULL)");

            // Clean up any existing data
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("DELETE FROM comments");

            // Insert seed data
            insertUser("admin", "!!SuperSecretAdmin!!");
            insertUser("alice", "AlicePassword!");
            insertUser("bob", "BobPassword!");
            insertUser("eve", "$EVELknev^l");
            insertUser("rick", "!GetSchwifty!");

            insertComment("rick", "cool dog m8");
            insertComment("alice", "OMG so cute!");
        } catch (SQLException e) {
            System.err.println("Error setting up database: " + e.getMessage());
        }
    }

    private static void insertUser(String username, String password) {
        try (Connection c = connection();
             PreparedStatement pStatement = c.prepareStatement(INSERT_USER_SQL)) {
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, md5(password));
            pStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
        }
    }

    private static void insertComment(String username, String body) {
        try (Connection c = connection();
             PreparedStatement pStatement = c.prepareStatement(INSERT_COMMENT_SQL)) {
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, body);
            pStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting comment: " + e.getMessage());
        }
    }

    private Postgres() {
        // Prevent instantiation
    }
}
