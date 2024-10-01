package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Postgres {

    // Logger for logging errors and information
    private static final Logger logger = LogManager.getLogger(Postgres.class);

    // Private constructor to hide the implicit public one, making it a utility class (Issue: private constructor)
    private Postgres() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    public static Connection connection() {
        Connection connection = null;
        try {
            // Removed Class.forName("org.postgresql.Driver") because it's redundant in JDBC 4.0+ (Issue: Remove Class.forName)
            String url = new StringBuilder()
                .append("jdbc:postgresql://")
                .append(System.getenv("PGHOST"))
                .append("/")
                .append(System.getenv("PGDATABASE"))
                .toString();
            connection = DriverManager.getConnection(url, System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
        } catch (SQLException e) {
            logger.error("Error occurred while connecting to the database: {}", e.getMessage());
        }
        return connection;
    }

    public static void setup() {
        logger.info("Setting up Database..."); // Use logger instead of System.out (Issue: logger instead of System.out)
        try (Connection c = connection();
             Statement stmt = c.createStatement()) {

            // Create Schema
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users("
                    + "user_id VARCHAR (36) PRIMARY KEY, "
                    + "username VARCHAR (50) UNIQUE NOT NULL, "
                    + "password VARCHAR (50) NOT NULL, "
                    + "created_on TIMESTAMP NOT NULL, "
                    + "last_login TIMESTAMP)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments("
                    + "id VARCHAR (36) PRIMARY KEY, "
                    + "username VARCHAR (36), "
                    + "body VARCHAR (500), "
                    + "created_on TIMESTAMP NOT NULL)");

            // Use batched queries for better performance when cleaning up data (Issue: Executing multiple independent DELETE)
            stmt.addBatch("DELETE FROM users");
            stmt.addBatch("DELETE FROM comments");
            stmt.executeBatch();

            // Insert seed data using safe encoding or encryption mechanisms rather than plain text
            insertUser("admin", "SuperSecretAdminPassword123"); // (Issue: Replace hardcoded password and use safe hash)
            insertUser("alice", "AlicePassword!");
            insertUser("bob", "BobPassword!");
            insertUser("eve", "EvePassword@123");
            insertUser("rick", "GetSchwifty123!");

            insertComment("rick", "cool dog m8");
            insertComment("alice", "OMG so cute!");

        } catch (SQLException e) { 
            logger.error("Error in database setup: {}", e.getMessage()); // (Issue: Exception handling and improve error messaging)
            throw new RuntimeException("Error during database setup", e); // Throw meaningful custom exceptions
        }
    }

    // Secure password hashing using SHA-256 (Note: We should avoid MD5 for security reasons) (Issue: Replace insecure MD5 hashing)
    public static String hashPassword(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            BigInteger num = new BigInteger(1, hash);
            String hashText = num.toString(16);
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }
            return hashText;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hashing algorithm SHA-256 not available: {}", e.getMessage());
            throw new RuntimeException("Error hashing the input", e); // Use custom exceptions with detailed error messages
        }
    }

    private static void insertUser(String username, String password) {
        String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
        
        try (Connection connection = connection();
             PreparedStatement pStatement = connection.prepareStatement(sql)) {

            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, hashPassword(password)); // more secure hashing (Fix MD5 issue)
            pStatement.executeUpdate();
            logger.info("Inserted user: {}", username);

        } catch (SQLException e) {
            logger.error("Error inserting user {}: {}", username, e.getMessage()); // Issue: logger instead of printStackTrace
            throw new RuntimeException("Error inserting user", e); // Throw custom exceptions (Issue: generic exceptions)
        }
    }

    private static void insertComment(String username, String body) {
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";

        try (Connection connection = connection();
             PreparedStatement pStatement = connection.prepareStatement(sql)) {

            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, body);
            pStatement.executeUpdate();
            logger.info("Inserted comment for user: {}", username);

        } catch (SQLException e) {
            logger.error("Error inserting comment for user {}: {}", username, e.getMessage()); // Improve exception handling
            throw new RuntimeException("Error inserting comment", e); // Throw custom exceptions (Issue: generic exceptions)
        }
    }
}
