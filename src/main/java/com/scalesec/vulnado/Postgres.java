package com.scalesec.vulnado;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class Postgres {

    private static final String DB_URL_TEMPLATE = "jdbc:postgresql://%s/%s";
    private static final String PGHOST = System.getenv("PGHOST");
    private static final String PGDATABASE = System.getenv("PGDATABASE");
    private static final String PGUSER = System.getenv("PGUSER");
    private static final String PGPASSWORD = System.getenv("PGPASSWORD");

    private static Connection connection() throws DatabaseConnectionException {
        try {
            Class.forName("org.postgresql.Driver");
            String url = String.format(DB_URL_TEMPLATE, PGHOST, PGDATABASE);
            return DriverManager.getConnection(url, PGUSER, PGPASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            throw new DatabaseConnectionException("Failed to establish database connection", e);
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
        } catch (Exception e) {
            System.err.println("Error setting up database: " + e.getMessage());
            System.exit(1);
        }
    }

    // Java program to calculate MD5 hash value
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            return String.format("%32s", no.toString(16)).replace(' ', '0');
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void insertUser(String username, String password) {
        String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
        try (PreparedStatement pStatement = connection().prepareStatement(sql)) {
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, md5(password));
            pStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
        }
    }

    private static void insertComment(String username, String body) {
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
        try (PreparedStatement pStatement = connection().prepareStatement(sql)) {
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, body);
            pStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting comment: " + e.getMessage());
        }
    }

    private static class DatabaseConnectionException extends SQLException {
        public DatabaseConnectionException(String message, Exception e) {
            super(message, e);
        }
    }
}
