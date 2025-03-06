package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Postgres {

    private static final Logger LOGGER = Logger.getLogger(Postgres.class.getName());

    public static Connection connection() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = String.format("jdbc:postgresql://%s/%s", 
                                        System.getenv("PGHOST"), 
                                        System.getenv("PGDATABASE"));
            return DriverManager.getConnection(url, 
                                              System.getenv("PGUSER"), 
                                              System.getenv("PGPASSWORD"));
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
            throw new RuntimeException("Failed to connect to the database", e); //Improved error handling
        }
    }

    public static void setup() {
        try (Connection c = connection();
             Statement stmt = c.createStatement()) { //Try-with-resources for automatic closing

            LOGGER.log(Level.INFO, "Setting up Database...");

            // Create Schema - using safer `IF NOT EXISTS` for idempotency
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (user_id VARCHAR(36) PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)"); //Increased password length
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments (id VARCHAR(36) PRIMARY KEY, username VARCHAR(36), body VARCHAR(500), created_on TIMESTAMP NOT NULL)");

            // Clean up any existing data - more efficient than DELETE
            stmt.executeUpdate("TRUNCATE TABLE users");
            stmt.executeUpdate("TRUNCATE TABLE comments");


            insertUser("admin", "!!SuperSecretAdmin!!");
            insertUser("alice", "AlicePassword!");
            insertUser("bob", "BobPassword!");
            insertUser("eve", "$EVELknev^l");
            insertUser("rick", "!GetSchwifty!");

            insertComment("rick", "cool dog m8");
            insertComment("alice", "OMG so cute!");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database setup failed", e);
            throw new RuntimeException("Failed to setup the database", e); //Improved error handling
        }
    }


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
            throw new RuntimeException("MD5 algorithm not found", e); //More informative exception
        }
    }

    private static void insertUser(String username, String password) {
        String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
        try (PreparedStatement pStatement = connection().prepareStatement(sql)) { //Try-with-resources
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, md5(password));
            pStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert user", e);
            throw new RuntimeException("Failed to insert user into database", e); //Improved error handling
        }
    }

    private static void insertComment(String username, String body) {
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
        try (PreparedStatement pStatement = connection().prepareStatement(sql)) { //Try-with-resources
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, body);
            pStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert comment", e);
            throw new RuntimeException("Failed to insert comment into database", e); //Improved error handling

        }
    }
}
