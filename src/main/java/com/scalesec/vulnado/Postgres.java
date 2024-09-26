package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Postgres {

    private static final Logger logger = LoggerFactory.getLogger(Postgres.class);

    private Postgres() {
        // Private constructor to prevent instantiation
    }

    public static Connection connection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            String url = new StringBuilder()
                    .append("jdbc:postgresql://")
                    .append(System.getenv("PGHOST"))
                    .append("/")
                    .append(System.getenv("PGDATABASE")).toString();
            return DriverManager.getConnection(url,
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
        } catch (ClassNotFoundException e) {
            throw new SQLException("Could not find PostgreSQL driver", e);
        }
    }

    public static void setup() throws SQLException {
        logger.info("Setting up Database...");
        try (Connection c = connection();
             Statement stmt = c.createStatement()) {

            // Create Schema
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR (36) PRIMARY KEY, username VARCHAR (50) UNIQUE NOT NULL, password VARCHAR (50) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments(id VARCHAR (36) PRIMARY KEY, username VARCHAR (36), body VARCHAR (500), created_on TIMESTAMP NOT NULL)");

            // Clean up any existing data
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("DELETE FROM comments");

            // Insert seed data
            insertUser("admin", "admin");
            insertUser("alice", "alice");
            insertUser("bob", "bob");
            insertUser("eve", "eve");
            insertUser("rick", "rick");

            insertComment("rick", "cool dog m8");
            insertComment("alice", "OMG so cute!");

        } catch (SQLException e) {
            throw new SQLException("Error setting up database", e);
        }
    }

    // Java program to calculate MD5 hash value
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            logger.error("MD5 algorithm not found", e);
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    private static void insertUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
        try (PreparedStatement pStatement = connection().prepareStatement(sql)) {
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, md5(password));
            pStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error inserting user", e);
        }
    }

    private static void insertComment(String username, String body) throws SQLException {
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
        try (PreparedStatement pStatement = connection().prepareStatement(sql)) {
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, body);
            pStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error inserting comment", e);
        }
    }
}


package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Postgres {

    private static final Logger logger = LoggerFactory.getLogger(Postgres.class);

    private Postgres() {
        // Private constructor to prevent instantiation
    }

    public Connection connection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            String url = new StringBuilder()
                    .append("jdbc:postgresql://")
                    .append(System.getenv("PGHOST"))
                    .append("/")
                    .append(System.getenv("PGDATABASE")).toString();
            return DriverManager.getConnection(url,
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
        } catch (ClassNotFoundException e) {
            throw new SQLException("Could not find PostgreSQL driver", e);
        }
    }

    public void setup() throws SQLException {
        logger.info("Setting up Database...");
        try (Connection c = connection();
             Statement stmt = c.createStatement()) {

            // Create Schema
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR (36) PRIMARY KEY, username VARCHAR (50) UNIQUE NOT NULL, password VARCHAR (50) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments(id VARCHAR (36) PRIMARY KEY, username VARCHAR (36), body VARCHAR (500), created_on TIMESTAMP NOT NULL)");

            // Clean up any existing data
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("DELETE FROM comments");

            // Insert seed data
            insertUser("admin", "admin");
            insertUser("alice", "alice");
            insertUser("bob", "bob");
            insertUser("eve", "eve");
            insertUser("rick", "rick");

            insertComment("rick", "cool dog m8");
            insertComment("alice", "OMG so cute!");

        } catch (SQLException e) {
            throw new SQLException("Error setting up database", e);
        }
    }

    // Java program to calculate MD5 hash value
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            logger.error("MD5 algorithm not found", e);
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    private void insertUser(String username, String password) throws SQLException {
        Map<String, String> values = new HashMap<>();
        values.put("user_id", UUID.randomUUID().toString());
        values.put("username", username);
        values.put("password", md5(password));
        insertData("users", values);
    }

    private void insertComment(String username, String body) throws SQLException {
        Map<String, String> values = new HashMap<>();
        values.put("id", UUID.randomUUID().toString());
        values.put("username", username);
        values.put("body", body);
        insertData("comments", values);
    }

    private void insertData(String tableName, Map<String, String> values) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder placeholders = new StringBuilder(" VALUES (");
        int count = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (count > 0) {
                sql.append(", ");
                placeholders.append(", ");
            }
            sql.append(entry.getKey());
            placeholders.append("?");
            count++;
        }
        sql.append(")").append(placeholders).append(")");

        try (PreparedStatement pStatement = connection().prepareStatement(sql.toString())) {
            count = 1;
            for (String value : values.values()) {
                pStatement.setString(count, value);
                count++;
            }
            pStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error inserting data into " + tableName, e);
        }
    }
}
