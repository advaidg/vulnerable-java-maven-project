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
    private Postgres() {} // Private constructor to prevent instantiation

    public static Connection connection() throws PostgresConnectionException {
        try {
            Class.forName("org.postgresql.Driver");
            String url = new StringBuilder()
                    .append("jdbc:postgresql://")
                    .append(System.getenv("PGHOST"))
                    .append("/")
                    .append(System.getenv("PGDATABASE")).toString();
            return DriverManager.getConnection(url,
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
        } catch (SQLException e) {
            throw new PostgresConnectionException("Failed to establish database connection.", e);
        } catch (ClassNotFoundException e) {
            throw new PostgresConnectionException("PostgreSQL JDBC driver not found.", e);
        } catch (Exception e) {
            throw new PostgresConnectionException("Unexpected error during connection.", e);
        }
    }

    public static void setup() {
        try (Connection c = connection()) {
            logger.info("Setting up Database...");
            try (Statement stmt = c.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR (36) PRIMARY KEY, username VARCHAR (50) UNIQUE NOT NULL, password VARCHAR (50) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments(id VARCHAR (36) PRIMARY KEY, username VARCHAR (36), body VARCHAR (500), created_on TIMESTAMP NOT NULL)");
                stmt.executeUpdate("DELETE FROM users");
                stmt.executeUpdate("DELETE FROM comments");
                insertUser("admin", "!!SuperSecretAdmin!!");
                insertUser("alice", "AlicePassword!");
                insertUser("bob", "BobPassword!");
                insertUser("eve", "$EVELknev^l");
                insertUser("rick", "!GetSchwifty!");
                insertComment("rick", "cool dog m8");
                insertComment("alice", "OMG so cute!");
            }
        } catch (Exception e) {
            logger.error("Error setting up database: ", e);
            //Consider more sophisticated error handling than System.exit in a production environment.  Perhaps alert monitoring system.
        }
    }

    //Improved MD5 function with better exception handling.  Note: MD5 is still insecure for passwords!
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
            logger.error("MD5 algorithm not found - this is a programming error!", e);
            throw new RuntimeException("MD5 algorithm not found.", e); //Re-throwing to halt execution -  this should never happen
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
            logger.error("Error inserting user: ", e);
        } catch (PostgresConnectionException e) {
            logger.error("Database connection error during user insertion: ", e);
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
            logger.error("Error inserting comment: ", e);
        } catch (PostgresConnectionException e) {
            logger.error("Database connection error during comment insertion: ", e);
        }
    }

    // Custom Exception for Postgres Connection Errors
    public static class PostgresConnectionException extends RuntimeException {
        public PostgresConnectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
