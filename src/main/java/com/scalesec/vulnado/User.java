package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class User {
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    private final String id;
    private final String username;
    private final String hashedPassword;

    public User(String id, String username, String hashedPassword) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String token(String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder().setSubject(this.username).signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public static void assertAuth(String secret, String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage(), e);
            throw new Unauthorized("Authentication failed.");
        }
    }

    public static Optional<User> fetch(String username) {
        try (Connection cxn = Postgres.connection(); // Assumes Postgres.connection() is properly implemented with connection pooling
             PreparedStatement stmt = cxn.prepareStatement("SELECT user_id, username, password FROM users WHERE username = ? LIMIT 1")) {
            logger.info("Opened database successfully");

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String userId = rs.getString("user_id");
                    String fetchedUsername = rs.getString("username");
                    String password = rs.getString("password");
                    return Optional.of(new User(userId, fetchedUsername, password));
                }
            }
        } catch (SQLException e) {
            logger.error("Database error during user fetch: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }
}
